(ns ui.health.modals.search.core
  (:require
    [ui.health.lin :refer [<sub >evt]]
    [ui.health.modals.search.subs :as subs]
    [ui.health.events :as event]
    [syn-antd.form :as form]
    [syn-antd.button :as button]
    [syn-antd.modal :as modal]
    [ui.syn-antd-fixed.input :as input]
    [syn-antd.date-picker :as date-picker]
    [syn-antd.select :as select]
    [syn-antd.form :as form]))


(defn- not-empty-fields
  "Discards empty fields
  Params:
   * `fields` – map with fields value"
  [value]
  (reduce-kv (fn [m k v]
               (if-not (nil? v)
                 (assoc m k v) m))
             {} value))

(defn on-finish-handler
  [data]
  (let [data*        (js->clj data :keywordize-keys true)
        prepare-data (not-empty-fields data*)]
    (>evt [::event/search-patient prepare-data])
    (>evt [::event/search-patient-modal false])))


(defn search-patient []
  (fn []
    [:div.container
     [modal/modal {:title            "Search patient"
                   :style            {:top 50}
                   :visible          (<sub [::subs/search-patient-modal-visible?])
                   :destroy-on-close true
                   :width            "50%"
                   :footer           nil
                   :on-cancel        #(>evt [::event/search-patient-modal false])}

      [form/form {:layout    :vertical
                  :style     {:padding 50}
                  :name      :new-patient
                  :on-finish on-finish-handler}

       [form/form-item {:name         "patient/fname"
                        :has-feedback true
                        :rules        [{:message "Input first patient name"}]}
        [input/input {:size        :middle
                      :placeholder "First name"}]]

       [form/form-item {:name         "patient/lname"
                        :placeholder  "last name"
                        :has-feedback true
                        :rules        [{:message "Input last patient name"}]}
        [input/input {:size        :middle
                      :placeholder "First name"}]]

       [form/form-item {:name         "patient/mname"
                        :has-feedback true
                        :rules        [{:message "Input last patient name"}]}
        [input/input {:size        :middle
                      :placeholder "Middle name"}]]

       [form/form-item {:name         "patient/insurance-policy"
                        :has-feedback true
                        :rules        [{:message "Input insurance policy"}]}
        [input/input {:size        :middle
                      :placeholder "Insurance policy"}]]

       [form/form-item {:name         "patient/gender"
                        :has-feedback true
                        :rules        [{:message "Choose patient gender"}]}
        [select/select {:size        :middle
                        :placeholder "Gender"}
         (let [items ["MALE" "FEMALE" "NONE"]]
           (doall
             (for [item items]
               ^{:key item} [select/select-option {:value item} item])))]]

       [form/form-item {:name         "patient/address"
                        :placeholder  "Patient address"
                        :has-feedback true
                        :rules        [{:message "Choose patient gender"}]}
        [input/input {:size        :middle
                      :placeholder "Patient address"}]]

       [form/form-item {:name         "patient/birth-date"
                        :has-feedback true
                        :rules        [{:message "Select birth date"}]}
        [date-picker/date-picker {:size        :middle
                                  :placeholder "Birth date"
                                  :style       {:width "100%"}}]]

       [form/form-item {:style {:margin-left "80%"}}
        [button/button {:type      :primary
                        :html-type :submit} "OK"]

        [button/button {:type      :default
                        :on-click  #(>evt [::event/search-patient-modal false])
                        :html-type :submit} "Cancel"]]]]]))


