(ns find.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [find.spec]
   [find.app.http :as app.http]
   [find.app.system-tray :as app.system-tray]
   [find.app.ipfs :as app.ipfs]))

(defn stop
  [{:keys [::app.http/port] :as opts}]
  (go
    (<! (app.http/stop {::app.http/port port}))))

(defn -main [& args]
  (println ::-main)
  (<!! (go
         (let [stateA (atom {})
               system-exit| (chan 1)
               system-tray? (if (System/getenv "FIND_SYSTEM_TRAY")
                              (read-string (System/getenv "FIND_SYSTEM_TRAY"))
                              false)
               peer-index (System/getenv "FIND_PEER_INDEX")
               http-opts {::app.http/port 4080}]
           (go
             (loop []
               (when-let [[value port] (alts! [system-exit|])]
                 (condp = port

                   system-exit|
                   (do
                     (let []
                       (println ::exit|)
                       (<! (stop http-opts))
                       (println ::exiting)
                       (System/exit 0)))))))

           (<! (app.http/start http-opts))
           (when system-tray?
             (app.system-tray/mount {::app.system-tray/quit| system-exit|}))))))
