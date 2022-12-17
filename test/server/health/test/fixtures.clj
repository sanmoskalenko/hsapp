(ns server.health.test.fixtures
  (:require
    [cambium.core :as log]
    [clojure.java.io :as io]
    [aero.core :refer [read-config]]
    [migratus.core :as migratus]
    [mount.core :refer [defstate]]
    [mount.core :as mount]
    [next.jdbc :as jdbc]))


(defstate ctx :start
  (read-config (io/file "test/resources/test_config.edn")))


(defstate ds
  :start (jdbc/get-datasource (:main-db ctx))
  :stop (constantly nil))


(defn get-table-migrations-list []
  (mapv :schema_migrations/id
        (let [migrations-table (-> ctx :migrate :migration-table-name)]
          (jdbc/execute! ds [(format "select id from %s" migrations-table)]))))


(defn get-data-migrations-list []
  (mapv :schema_data_migrations/id
        (let [migrations-table (-> ctx :populate :migration-table-name)]
          (jdbc/execute! ds [(format "select id from %s" migrations-table)]))))


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


(defn migrate-data []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:populate :db :connection] connection)
        migrate-config (:populate db-config)]
    (migratus/migrate migrate-config)))


(defn migrate-table-down []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:migrate :db :connection] connection)
        migrate-config (:migrate db-config)
        table-ids      (get-table-migrations-list)]
    (apply migratus/down (concat [migrate-config] table-ids))))


(defn migrate-data-down []
  (let [connection     (jdbc/get-connection ds)
        db-config      (assoc-in ctx [:populate :db :connection] connection)
        migrate-config (:populate db-config)
        table-ids      (get-data-migrations-list)]
    (apply migratus/down (concat [migrate-config] table-ids))))


(defn migrations-up []
  (migrate-schema)
  (migrate-table)
  (migrate-data))


(defn migrations-down []
  (migrate-data-down)
  (migrate-table-down))


(defn stop! []
  (let [_      (log/info {:msg "Stop test-hsapp"})
        _      (migrations-down)
        status (mount/stop #'ctx #'ds)]
    (log/info {:status status})
    status))


(defn start! []
  (let [_      (log/info {:msg "Start test-hsapp"})
        status (mount/start #'ctx #'ds)
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
