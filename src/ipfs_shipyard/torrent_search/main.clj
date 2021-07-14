(ns ipfs-shipyard.torrent-search.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]

   [cljctools.fs.runtime.core :as fs.runtime.core]
   [cljctools.fs.protocols :as fs.protocols]

   [ipfs-shipyard.torrent-search.spec :as torrent-search.spec]
   [ipfs-shipyard.torrent-search.cljfx :as torrent-search.cljfx]
   [ipfs-shipyard.torrent-search.db :as torrent-search.db]
   [ipfs-shipyard.torrent-search.bittorrent-dht-crawl :as torrent-search.bittorrent-dht-crawl]
   [ipfs-shipyard.torrent-search.ipfs-dht :as torrent-search.ipfs-dht]))

(println "clojure.core.async.pool-size" (System/getProperty "clojure.core.async.pool-size"))
(println "clojure.compiler.direct-linking" (System/getProperty "clojure.compiler.direct-linking"))
(clojure.spec.alpha/check-asserts true)
(set! *warn-on-reflection* true)

(def ctxA (atom nil))

(defn -main [& args]
  (println ::-main)
  (let [stateA (atom {:searchS ""})
        peer-index (or (System/getenv "FIND_PEER_INDEX") 1)
        data-dir (fs.runtime.core/path-join (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index))
        state-dir (fs.runtime.core/path-join data-dir "state")
        ctx {:stateA stateA}]
    (add-watch stateA :watch-fn (fn [k stateA old-state new-state] (torrent-search.cljfx/render ctx new-state)))

    (go
      #_(<! (torrent-search.bittorrent-dht-crawl/start {:data-dir state-dir}))
      (torrent-search.cljfx/start ctx))

    (reset! ctxA ctx)))

(comment

  (require
   '[cljctools.bytes.core :as bytes.core]
   '[ipfs-shipyard.torrent-search.ipfs-dht :as torrent-search.ipfs-dht]
   '[ipfs-shipyard.torrent-search.spec :as torrent-search.spec]
   '[ipfs-shipyard.torrent-search.cljfx :as torrent-search.cljfx]
   '[ipfs-shipyard.torrent-search.db :as torrent-search.db]
   :reload)
  
  (-main)

  (do
    (def ctx @ctxA)
    (def stateA (:stateA ctx)))

  (torrent-search.cljfx/render ctx @(:stateA ctx))

  (swap! stateA assoc :searchS "123")

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

