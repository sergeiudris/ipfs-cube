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
(defonce path (js/require "path"))
(defonce IpfsHttpClient (js/require "ipfs-http-client"))
(defonce IpfsdCtl (js/require "ipfsd-ctl"))
(defonce GoIpfs (js/require "go-ipfs"))

(defn start
  []
  (go
    (let [ipfsd (<p! (->
                      (.createController IpfsdCtl
                                         (clj->js
                                          {"ipfsHttpModule" IpfsHttpClient
                                           "remote" false
                                           "disposable" false
                                           "test" false
                                           "ipfsBin" (.path GoIpfs)
                                           "args" ["--writable" "--enable-pubsub-experiment" "--migrate=true"]
                                           "ipfsOptions" {"repo" (.join path js/__dirname (str "../../volumes/ipfs" (or (.. js/process -env -FIND_PEER_INDEX) 0)))}}))
                      #_(.catch (fn [error]
                                  (println ::error error)))))
          _ (<p! (->
                  (.init ipfsd)
                  (.catch (fn [error]
                            (println ::error error)))) )
          id (<p! (.. ipfsd -api (id)))]
      (println id))))

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