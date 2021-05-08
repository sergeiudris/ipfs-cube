(ns find.app.ipfs
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
(defonce IpfsHttpClient (js/require "ipfs-http-client"))
(when-not (.-create IpfsHttpClient)
  (set! (.-create IpfsHttpClient) IpfsHttpClient))
(defonce IpfsdCtl (js/require "ipfsd-ctl"))
(defonce GoIpfs (js/require "go-ipfs"))

(defn start
  [{:keys [peer-index
           :data-dir] :as opts}]
  (go
    (let [config-dir (.join path data-dir "ipfs")
          ipfsd (<p! (->
                      (.createController IpfsdCtl
                                         (clj->js
                                          {"ipfsHttpModule" IpfsHttpClient
                                           "remote" false
                                           "disposable" false
                                           "test" false
                                           "ipfsBin" (.path GoIpfs)
                                           "args" ["--writable" "--enable-pubsub-experiment" "--migrate=true"]
                                           "ipfsOptions" {"repo" config-dir}}))
                      #_(.catch (fn [error]
                                  (println ::error error)))))]

      (.ensureDirSync fs config-dir)

      (<p! (->
            (.init ipfsd)
            (.catch (fn [error]
                      (println ::error-init error)))))

      (let [config-filepath (.join path config-dir "config")
            config (js->clj (.readJsonSync fs config-filepath))
            swarm-port (+ 4000 peer-index)
            config (-> config
                       (assoc-in ["Swarm" "ConnMgr" "LowWater"] 50)
                       (assoc-in ["Swarm" "ConnMgr" "HighWater"] 300)
                       (assoc-in ["Swarm" "ConnMgr" "GracePeriod"] "300s")
                       (assoc-in ["Addresses" "Swarm"] [(format "/ip4/0.0.0.0/tcp/%s" swarm-port)
                                                        (format "/ip6/::/tcp/%s" swarm-port)
                                                        (format "/ip4/0.0.0.0/udp/%s/quic" swarm-port)
                                                        (format "/ip6/::/udp/%s/quic" swarm-port)])
                       (assoc-in ["Discovery" "MDNS" "Enabled"] true))]
        (.writeJsonSync fs config-filepath (clj->js config) (clj->js {"spaces" 2})))

      (do
        (<p! (->
              (.start ipfsd)
              (.catch (fn [error]
                        (.removeSync fs (.join path (.-path ipfsd) "api"))))))
        (<p! (->
              (.start ipfsd)
              (.catch (fn [error]
                        (println ::error-start error)))))
        (println ::peer-id (.-id (<p! (.. ipfsd -api (id))))))

      #_(let [id (.-id (<p! (.. ipfsd -api (id))))
              encoder (js/TextEncoder.)
              decoder (js/TextDecoder.)
              counterV (volatile! (rand-int 100))]
          (.. ipfsd -api -pubsub
              (subscribe
               "github-foo-find-ping"
               (fn [msg]
                 (when-not (= id (. msg -from))
                   (do
                     #_(println (format "id: %s" id))
                     (println (format "from: %s" (. msg -from)))
                     (println (format "data: %s" (.decode decoder (. msg -data))))
                     #_(println (format "topicIDs: %s" msg.topicIDs)))
                   #_(put! pubsub| msg)))))
          (go (loop []
                (<! (timeout (* 2000 (+ 1 (rand-int 2)))))
                (vswap! counterV inc)
                (.. ipfsd -api -pubsub
                    (publish
                     "github-foo-find-ping"
                     (.encode encoder (str {::count @counterV}))))
                (recur))))
      ipfsd)))



(comment

  (js/Object.keys ipfs)
  (js/Object.keys ipfs.pubsub)

  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  ;;
  )