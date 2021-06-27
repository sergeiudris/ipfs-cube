(ns ipfs-shipyard.find.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]

   [cljctools.system-tray.core :as system-tray.core]
   [ipfs-shipyard.find.bittorrent-dht-crawl.core :as bittorrent-dht-crawl.core]

   [ipfs-shipyard.find.spec :as find.spec]
   [ipfs-shipyard.find.app.http :as find.app.http]
   [ipfs-shipyard.find.app.ipfs :as find.app.ipfs]
   [ipfs-shipyard.find.app.cljfx :as find.app.cljfx]))

(println "clojure.core.async.pool-size" (System/getProperty "clojure.core.async.pool-size"))
(println "clojure.compiler.direct-linking" (System/getProperty "clojure.compiler.direct-linking"))
(clojure.spec.alpha/check-asserts true)
(set! *warn-on-reflection* true)

(defn stop
  [{:keys [::find.app.http/port] :as opts}]
  (go
    (<! (find.app.http/stop {::find.app.http/port port}))))

(defn -main [& args]
  (println ::-main)
  (<!! (go
         (let [stateA (atom {})
               system-exit| (chan 1)
               system-tray? (if (System/getenv "FIND_SYSTEM_TRAY")
                              (read-string (System/getenv "FIND_SYSTEM_TRAY"))
                              false)
               peer-index (or (System/getenv "FIND_PEER_INDEX") 1)
               data-dir (->
                         (io/file (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index))
                         (.getCanonicalPath))
               state-dir (->
                          (io/file data-dir "state")
                          (.getCanonicalPath))
               http-opts {::find.app.http/port 4080}]
           (go
             (<! system-exit|)
             (println ::exit|)
             (<! (stop http-opts))
             (println ::exiting)
             (System/exit 0))

           #_(<! (find.app.http/start http-opts))
           (<! (bittorrent-dht-crawl.core/start {:data-dir state-dir}))
           #_(find.app.cljfx/start)
           #_(when system-tray?
               (system-tray.core/create {:on-quit (fn [] (close! system-exit|))
                                         :image (clojure.java.io/resource "logo/logo.png")}))))))

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