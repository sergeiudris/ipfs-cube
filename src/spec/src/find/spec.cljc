(ns find.spec
  #?(:cljs (:require-macros [find.spec]))
  (:require
   [clojure.spec.alpha :as s]))


(s/def ::username string?)
(s/def ::password string?)

(s/def ::user-info (s/keys :req [::username]))