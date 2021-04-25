(ns find.app.orbitdb
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
(defonce OrbitDB (js/require "orbit-db"))
(when-not (.-create IpfsHttpClient)
  (set! (.-create IpfsHttpClient) IpfsHttpClient))


(defn start
  [{:keys [:ipfsd
           :peer-index
           :data-dir] 
    :as opts}]
  (go
    (let [orbitdb (<p! (.createInstance OrbitDB
                                        (.-api ipfsd)
                                        (clj->js {"directory" (.join path
                                                                     data-dir
                                                                     "orbitdb")})))
          eventlog (<p! (.eventlog orbitdb
                                   "github.find/main-eventlog"
                                   (clj->js {"accessController"
                                             {"write" ["*"]}})))]
      #_(<p! (.drop eventlog))
      (<p! (.load eventlog))
      (let [entries (-> eventlog
                        (.iterator  #js {"limit" -1
                                         "reverse" true})
                        (.collect)
                        (vec))]
        (println ::count-entries (count entries)))

      (.. eventlog -events
          (on "replicated"
              (fn [address]
                (println ::repllicated))))

      (.. eventlog -events
          (on "replicate.progress"
              (fn [address hash entry progress have]
                (println ::replicate-progress)
                (println (read-string (.-value (.-payload entry))))
                #_(let [value (read-string (.-value (.-payload entry)))]
                    (put! ops| value)))))

      #_(let [peer-id (.-id (<p! (.. ipfsd -api (id))))
              counterV (volatile! (rand-int 100))]
          (go
            (loop []
              (<! (timeout 2000))
              (<p! (.add eventlog
                         (pr-str {:peer-id peer-id
                                  :counter @counterV})))
              (vswap! counterV inc)
              (recur))))

      #_(.on (.-events app-eventlog)
             "write"
             (fn [address entry heads])))))




