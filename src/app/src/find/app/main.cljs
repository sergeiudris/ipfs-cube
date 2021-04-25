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
   [find.app.orbitdb :as app.orbitdb]
   [find.app.sqlitedb :as app.sqlitedb]
   
   [find.app.bittorrent :as app.bittorrent]))

(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))
(defonce async-exit-hook (js/require "async-exit-hook"))


(def FIND_PEER_INDEX (or (.. js/process -env -FIND_PEER_INDEX) 1))

(declare)

(defn stop
  []
  (go
    (<! (app.sqlitedb/stop {}))
    (println :exited-gracefully)))


(defn main [& args]
  (println ::main)
  (go
    (let [data-dir (.join path
                          js/__dirname
                          (format "../../volumes/peer%s" FIND_PEER_INDEX))]
      #_(<! (app.http/start))
      (app.electron/start {:on-close stop})
      (let [[sqlitedbA bittorrentA]
            (<! (a/map vector
                       [(app.sqlitedb/start {:peer-index FIND_PEER_INDEX
                                             :data-dir data-dir})
                        (app.bittorrent/start {:data-dir data-dir
                                               :peer-index FIND_PEER_INDEX})]))]
        (pipe (:torrent| @bittorrentA) (:torrent| @sqlitedbA)))

      #_(let [ipfsd (<! (app.ipfs/start {:data-dir data-dir
                                         :peer-index FIND_PEER_INDEX}))]
          (<! (app.orbitdb/start {:ipfsd ipfsd
                                  :peer-index FIND_PEER_INDEX
                                  :data-dir data-dir})))
      (async-exit-hook
       (fn [callback]
         (when (ifn? callback)
           (take! (stop)
                  (fn [_]
                    (callback)))))))))


(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))