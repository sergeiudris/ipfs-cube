(ns find.app.main
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
   [cljs.reader :refer [read-string]]
   [find.app.http :as app.http]
   [find.app.ipfs :as app.ipfs]
   [find.app.electron :as app.electron]
   [find.app.orbitdb :as app.orbitdb]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))

(def FIND_PEER_INDEX (or (.. js/process -env -FIND_PEER_INDEX) 1))

(declare)

(defn main [& args]
  (println ::main)
  (go
    (<! (app.http/start))
    (<! (app.electron/start))
    (let [ipfsd (<! (app.ipfs/start {:peer-index FIND_PEER_INDEX}))]
      (<! (app.orbitdb/start {:ipfsd ipfsd
                              :peer-index FIND_PEER_INDEX})))))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))