(ns health.core
  (:require
    [mount.core :as mount]
    [health.dao.pg :refer [ds]]
    [health.api :refer [webserver]]
    [health.config :refer [ctx]]
    [health.dao.magrations :as migrations]
    [clojure.tools.logging :as log])
  (:gen-class))


(defn stop []
  (log/info :msg "Stop hsapp")
  (let [status (mount/stop #'webserver #'ds #'ctx)]
    (log/info {:status status})
    status))


(defn start []
  (log/info :msg "Start hsapp")
  (let [status (mount/start #'ctx #'ds #'webserver)
        _      (.addShutdownHook (Runtime/getRuntime) (Thread. stop))]
    (log/info :status status)
    status))


(defn restart []
  (stop)
  (start))

(defn -main []
  (start)
  (migrations/migrate-schema)
  (migrations/migrate-table))


(comment

  (-main)
  (restart)

  )