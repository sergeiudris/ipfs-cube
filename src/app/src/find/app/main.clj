(ns find.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]
   [find.spec]
   [find.app.http :as app.http]
   [cljctools.system-tray.core :as system-tray.core]
   [find.app.ipfs :as app.ipfs]
   [find.app.cljfx :as app.cljfx]
   [cljctools.bittorrent.dht-crawl.core :as dht-crawl.core]))

(defn stop
  [{:keys [::app.http/port] :as opts}]
  (go
    (<! (app.http/stop {::app.http/port port}))))

(println "clojure.core.async.pool-size" (System/getProperty "clojure.core.async.pool-size"))
(println "clojure.compiler.direct-linking" (System/getProperty "clojure.compiler.direct-linking"))
(clojure.spec.alpha/check-asserts true)

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
               http-opts {::app.http/port 4080}]
           (go
             (<! system-exit|)
             (println ::exit|)
             (<! (stop http-opts))
             (println ::exiting)
             (System/exit 0))

           #_(<! (app.http/start http-opts))
           (<! (dht-crawl.core/start {:data-dir state-dir}))
           #_(app.cljfx/start)
           #_(when system-tray?
               (system-tray.core/create {:on-quit (fn [] (close! system-exit|))
                                         :image (clojure.java.io/resource "logo/logo.png")}))))))

(comment

  (require '[cljctools.bytes.core :as bytes.core])

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