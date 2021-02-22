(ns ipfscube.ui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as str]
   [cljs.reader :refer [read-string]]
   [clojure.spec.alpha :as s]
   [ipfscube.ui.render]

   [notepad.docker-from-browser1]))

(goog-define BAR_PORT 0)
(goog-define FOO_ORIGIN "")

#_(set! BAR_PORT (str (subs js/location.port 0 2) (subs (str BAR_PORT) 2)))
#_(set! FOO_ORIGIN "http://localhost:3001")

(def channels (let [ops| (chan 10)]
                {::ops| ops|}))

(def ctx {::state* (ipfscube.ui.render/create-state*
                    {::baz {}})})

(do (clojure.spec.alpha/check-asserts true))
(s/def ::foo keyword?)
(s/def ::bar string?)

(defn ^:export main
  []
  (println ::main)
  (let [{:keys [::ops|]} channels
        {:keys [::state*]} ctx]
    (println ::BAR_PORT BAR_PORT)
    (ipfscube.ui.render/render-ui channels state* {})
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (:op value)

              ::foo
              (let [{:keys []} value]
                (println ::bar))))

          (recur))))))

(do (main))