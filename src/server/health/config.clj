(ns server.health.config
  (:require
    [clojure.java.io :as io]
    [aero.core :refer [read-config]]
    [mount.core :refer [defstate]]))

(defstate ctx :start
  (read-config (io/resource "config.edn")))
