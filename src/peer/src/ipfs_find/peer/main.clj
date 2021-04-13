(ns ipfs-find.peer.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [ipfs-find.peer.spec :as app.spec]

   [ipfs-find.peer.reitit :as peer.reitit]
   [ipfs-find.peer.dgraph :as peer.dgraph]
   [ipfs-find.peer.system-tray :as peer.system-tray]
   [ipfs-find.peer.ipfs :as peer.ipfs]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [{:keys [::id] :as opts}]
  {::id id
   ::app.spec/state* (atom {})
   ::system-tray? (or (boolean (System/getenv "IPFSFIND_SYSTEM_TRAY")) true)
   ::channels {::app.spec/system-exit| (chan 1)}
   ::port 4080})

(def peer1-preset (create-opts
                   {::id :peer1
                    ::port 4081}))

(def peer2-preset (create-opts
                   {::id :peer2
                    ::port 4082}))

(defn unmount
  [{:keys [::id] :as opts}]
  (go
    (let [{:keys [::dgraph-opts
                  ::channels
                  ::port]} opts]
      (<! (peer.reitit/stop channels {::peer.reitit/port port}))
      #_(let [opts-in-registry (get @registry-ref id)]
          (when (::procs-exit opts-in-registry)
            (<! ((::procs-exit opts-in-registry)))))
      (swap! registry-ref dissoc id)
      (println ::unmount-done))))

(defn mount
  [{:keys [::id] :as opts}]
  (go
    (let [opts (merge (create-opts opts) opts)
          {:keys [::dgraph-opts
                  ::channels
                  ::system-tray?
                  ::port]
           {:keys [::app.spec/system-exit|]} ::channels} opts
          procs (atom [])
          procs-exit (fn []
                       (doseq [[exit| proc|] @procs]
                         (close! exit|))
                       (a/merge (mapv second @procs)))]
      (let [exit| (chan 1)
            proc|
            (go
              (loop []
                (when-let [[value port] (alts! [system-exit| exit|])]
                  (condp = port

                    exit|
                    (do nil)

                    system-exit|
                    (do
                      (let []
                        (println ::exit|)
                        (<! (unmount opts))
                        (println ::exiting)
                        (System/exit 0))))))
              (println ::go-block-exits))]
        (swap! procs conj [exit| proc|]))

      (swap! registry-ref assoc id (merge
                                    opts
                                    {::procs-exit procs-exit}))
      (<! (peer.reitit/start channels {::peer.reitit/port port}))
      (when system-tray?
        (peer.system-tray/mount {::peer.system-tray/quit| system-exit|}))
      #_(<! (peer.dgraph/ready?))
      (<! (peer.dgraph/upload-schema))

      (println ::mount-done))))

(defn -main [& args]
  (println ::-main)
  (a/<!! (mount peer1-preset)))
