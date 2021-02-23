(ns ipfscube.app.tray
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clj-systemtray.core :refer [popup-menu
                                menu-item
                                separator
                                menu
                                make-tray-icon!]]))


(def tray-menu
  (popup-menu
   (menu-item :title (fn [evt] (println ::foo)))
   (menu-item "another title" (fn [evt] (println ::foo)))
   (separator)
   (menu "more options"
         (menu-item "deep item 1" (fn [evt] (println ::foo)))
         (menu-item "deep item 2" (fn [evt] (println ::foo))))
   (menu-item :exit-title (fn [evt] (println ::exit)))))

(defn render
  []
   (make-tray-icon! "ipfs-cube.png" tray-menu))

