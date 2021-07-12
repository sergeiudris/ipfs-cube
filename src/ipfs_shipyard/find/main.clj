(ns ipfs-shipyard.find.main
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

   [ipfs-shipyard.find.spec :as find.spec]
   [ipfs-shipyard.find.cljfx :as find.cljfx]
   [ipfs-shipyard.find.db :as find.db]
   [ipfs-shipyard.find.bittorrent-dht-crawl :as find.bittorrent-dht-crawl]
   [ipfs-shipyard.find.ipfs-dht :as find.ipfs-dht]))

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
    (add-watch stateA :watch-fn (fn [k stateA old-state new-state] (find.cljfx/render ctx new-state)))

    (go
      #_(<! (find.bittorrent-dht-crawl/start {:data-dir state-dir}))
      (find.cljfx/start ctx))

    (reset! ctxA ctx)))

(comment

  (require
   '[cljctools.bytes.core :as bytes.core]
   '[ipfs-shipyard.find.ipfs-dht :as find.ipfs-dht]
   '[ipfs-shipyard.find.spec :as find.spec]
   '[ipfs-shipyard.find.cljfx :as find.cljfx]
   '[ipfs-shipyard.find.db :as find.db]
   :reload)
  
  (-main)

  (do
    (def ctx @ctxA)
    (def stateA (:stateA ctx)))

  (find.cljfx/render ctx @(:stateA ctx))

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

