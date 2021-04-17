(ns find.ui.spec
  #?(:cljs (:require-macros [find.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)