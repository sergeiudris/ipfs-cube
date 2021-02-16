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

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [ipfscube.ui.spec :as ui.spec]
   [ipfscube.ui.chan :as ui.chan]

   [ipfscube.daemon.spec :as daemon.spec]
   [ipfscube.daemon.chan :as daemon.chan]

   [ipfscube.ui.render :as ui.render]))

(goog-define BAR_PORT 0)

(goog-define FOO_ORIGIN "")

(set! BAR_PORT (str (subs js/location.port 0 2) (subs (str BAR_PORT) 2)))
(set! FOO_ORIGIN "http://localhost:3001")

(def channels (merge
               (daemon.chan/create-channels)
               (ui.chan/create-channels)))

(def ctx {::ui.spec/state* (ui.render/create-state*
                            {::daemon.spec/peer-metas {}})})

(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::ui.chan/ops|]} channels
        {:keys [::ui.spec/state*]} ctx]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::ui.chan/init
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::init)
                (ui.render/render-ui channels state* {})
                #_(daemon.chan/op
                   {::op.spec/op-key ::daemon.chan/request-state-update
                    ::op.spec/op-type ::op.spec/fire-and-forget}
                   channels
                   {}))

              {::op.spec/op-key ::ui.chan/update-state
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (swap! state* merge value))))

          (recur))))))

(def ops (create-proc-ops channels ctx {}))

(defn ^:export main
  []
  (println ::main)
  (println ::BAR_PORT BAR_PORT)
  (ui.chan/op
   {::op.spec/op-key ::ui.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))


(do (main))