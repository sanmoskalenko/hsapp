(ns ui.health.formats
  (:require
    ["dayjs" :as dayjs]))


(defn get-date-object
  [t]
  (-> t
      dayjs
      (.format "YYYY-MM-DDTHH:mm:ssZ")))
