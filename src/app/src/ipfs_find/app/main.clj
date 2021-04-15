(ns ipfs-find.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [ipfs-find.app.spec :as app.spec]

   [ipfs-find.app.reitit :as app.reitit]
   [ipfs-find.app.dgraph :as app.dgraph]
   [ipfs-find.app.system-tray :as app.system-tray]
   [ipfs-find.app.ipfs :as app.ipfs]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [{:keys [::id] :as opts}]
  {::id id
   ::app.spec/state* (atom {})
   ::system-tray? (if (System/getenv "IPFSFIND_SYSTEM_TRAY")
                    (read-string (System/getenv "IPFSFIND_SYSTEM_TRAY"))
                    true)
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
      (<! (app.reitit/stop channels {::app.reitit/port port}))
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
      (<! (app.reitit/start channels {::app.reitit/port port}))
      (when system-tray?
        (app.system-tray/mount {::app.system-tray/quit| system-exit|}))
      #_(<! (app.dgraph/ready?))
      (<! (app.dgraph/upload-schema))

      (println ::mount-done))))

(defn -main [& args]
  (println ::-main)
  (a/<!! (mount peer1-preset)))
