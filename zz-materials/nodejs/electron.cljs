(ns torrent-search.app.electron
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
(defonce Electron (js/require "electron"))
(def ElectronApp (.-app Electron))
(def ElectronBrowserWindow (.-BrowserWindow Electron))

(declare)

(defn start
  [{:keys [:on-close]}]
  (go
    (let [_ (<p! (.whenReady ElectronApp))
          create-window
          (fn []
            (let [main-window (ElectronBrowserWindow.
                               (clj->js {"width" 800
                                         "height" 600
                                         "icon" (.join path
                                                       js/__dirname
                                                       "../"
                                                       "logo"
                                                       "logo.png")
                                         "webPreferences" {}}))]
              (.loadFile main-window (.join path js/__dirname "../public/index.html"))))]
      (create-window)
      (.on ElectronApp "activate"
           (fn []
             (when (empty? (.getAllWindows ElectronBrowserWindow))
               (create-window))))
      (.on ElectronApp "window-all-closed"
           (fn []
             (go
               (when (not= js/global.process.platform "darwin")
                 (<! (on-close))
                 #_(.exit ElectronApp 0)
                 (.quit ElectronApp))))))))