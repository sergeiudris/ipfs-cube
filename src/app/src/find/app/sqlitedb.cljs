(ns find.app.sqlitedb
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string]
   [clojure.pprint :refer [pprint]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]))

(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce Sqlite3 (js/require "sqlite3"))

(defonce ^:private registryA (atom {}))

(defn start
  [{:keys [:peer-index
           :id]
    :or {id :main}
    :as opts}]
  (go
    (let [stateA (atom nil)
          db-filename (.join path
                             js/__dirname
                             (format "../../volumes/peer%s/db.sqlite3" peer-index))
          db (Sqlite3.Database. db-filename)
          transact| (chan (sliding-buffer 100))
          torrent| (chan (sliding-buffer 100))]

      (.serialize db
                  (fn []
                    (.run db "CREATE TABLE IF NOT EXISTS torrents (infohash TEXT PRIMARY KEY)")
                    #_(let [statement (.prepare db "INSERT INTO lorem VALUES (?)")]
                        (doseq [i (range 0 10)]
                          (.run statement (str "Ipsum " i)))
                        (.finalize statement))

                    (.get db "SELECT COUNT(rowid) FROM torrents"
                          (fn [error row]
                            (pprint (js->clj row))))
                    #_(.each db "SELECT rowid AS id, info FROM lorem"
                             (fn [error row]
                               (println (str (. row -id) ":" (. row -info)))))))

      (go
        (loop []
          (<! (timeout 10000))
          (.get db "SELECT COUNT(rowid) FROM torrents"
                (fn [error row]
                  (pprint (js->clj row))))
          (recur)))

      (go
        (loop [batch (transient [])]
          (when-let [value (<! torrent|)]

            (cond

              (= (count batch) 500)
              (do
                (put! transact| (persistent! batch))
                (recur (transient [])))

              :else
              (recur (conj! batch value))))))

      (go
        (loop []
          (when-let [batch (<! transact|)]
            (.serialize db
                        (fn []
                          (.run db "BEGIN TRANSACTION")
                          (let [statement (.prepare db "INSERT INTO torrents(infohash) VALUES (?) ON CONFLICT DO NOTHING")]
                            (doseq [{:keys [infohash]} batch]
                              (.run statement infohash))
                            (.finalize statement))
                          (.run db "COMMIT")))
            (recur))))
      (reset! stateA {:db db
                      :torrent| torrent|})
      (swap! registryA assoc id stateA)
      stateA)))

(defn stop
  [{:keys [:id]
    :or {id :main}}]
  (when-let [stateA (get @registryA id)]
    (let [result| (chan 1)]
      (.close (:db @stateA)
              (fn [error]
                (if error
                  (println ::db-close-error error)
                  (println ::db-closed-ok))
                (swap! registryA dissoc id)
                (close! result|)))
      result|)))