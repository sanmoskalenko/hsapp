(ns server.dao.pg
  (:require
    [next.jdbc :as jdbc]
    [mount.core :refer [defstate]]
    [server.config :refer [ctx]]
    [clojure.tools.logging :as log]))


(defstate ^:dynamic ds
  :start (do (log/info :msg "Start health maindb")
             (jdbc/get-datasource (:main-db ctx)))
  :stop (constantly nil))


(defn execute!
  [query]
  (with-open [connection (jdbc/get-connection (:main-db ctx))]
    (jdbc/execute! connection query)))


(defn execute-one!
  [query]
  (with-open [connection (jdbc/get-connection ds)]
    (jdbc/execute! connection query)))