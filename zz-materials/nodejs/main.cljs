(ns torrent-search.app.main
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
   [torrent-search.app.http :as app.http]
   [torrent-search.app.ipfs :as app.ipfs]
   [torrent-search.app.electron :as app.electron]
   [torrent-search.app.sqlitedb :as app.sqlitedb]
   [torrent-search.bittorrent.crawl :as bittorrent.crawl]
   [cljctools.peerdb.core :as peerdb.core]))

(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))
(defonce async-exit-hook (js/require "async-exit-hook"))


(def TORRENT_SEARCH_PEER_INDEX (or (.. js/process -env -TORRENT_SEARCH_PEER_INDEX) 1))

(declare)

(defn stop
  []
  (go
    
    (println :exited-gracefully)))


(defn main [& args]
  (println ::main)
  (go
    (let [data-dir (.join path
                          js/__dirname
                          (format "../../volumes/peer%s" TORRENT_SEARCH_PEER_INDEX))]
      #_(<! (app.http/start))
      #_(app.electron/start {:on-close stop})
      #_(let [[peerdbA bittorrentA]
              (<! (a/map vector
                         [#_(bittorrent.crawl/start {:data-dir data-dir
                                                     :peer-index TORRENT_SEARCH_PEER_INDEX})]))]
          #_(pipe (:torrent| @bittorrentA) (:torrent| @peerdbA)))

      #_(let [ipfsd (<! (app.ipfs/start {:peer-index TORRENT_SEARCH_PEER_INDEX
                                         :data-dir data-dir}))])
      (<! (app.sqlitedb/start {:data-dir data-dir}))
      (doto js/process
        (.on "unhandledRejection"
             (fn [reason promise]
               (println "unhandledRejection:")
               (js/console.log reason)
               (.exit js/process 1)))

        (.on "uncaughtException"
             (fn [error]
               (println "uncaughtException:")
               (js/console.log error)
               (.exit js/process 1))))

      (async-exit-hook
       (fn [callback]
         (when (ifn? callback)
           (take! (stop)
                  (fn [_]
                    (callback)))))))))


(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))