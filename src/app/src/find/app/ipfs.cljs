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

(defonce fs (js/require "fs"))
(defonce fsExtra (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce IpfsHttpClient (js/require "ipfs-http-client"))
(when-not (.-create IpfsHttpClient)
  (set! (.-create IpfsHttpClient) IpfsHttpClient))
(defonce IpfsdCtl (js/require "ipfsd-ctl"))
(defonce GoIpfs (js/require "go-ipfs"))



(defn start
  []
  (go
    (let [config-dir (.join path
                            js/__dirname
                            (str "../../volumes/ipfs" (or (.. js/process -env -FIND_PEER_INDEX) 0)))
          _ (when-not (.existsSync fs config-dir)
              (.mkdirSync fs config-dir (clj->js {"recursive" true})))
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
      (<p! (->
            (.init ipfsd)
            (.catch (fn [error]
                      (println ::error-init error)))))
      (<p! (->
            (.start ipfsd)
            (.catch (fn [error]
                      (.removeSync fsExtra  (.join path (.-path ipfsd) "api"))))))
      (<p! (->
            (.start ipfsd)
            (.catch (fn [error]
                      (println ::error-start error)))))
      (println (<p! (.. ipfsd -api (id)))))))


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