(ns server.health.dao.pg
  (:require
    [next.jdbc :as jdbc]
    [mount.core :refer [defstate]]
    [server.health.config :refer [ctx]]
    [cambium.core :as log]))


(defstate ds
  :start (do (log/info {:msg "Start health maindb"})
             (jdbc/get-datasource (:main-db ctx)))
  :stop (constantly nil))


(defn execute!
  [ds query]
  (with-open [connection (jdbc/get-connection ds)]
    (jdbc/execute! connection query)))

(defn execute-one!
  [ds query]
  (with-open [connection (jdbc/get-connection ds)]
    (jdbc/execute! connection query)))
