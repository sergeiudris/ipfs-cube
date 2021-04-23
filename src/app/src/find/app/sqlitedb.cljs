(ns find.app.sqlitedb
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string]
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
    (let [db-filename (.join path
                             js/__dirname
                             (format "../../volumes/peer%s/db.sqlite3" peer-index))
          db (Sqlite3.Database. db-filename)]
      (.serialize db
                  (fn []
                    (.run db "CREATE TABLE IF NOT EXISTS lorem (info TEXT)")
                    #_(let [statement (.prepare db "INSERT INTO lorem VALUES (?)")]
                        (doseq [i (range 0 10)]
                          (.run statement (str "Ipsum " i)))
                        (.finalize statement))

                    (.each db "SELECT rowid AS id, info FROM lorem"
                           (fn [error row]
                             (println (str (. row -id) ":" (. row -info)))))))
      (swap! registryA assoc id db)
      db)))

(defn stop
  [{:keys [:id]
    :or {id :main}}]
  (when-let [db (get @registryA id)]
    (let [result| (chan 1)]
      (.close db
              (fn [error]
                (if error
                  (println ::db-close-error error)
                  (println ::db-closed-ok))
                (swap! registryA dissoc id)
                (close! result|)))
      result|)))