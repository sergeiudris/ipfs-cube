(ns find.app.orbitdb
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [clojure.string]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [find.app.core :refer [transit-write transit-read]]))

(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce crypto (js/require "crypto"))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IPFS (js/require "ipfs"))

#_(defn start
    [{:keys [:ipfsd
             :data-dir]
      :as opts}]
    (let [stateA (atom {})
          torrents| (chan 1000)]
      (go
        (let [orbitdb (<p! (.createInstance OrbitDB
                                            (.-api ipfsd)
                                            (clj->js {"directory" (.join path
                                                                         data-dir
                                                                         "orbitdb")})))
              eventlog (<p! (.eventlog orbitdb
                                       "github-find-main-eventlog"
                                       (clj->js {"accessController"
                                                 {"write" ["*"]}})))]
          (println (.. eventlog -address (toString)))
          #_(<p! (.drop eventlog))
          (<p! (.load eventlog))
          (let [entries (-> eventlog
                            (.iterator  #js {"limit" -1
                                             "reverse" true})
                            (.collect)
                            (vec))]
            (println ::count-entries (count entries)))

          (.. eventlog -events
              (on "replicated"
                  (fn [address]
                    (println ::repllicated))))

          (.. eventlog -events
              (on "replicate.progress"
                  (fn [address hash entry progress have]
                    (println ::replicate-progress)
                    (println (read-string (.-value (.-payload entry))))
                    #_(let [value (read-string (.-value (.-payload entry)))]
                        (put! ops| value)))))

          #_(let [peer-id (.-id (<p! (.. ipfsd -api (id))))
                  counterV (volatile! (rand-int 100))]
              (go
                (loop []
                  (<! (timeout 2000))
                  (<p! (.add eventlog
                             (pr-str {:peer-id peer-id
                                      :counter @counterV})))
                  (vswap! counterV inc)
                  (recur))))

          #_(.on (.-events app-eventlog)
                 "write"
                 (fn [address entry heads])))
        stateA)))

(defn start
  [{:keys [:ipfsd
           :data-dir]
    :as opts}]
  (let []
    (go
      (let [orbitdb (<p! (.createInstance OrbitDB
                                          (.-api ipfsd)
                                          (clj->js {"directory" (.join path
                                                                       data-dir
                                                                       "orbitdb")})))]

        (let [db (<p! (.eventlog orbitdb
                                 "github-find-foo"
                                 (clj->js {"accessController" {"write" ["*"]}
                                           "indexBy" "infohash"})))]
          (println (.. db -address (toString)))
          #_(<p! (.drop db))
          (println :starting-load)
          (time (<p! (.load db)))
          (println :loaded)
          (<p! (-> (.add db (clj->js {:infohash "foo"}))
                   (.catch (fn [error]
                             (js/console.log error)))))
          (let [entries (-> db
                            (.iterator  #js {"limit" -1
                                             "reverse" true})
                            (.collect)
                            (vec))]
            (println ::count-entries (count entries)))

          (let [entries (map (fn [i]
                               [i (clj->js {:infohash (.toString (.randomBytes crypto 20) "hex")
                                            :metadata {:name (str "some torrent name details 12312312 foo bar " i)
                                                       :files (map (fn [n]
                                                                     {:name (str "some file name details 12312312 foo bar " n)}) (range 0 10))}})]) (range 0 10000))]
            (println :starting-to-add (count entries))
            (time
             (doseq [[i entry] entries]
               (when (= 0 (mod i 100))
                 (println i))
               (<p! (.add db entry #js {:pin false})))))

          (.. db -events
              (on "replicate.progress"
                  (fn [address hash entry progress have]
                    (println ::replicate-progress progress)
                    #_(println (read-string (.-value (.-payload entry))))
                    #_(let [value (read-string (.-value (.-payload entry)))]
                        (put! ops| value))))))))))


#_(defn start
    [{:keys [:data-dir]
      :as opts}]
    (let []
      (go
        (let [ipfs (<p! (->
                         (.create IPFS (clj->js {:repo (.join path
                                                              data-dir
                                                              "ipfs")}))
                         (.catch #(println %))))
              orbitdb (<p! (.createInstance OrbitDB
                                            ipfs
                                            (clj->js {"directory" (.join path
                                                                         data-dir
                                                                         "orbitdb")})))]

          (let [db (<p! (.eventlog orbitdb
                                   "github-find-foo"
                                   (clj->js {"accessController" {"write" ["*"]}
                                             "indexBy" "infohash"})))]
            (println (.. db -address (toString)))
            #_(<p! (.drop db))
            (println :starting-load)
            (time (<p! (.load db)))
            (println :loaded)
            (<p! (-> (.add db (clj->js {:infohash "foo"}))
                     (.catch (fn [error]
                               (js/console.log error)))))
            (let [entries (-> db
                              (.iterator  #js {"limit" -1
                                               "reverse" true})
                              (.collect)
                              (vec))]
              (println ::count-entries (count entries)))

            (let [entries (map (fn [i]
                                 [i (clj->js {:infohash (.toString (.randomBytes crypto 20) "hex")
                                              :metadata {:name (str "some torrent name details 12312312 foo bar " i)
                                                         :files (map (fn [n]
                                                                       {:name (str "some file name details 12312312 foo bar " n)}) (range 0 10))}})]) (range 0 10000))]
              (println :starting-to-add (count entries))
              (time
               (doseq [[i entry] entries]
                 (when (= 0 (mod i 100))
                   (println i))
                 (<p! (.add db entry #js {:pin false})))))

            (.. db -events
                (on "replicate.progress"
                    (fn [address hash entry progress have]
                      (println ::replicate-progress progress)
                      #_(println (read-string (.-value (.-payload entry))))
                      #_(let [value (read-string (.-value (.-payload entry)))]
                          (put! ops| value))))))


          #_(let [db (<p! (.docs orbitdb
                                 "github-find-foo"
                                 (clj->js {"accessController" {"write" ["*"]}
                                           "indexBy" "infohash"})))]

              (println (.. db -address (toString)))
              #_(<p! (.drop eventlog))
              (println :starting-load)
              (time (<p! (.load db)))
              (println :loaded)

              (time
               (doseq [i (range 0 100)
                       :let [batch (map (fn [j]
                                          (clj->js {:infohash (.toString (.randomBytes crypto 20) "hex")
                                                    :metadata {:name (str "some torrent name details 12312312 foo bar " i j)
                                                               :files (map (fn [n]
                                                                             {:name (str "some file name details 12312312 foo bar " n)}) (range 0 10))}})) (range 0 10000))]]
                 (println i)
                 (<p! (.putAll db (into-array batch) #js {:pin true}))))

              (.. db -events
                  (on "replicate.progress"
                      (fn [address hash entry progress have]
                        (println ::replicate-progress progress)
                        #_(println (read-string (.-value (.-payload entry))))
                        #_(let [value (read-string (.-value (.-payload entry)))]
                            (put! ops| value))))))))))