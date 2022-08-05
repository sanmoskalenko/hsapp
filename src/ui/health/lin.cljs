(ns ui.health.lin
  (:require
    [re-frame.core :as rf]))

(def >evt rf/dispatch)
(def <sub (comp deref rf/subscribe))
(def sub rf/subscribe)
