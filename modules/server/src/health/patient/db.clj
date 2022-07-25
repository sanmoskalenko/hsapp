(ns health.patient.db
  (:require
    [honeysql.core :as sql]
    [honeysql.helpers :as hsql]
    [health.dao.pg :as db])
  (:import
    (java.util
      UUID)))


(defn- prepare-where-for-search-patient
  "Prepares the condition for filtering
   Params:
    * `value` - map with search data"
  [value]
  (let [params         (mapv (fn [[k v]] [:= k v]) value)
        prepared-where (into [:and] params)]
    prepared-where))


(defn list-all []
  (let [query (-> {:select [:*]
                   :from   [:health.patient]}
                  sql/format)]
    (db/execute! query)))


(defn get-patient
  [value]
  (let [where-params (prepare-where-for-search-patient value)
        init-query   {:select [:*]
                      :from   [:health.patient]}
        merge-query  (hsql/merge-where init-query where-params)
        query        (sql/format merge-query)]
    (db/execute! query)))


(defn get-by-id
  [id]
  (let [query (-> {:select [:*]
                   :from   [:health.patient]
                   :where  [:= :patient.id id]}
                  sql/format)]
    (db/execute-one! query)))


(defn create-patient
  [value]
  (let [owner-id (UUID/randomUUID)
        value*   (assoc value :patient/id owner-id)
        query    (-> {:insert-into :health.patient
                      :values      [value*]}
                     sql/format)]
    (db/execute-one! query)
    (get-by-id owner-id)))


(defn update-patient
  [value owner-id]
  (let [query (-> {:update :health.patient
                   :set    value
                   :where  [:= :id owner-id]}
                  sql/format)]
    (db/execute-one! query)
    (get-by-id owner-id)))


(defn delete-patient
  [owner-id]
  (let [query (-> {:delete-from :health.patient
                   :where       [:= :id owner-id]}
                  sql/format)]
    (db/execute-one! query)))