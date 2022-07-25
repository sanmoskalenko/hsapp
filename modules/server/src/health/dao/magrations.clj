(ns health.dao.magrations
  (:require
    [health.dao.pg :refer [ds]]
    [health.config :refer [ctx]]
    [migratus.core :as migratus]
    [next.jdbc :as jdbc]))


;; TODO does it make sense to write a migration layer for one table??
(defn migrate-schema []
  (let [connection     (jdbc/get-connection (:main-db ctx))
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/init migrate-config)))

(defn migrate-table []
  (let [connection     (jdbc/get-connection (:main-db ctx))
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/migrate migrate-config)))



