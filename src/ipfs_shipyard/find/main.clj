(ns ipfs-shipyard.find.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]

   [ipfs-shipyard.find.spec :as find.spec]
   [ipfs-shipyard.find.cljfx :as find.cljfx]
   [ipfs-shipyard.find.db :as find.db]
   [ipfs-shipyard.find.bittorrent-dht-crawl :as find.bittorrent-dht-crawl]
   [ipfs-shipyard.find.ipfs-dht :as find.ipfs-dht]
   [ipfs-shipyard.find.bittorrent :as find.bittorrent]))

(println "clojure.core.async.pool-size" (System/getProperty "clojure.core.async.pool-size"))
(println "clojure.compiler.direct-linking" (System/getProperty "clojure.compiler.direct-linking"))
(clojure.spec.alpha/check-asserts true)
(set! *warn-on-reflection* true)

(defn stop
  [{:keys [] :as opts}]
  (go))

(defn -main [& args]
  (println ::-main)
  (<!! (go
         (let [stateA (atom {})
               system-exit| (chan 1)
               peer-index (or (System/getenv "FIND_PEER_INDEX") 1)
               data-dir (->
                         (io/file (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index))
                         (.getCanonicalPath))
               state-dir (->
                          (io/file data-dir "state")
                          (.getCanonicalPath))]
           (go
             (<! system-exit|)
             (println ::exit|)
             (println ::exiting)
             (System/exit 0))

           #_(<! (find.bittorrent-dht-crawl/start {:data-dir state-dir}))
           (find.cljfx/start)))))

(comment

  (require
   '[cljctools.bytes.core :as bytes.core]
   '[cljctools.ipfs.dht.core :as dht.core]
   '[cljctools.ipfs.dht.impl :as dht.impl]
   :reload)

  ;
  )


(comment

  (let [peer-index (or (System/getenv "FIND_PEER_INDEX") 1)
        data-dir (io/file (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index))]
    (println data-dir)
    (println (.getCanonicalFile data-dir))
    (println (.getAbsolutePath data-dir)))

  ;;
  )