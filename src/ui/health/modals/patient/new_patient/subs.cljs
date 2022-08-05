(ns ui.health.modals.patient.new-patient.subs
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  ::new-patient-modal-visible?
  (fn [db _]
    (:visible-new-patient db)))