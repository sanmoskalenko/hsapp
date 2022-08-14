(ns server.health.patient.db
  (:require
    [honeysql.core :as sql]
    [honeysql.helpers :as hsql]
    [server.health.dao.pg :as db])
  (:import
    (java.util
      UUID)))

(defn- prepare-where-for-search-patient
  "Prepares the condition for filtering
   Params:
    * `value` - map with search data"
  [value]
  (if (nil? value)
    nil
    (let [params         (mapv (fn [[k v]] [:= k v]) value)
          prepared-where (into [:and] params)]
      prepared-where)))


(defn list-all
  [ds]
  (let [query (-> {:select [:*]
                   :from   [:health.patient]}
                  sql/format)]
    (db/execute! ds query)))


(defn get-patient
  [ds value]
  (let [where-params (prepare-where-for-search-patient value)
        init-query   {:select [:*]
                      :from   [:health.patient]}
        merge-query  (hsql/merge-where init-query where-params)
        query        (sql/format merge-query)]
    (db/execute! ds query)))


(defn get-by-id
  [ds id]
  (let [query (-> {:select [:*]
                   :from   [:health.patient]
                   :where  [:= :id id]}
                  sql/format)]
    (db/execute-one! ds query)))


(defn create-patient
  [ds value]
  (let [patient-id (UUID/randomUUID)
        value*     (assoc value :patient/id patient-id)
        query      (-> {:insert-into :health.patient
                        :values      [value*]}
                       sql/format)]
    (db/execute-one! ds query)
    (get-by-id ds patient-id)))


(defn update-patient
  [ds value patient-id]
  (let [query (-> {:update :health.patient
                   :set    value
                   :where  [:= :id patient-id]}
                  sql/format)]
    (db/execute! ds query)
    (get-by-id ds patient-id)))


(defn delete-patient
  [ds patient-id]
  (let [query (-> {:delete-from :health.patient
                   :where       [:= :id patient-id]}
                  sql/format)]
    (db/execute-one! ds query)))