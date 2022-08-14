(ns server.health.dao.magrations
  (:require
    [server.health.dao.pg :refer [ds]]
    [server.health.config :refer [ctx]]
    [migratus.core :as migratus]
    [next.jdbc :as jdbc]))


(defn migrate-schema []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/init migrate-config)))


(defn migrate-table []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/migrate migrate-config)))


(defn get-migrations-list  []
  (mapv :schema_migrations/id
        (let [migrations-table (-> ctx :migrate :migration-table-name)]
          (jdbc/execute! ds [(format "select id from %s" migrations-table)]))))


(defn migrate-down []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)
        table-ids      (get-migrations-list)]
    (run! #(migratus/down migrate-config %) table-ids)))


(defn migrations-up []
  (migrate-schema)
  (migrate-table))

(defn migrations-down []
  (migrate-down))