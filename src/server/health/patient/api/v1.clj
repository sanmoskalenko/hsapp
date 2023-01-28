(ns server.health.patient.api.v1
  (:require
    [server.health.patient.db :as db]
    [honeysql.core :as sql]
    [clojure.walk :as walk]
    [unifier.response :as r]
    [cambium.core :as log]
    [clojure.instant :as instant]))


(defn- empty-required-fields
  "Checks that all required fields are filled in
  Params:
   * `fields` – map with fields value"
  [value]
  (reduce-kv (fn [m k v] (if (nil? v)
                           (assoc m k v) m))
             {} value))


(defn str->inst
  "Transform date with string to inst"
  [date]
  (instant/read-instant-timestamp date))


(defn normalize-params
  "Transform map keys from strings to keywords.
   Params:
     * `params` – map with params"
  [params]
  (walk/keywordize-keys params))


(defn get-patient
  "Performs a search based on the given parameters"
  [ds req]
  (let [req-data      (:params req)
        search-params (normalize-params req-data)
        patient       (db/get-patient ds search-params)]
    (log/info {:msg "Search patient"})
    (if-not (empty? patient)
      (r/as-success patient)
      (do (log/info {:msg    "Patient not found"
                     :params req-data})
          (r/as-not-found {:msg "Patient not found"})))))



(defn list-patients
  "Returns a list of patients"
  [ds]
  (let [patients (db/list-all ds)]
    (if (empty? patients)
      (do (log/warn {:msg "Patient not-found"})
          (r/as-not-found {:error/msg "Patients not-found"}))
      (do (log/info {:msg "Return list patients"})
          (r/as-success patients)))))


(defn create-patient
  "Creates a patient"
  [ds req]
  (let [req-data             (:body-params req)
        birth-date           (:birth-date req-data)
        normalize-birth-date (str->inst birth-date)
        data                 (assoc req-data :birth-date normalize-birth-date)]
    (try
      (log/info {:msg "Create patient"})
      (if-not (empty? (empty-required-fields data))
        (do (log/error {:msg "Required fields must not be empty"})
            (r/as-incorrect {:error/msg "Required fields must not be empty"}))
        (do (log/info {:msg "Patient created"})
            (r/as-success (db/create-patient ds data))))
      (catch Exception e
        (log/error {:msg    "User creation errors!"
                    :params req-data
                    :ex-msg (ex-message e)})
        (r/as-error {:error/msg "User creation errors, check server logs"})))))


(defn update-patient
  [ds req]
  (let [req-data   (:body-params req)
        patient-id (parse-uuid (or (:id req-data) ""))]
    (cond
      (nil? patient-id)
      (do
        (log/info {:msg "Impossible update patient, because id is null"})
        (r/as-incorrect {:error/msg "Impossible update patient, because id is null"}))

      (empty? (db/get-by-id ds patient-id))
      (do
        (log/info {:msg (format "Patient with id %s not found" patient-id)})
        (r/as-not-found {:error/msg (format "Patient with id %s not found" patient-id)}))

      (seq (empty-required-fields req-data))
      (do (log/error {:msg "Required fields must not be empty"})
          (r/as-incorrect {:error/msg "Required fields must not be empty"}))

      :else
      (let [values (-> req-data
                       (dissoc :id)
                       (dissoc :created_at)
                       (assoc :updated-at (sql/raw "current_timestamp")))
            res    (db/update-patient ds values patient-id)]
        (log/info {:msg "Patient updated"})
        (r/as-success res)))))


(defn delete-patient
  "Deleted patient by id"
  [ds req]
  (let [req-data   (:body-params req)
        patient-id (parse-uuid (or req-data ""))]
    (cond
      (nil? patient-id)
      (do
        (log/error {:msg "ID must not be null"})
        (r/as-incorrect {:error/msg "ID must not be null"}))

      (empty? (db/get-by-id ds  patient-id))
      (do
        (log/error {:msg (format "Patient with id %s not found" patient-id)})
        (r/as-not-found {:error/msg (format "Patient with id %s not found" patient-id)}))

      :else
      (let [_ (db/delete-patient ds patient-id)]
        (log/info {:msg (format "Patient with id %s deleted" patient-id)})
        (r/as-success {:msg (format "Patient with id %s deleted" patient-id)})))))

