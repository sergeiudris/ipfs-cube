(ns ipfscube.ui.spec
  #?(:cljs (:require-macros [ipfscube.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::page-events keyword?)
(s/def ::page-game keyword?)
(s/def ::scenario-origin string?)
