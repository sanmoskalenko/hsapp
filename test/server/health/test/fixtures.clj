(ns server.health.test.fixtures
  (:require
    [cprop.core :refer [load-config]]
    [mount.core :refer [defstate]]
    [next.jdbc :as jdbc]
    [migratus.core :as migratus]
    [cambium.core :as log]
    [mount.core :as mount]))


(defstate ctx :start
  (load-config :resource "resources/test_config.edn"))


(defstate ds
  :start (jdbc/get-datasource (:main-db ctx))
  :stop (constantly nil))


(defn get-migrations-list
  []
  (mapv :schema_migrations/id
        (let [migrations-table (-> ctx :migrate :migration-table-name)]
          (jdbc/execute! ds [(format "select id from %s" migrations-table)]))))


(defn migrate-schema []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/init migrate-config)))


(defn migrate-table
  []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)]
    (migratus/migrate migrate-config)))


(defn migrate-down []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)
        table-ids      (get-migrations-list)]
    (run! #(migratus/down migrate-config %) table-ids)))


(defn migrations-up  []
  (migrate-schema)
  (migrate-table))


(defn migrations-down []
  (migrate-down))


(defn stop! []
  (let [_      (log/info {:msg "Stop test-hsapp"})
        _      (migrations-down)
        status (mount/stop #'ds #'ctx)]
    (log/info {:status status})
    status))


(defn start! []
  (let [status (mount/start #'ctx #'ds)
        _      (log/info {:msg "Start test-hsapp"})
        _      (.addShutdownHook (Runtime/getRuntime) (Thread. stop!))
        _      (migrations-up)
        _      (log/info {:status status})]
    status))


(defn system-fixtures
  [f]
  (start!)
  (try
    (f)
    (catch Exception e
      (throw e))
    (finally
      (stop!))))
