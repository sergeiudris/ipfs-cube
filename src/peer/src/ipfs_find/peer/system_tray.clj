(ns ipfs-find.peer.system-tray
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io])

  (:import
   java.awt.event.ActionListener
   java.io.IOException
   java.net.URL
   java.util.Random

   dorkbox.os.OS
   dorkbox.systemTray.Checkbox
   dorkbox.systemTray.Menu
   dorkbox.systemTray.MenuItem
   dorkbox.systemTray.Separator
   dorkbox.systemTray.SystemTray
   dorkbox.util.CacheUtil
   dorkbox.util.Desktop))

(defonce ^:private registry-ref (atom {}))

(defn mount
  [{:keys [::quit| ::id] :or {id "ipfs-find-system-tray"} :as opts}]
  (go
    (let [image (clojure.java.io/resource "logo/logo.png")
          _ (set! SystemTray/DEBUG true)
        ;; _ (CacheUtil/clear name)
          system-tray (SystemTray/get)
          _ (when (nil? system-tray)
              (throw (RuntimeException. "Unable to load SystemTray!")))
          _ (.setTooltip system-tray "Mail Checker")
          _ (.setImage system-tray image)
          _ (.setStatus system-tray "no mail")
          menu (.getMenu system-tray)
          foo-entry (MenuItem. "foo" (reify ActionListener
                                       (actionPerformed [_ event]
                                         (println ::foo))))
          _ (.add menu foo-entry)
          _ (.add menu (Separator.))
          bar-entry (MenuItem. "bar" (reify ActionListener
                                       (actionPerformed [_ event]
                                         (println ::bar))))
          _ (.add menu bar-entry)
          quit-entry (MenuItem. "quit" (reify ActionListener
                                         (actionPerformed
                                           [_ event]
                                           (println ::quit)
                                           (close! quit|))))
          _ (.add menu quit-entry)]
      (swap! registry-ref assoc id system-tray)
      #_(Desktop/browseURL "https://git.dorkbox.com/dorkbox/SystemTray")
      (println ::created))))

(defn unmount
  [{:keys [::id] :or {id "ipfs-find-system-tray"} :as opts}]
  (go
    (let [system-tray (get @registry-ref id)]
      (when system-tray
        (.shutdown system-tray) ;; does not work, needs look-into
        (swap! registry-ref dissoc id)))))
