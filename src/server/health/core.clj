(ns server.health.core
  (:require
    [mount.core :as mount]
    [server.health.dao.pg :refer [ds]]
    [server.health.api :refer [webserver]]
    [server.health.config :refer [ctx]]
    [server.health.dao.magrations :as migrations]
    [cambium.core :as log])
  (:gen-class))


(defn stop! []
  (let [_      (log/info {:msg "Stop hsapp"})
        _      (migrations/migrations-down)
        status (mount/stop #'webserver #'ds #'ctx)]
    (log/info {:status status})
    status))


(defn start! []
  (let [status (mount/start #'ctx #'ds #'webserver)
        _      (log/info {:msg "Start hsapp"})
        _      (.addShutdownHook (Runtime/getRuntime) (Thread. stop!))
        _      (migrations/migrations-up)
        _      (log/info {:status status})]
    status))


(defn -main []
  (start!))


(comment

  (-main)

  (defn restart []
    (stop #'webserver #'ds #'ctx)
    (start #'ctx #'ds #'webserver))



  )