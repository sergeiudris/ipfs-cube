(ns ipfscube.daemon.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]

  ;;  [cljctools.csp.op.spec :as op.spec]
  ;;  [cljctools.cljc.core :as cljc.core]

  ;;  [ipfscube.daemon.spec :as daemon.spec]
  ;;  [ipfscube.daemon.chan :as daemon.chan]
   )
  (:gen-class))

;; (def channels (merge
;;                (daemon.chan/create-channels)))

;; (def ctx {::daemon.spec/state* (atom {})})

;; (defn create-proc-ops
;;   [channels ctx]
;;   (let [{:keys [::daemon.chan/ops|]} channels]
;;     (go
;;       (loop []
;;         (when-let [[value port] (alts! [ops|])]
;;           (condp = port
;;             ops|
;;             (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

;;               {::op.spec/op-key ::daemon.chan/init
;;                ::op.spec/op-type ::op.spec/fire-and-forget}
;;               (let [{:keys []} value]
;;                 (println ::init)))))
;;         (recur)))))



;; (def _ (create-proc-ops channels {}))

(defn -main [& args]
  (println ::-main)
  ;; (daemon.chan/op
  ;;    {::op.spec/op-key ::daemon.chan/init
  ;;     ::op.spec/op-type ::op.spec/fire-and-forget}
  ;;    channels
  ;;    {})
  )