(ns server.health.config
  (:require
    [cprop.core :refer [load-config]]
    [mount.core :refer [defstate]]))

(defstate ctx :start
  (load-config))
