(ns ui.health.subs
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  ::patient
  (fn [db _]
    (:patient db)))

