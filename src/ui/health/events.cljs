(ns ui.health.events
  (:require
    [re-frame.core :as rf]
    [ui.health.db :as db]
    [day8.re-frame.http-fx]
    [ajax.core :as ajax]))



(rf/reg-event-db
  ::initialize-db
  (fn [_ _] db/db))


;;;;;;
;; MODALS
;;;;;;


(rf/reg-event-db
  ::new-patient-modal
  (fn [db [_ action]]
    (assoc db :visible-new-patient action)))

(rf/reg-event-db
  ::search-patient-modal
  (fn [db [_ action]]
    (assoc db :visible-search-patient action)))


(rf/reg-event-db
  ::update-patient-modal
  (fn [db [_ action patient]]
    (assoc db :visible-update-patient action
              :record patient)))


;;;;;;
;; LIST PATIENT
;;;;;;


(rf/reg-event-db
  ::list-patient-success
  (fn [db [_ data]]
    (assoc db :patient data)))


(rf/reg-event-fx
  ::list-patient-failure
  (fn [_ [_ data]]
    (js/console.log :error/data data)))


(rf/reg-event-fx
  ::list-patient
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/api/patient"
                  :timeout         8000
                  :format          (ajax/json-request-format {:keywords? true})
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-failure      [::list-patient-failure]
                  :on-success      [::list-patient-success]}}))


;;;;;;
;; DELETE PATIENT
;;;;;;


(rf/reg-event-fx
  ::delete-patient-success
  (fn [_ _]
    {:dispatch [::list-patient]}))


(rf/reg-event-db
  ::delete-patient-failure
  (fn [_ [_ data]]
    (js/console.log :data data)))


(rf/reg-event-fx
  ::delete-patient
  (fn [_ [_ data]]
    {:http-xhrio {:method          :delete
                  :uri             "/api/patient"
                  :params          data
                  :timeout         8000
                  :format          (ajax/json-request-format {:keywords? true})
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-failure      [::delete-patient-failure]
                  :on-success      [::delete-patient-success]}}))

;;;;;;
;; CREATE PATIENT
;;;;;;


(rf/reg-event-fx
  ::create-patient-success
  (fn [_ _]
    {:dispatch [::list-patient]}))


(rf/reg-event-fx
  ::create-patient-failure
  (fn [_ [_ data]]
    (js/console.log :error/data data)))


(rf/reg-event-fx
  ::create-patient
  (fn [_ [_ data]]
    (js/console.log :data data)
    {:http-xhrio {:method          :post
                  :uri             "/api/patient"
                  :params          data
                  :timeout         8000
                  :format          (ajax/json-request-format {:keywords? true})
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::create-patient-success data]
                  :on-failure      [::create-patient-failure data]}}))

;;;;;;
;; UPDATE PATIENT
;;;;;;


(rf/reg-event-fx
  ::update-patient-success
  (fn [_ [_ _]]
    {:dispatch [::list-patient]}))


(rf/reg-event-db
  ::update-patient-failure
  (fn [_ [_ data]]
    (js/console.log :data {:patient data})))

(rf/reg-event-fx
  ::update-patient
  (fn [_ [_ data]]
    {:http-xhrio {:method          :put
                  :uri             "/api/patient"
                  :params          data
                  :timeout         8000
                  :format          (ajax/json-request-format {:keywords? true})
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::update-patient-success]
                  :on-failure      [::update-patient-failure]}}))

;;;;;;
;; SEARCH PATIENT
;;;;;;


(rf/reg-event-db
  ::search-patient-success
  (fn [db [_ data]]
    (assoc db :patient data)))


(rf/reg-event-fx
  ::search-patient-failure
  (fn [_ [_ data]]
    (js/console.log "Patient not found"
                    :error/data data)))

(rf/reg-event-fx
  ::search-patient
  (fn [_ [_ data]]
    {:http-xhrio {:method          :get
                  :uri             "/api/search"
                  :params          data
                  :timeout         8000
                  :format          (ajax/json-request-format {:keywords? true})
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::search-patient-success]
                  :on-failure      [::search-patient-failure]}}))


