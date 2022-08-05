(ns ui.health.modals.search.subs
  (:require
    [re-frame.core :as rf]))

(enable-console-print!)

(rf/reg-sub
  ::search-patient-modal-visible?
  (fn [db _]
    (:visible-search-patient db)))
