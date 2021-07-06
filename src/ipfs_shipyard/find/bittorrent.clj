(ns ipfs-shipyard.find.bittorrent
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s])
  (:import
   (java.io ByteArrayOutputStream ByteArrayInputStream PushbackInputStream)
   (bt.metainfo TorrentId)))