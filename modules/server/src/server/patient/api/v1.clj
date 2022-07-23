(ns server.patient.api.v1
  (:require
    [server.patient.db :as db]
    [honeysql.core :as sql]
    [unifier.response :as r]
    [clojure.tools.logging :as log]))


(defn- empty-required-fields
  "Checks that all required fields are filled in
  Params:
   * `fields` – map with fields value"
  [value]
  (reduce-kv (fn [m k v] (if (nil? v)
                           (assoc m k v) m))
             {} value))


(defn- not-empty-fields
  "Discards empty fields
  Params:
   * `fields` – map with fields value"
  [value]
  (reduce-kv (fn [m k v] (if-not (nil? v)
                           (assoc m k v) m))
             {} value))


(defn get-patient
  "Performs a search based on the given parameters"
  [req]
  (let [req-data      (:body-params req)
        search-params (not-empty-fields req-data)
        patient       (db/get-patient search-params)]
    (log/info {:msg "Search patient"})
    (if-not (empty? patient)
      (r/as-success patient)
      (do (log/info {:msg    "Patient not found"
                     :params req-data})
          (r/as-not-found {:msg "Patient not found"})))))



(defn list-patients []
  "Returns a list of patients"
  (let [patients (db/list-all)]
    (if-not (empty? patients)
      (do (log/info {:msg "Return list patients"})
          (r/as-success patients))
      (do (log/warn {:msg "Patient not-found"})
          (r/as-not-found {:error/msg "Patients not-found"})))))


(defn create-patient
  "Creates a patient"
  [req]
  (let [req-data (:body-params req)]
    (log/info {:msg "Create patient"})
    (if-not (empty? (empty-required-fields req-data))
      (do (log/error {:msg "Required fields must not be empty"})
          (r/as-incorrect {:error/msg "Required fields must not be empty"}))
      (do (log/info {:msg "Patient created"})
          (r/as-success (db/create-patient req-data))))))


(defn update-patient
  [req]
  (let [req-data   (:body-params req)
        patient-id (:patient/id req-data)]
    (cond
      (nil? patient-id)
      (do
        (log/info {:msg "Impossible update patient, because id is null"})
        (r/as-incorrect {:error/msg "Impossible update patient, because id is null"}))

      (empty? (db/get-by-id patient-id))
      (do
        (log/info {:msg (format "Patient with id %s not found" patient-id)})
        (r/as-not-found {:error/msg (format "Patient with id %s not found" patient-id)}))

      (not (empty? (empty-required-fields req-data)))
      (do (log/error {:msg "Required fields must not be empty"})
          (r/as-incorrect {:error/msg "Required fields must not be empty"}))

      :else
      (let [values (-> req-data
                       (dissoc :patient/id :patient/birth-date)
                       (assoc :updated-at (sql/raw "current_timestamp")))
            res    (db/update-patient values patient-id)]
        (log/info {:msg "Patient updated"})
        (r/as-success res)))))


(defn delete-patient
  "Deleted policy owner by id"
  [req]
  (let [req-data   (:body-params req)
        patient-id (:patient/id req-data)]
    (cond
      (nil? patient-id)
      (do
        (log/error {:msg "ID must not be null"})
        (r/as-incorrect {:error/msg "ID must not be null"}))

      (empty? (db/get-by-id patient-id))
      (do
        (log/error {:msg (format "Patient with id %s not found" patient-id)})
        (r/as-not-found {:error/msg (format "Patient with id %s not found" patient-id)}))

      :else
      (let [_ (db/delete-patient patient-id)]
        (log/info {:msg (format "Patient with id %s deleted" patient-id)})
        (r/as-success {:msg (format "Patient with id %s deleted" patient-id)})))))

