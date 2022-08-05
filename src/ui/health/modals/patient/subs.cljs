(ns ui.health.modals.patient.subs
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  ::update-patient-modal-visible?
  (fn [db _]
    (:visible-update-patient db)))

(rf/reg-sub
  ::mname
  (fn [db _]
    (-> db :record :mname)))

(rf/reg-sub
  ::fname
  (fn [db _]
    (-> db :record :fname)))

(rf/reg-sub
  ::lname
  (fn [db _]
    (-> db :record :lname)))

(rf/reg-sub
  ::gender
  (fn [db _]
    (-> db :record :gender)))

(rf/reg-sub
  ::birth-date
  (fn [db _]
    (-> db :record :birth_date)))

(rf/reg-sub
  ::address
  (fn [db _]
    (-> db :record :address)))

(rf/reg-sub
  ::insurance-policy
  (fn [db _]
    (-> db :record :insurance_policy)))

(rf/reg-sub
  ::id
  (fn [db _]
    (-> db :record :id)))
