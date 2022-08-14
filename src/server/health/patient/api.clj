(ns server.health.patient.api
  (:require
    [server.health.patient.api.v1 :as v1]))


(defn list-patient
  [ds]
  (v1/list-patients ds))

(defn search-patient
  [ds req]
  (v1/get-patient ds req))

(defn create-patient
  [ds req]
  (v1/create-patient ds req))

(defn update-patient
  [ds req]
  (v1/update-patient ds req))

(defn delete-patient
  [ds req]
  (v1/delete-patient ds req))





