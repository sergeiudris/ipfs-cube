(ns ipfs-find.ui.spec
  #?(:cljs (:require-macros [ipfs-find.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)