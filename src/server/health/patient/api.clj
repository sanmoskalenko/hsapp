(ns server.health.patient.api
  (:require
    [server.health.patient.api.v1 :as v1]))


(defn list-patient []
  (v1/list-patients))

(defn search-patient
  [req]
  (v1/get-patient req))

(defn create-patient
  [req]
  (v1/create-patient req))

(defn update-patient
  [req]
  (v1/update-patient req))

(defn delete-patient
  [req]
  (v1/delete-patient req))




