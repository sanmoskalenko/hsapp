(ns ui.health.modals.core
  (:require
    [ui.health.modals.patient.new-patient.core :refer [new-patient]]
    [ui.health.modals.search.core :refer [search-patient]]
    [ui.health.modals.patient.core :refer [update-patient]]))


(defn modals []
  [:<>
   [new-patient]
   [search-patient]
   [update-patient]])
