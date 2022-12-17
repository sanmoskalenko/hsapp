(ns ui.health.modals.patient.new-patient.core
  (:require
    [ui.health.lin :refer [<sub >evt]]
    [ui.health.modals.patient.new-patient.subs :as subs]
    [ui.health.events :as event]
    [syn-antd.form :as form]
    [syn-antd.button :as button]
    [syn-antd.modal :as modal]
    [ui.syn-antd-fixed.input :as input]
    [syn-antd.date-picker :as date-picker]
    [syn-antd.select :as select]
    [ui.health.formats :as formatter]))


(defn- on-finish-handler
  [data]
  (let [data*           (js->clj data :keywordize-keys true)
        date-object     (:patient/birth-date data*)
        birth-date      (formatter/get-date-object date-object)
        prepared-data   (assoc data* :patient/birth-date birth-date)]
    (js/console.log :birth-date birth-date)
    (>evt [::event/create-patient prepared-data])
    (>evt [::event/new-patient-modal false])))



(defn new-patient []
  (fn []
    [:div.container
     [modal/modal {:title            "Сreate patient"
                   :style            {:top 50}
                   :visible          (<sub [::subs/new-patient-modal-visible?])
                   :destroy-on-close true
                   :width            "50%"
                   :footer           nil
                   :on-cancel        #(>evt [::event/new-patient-modal false])}

      [form/form {:layout    :vertical
                  :style     {:padding 50}
                  :name      :new-patient
                  :on-finish on-finish-handler}

       [form/form-item {:name         "patient/fname"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Input first patient name"}]}
        [input/input {:size        :middle
                      :placeholder "First name"}]]

       [form/form-item {:name         "patient/lname"
                        :placeholder  "last name"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Input last patient name"}]}
        [input/input {:size        :middle
                      :placeholder "Last name"}]]

       [form/form-item {:name         "patient/mname"
                        :has-feedback true
                        :rules        [{:required false
                                        :message  "Input last patient name"}]}
        [input/input {:size        :middle
                      :placeholder "Middle name"}]]

       [form/form-item {:name         "patient/insurance-policy"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Input insurance policy"}]}
        [input/input {:size        :middle
                      :placeholder "Insurance policy"}]]

       [form/form-item {:name         "patient/gender"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Choose patient gender"}]}
        [select/select {:size        :middle
                        :placeholder "Gender"}
         (let [items ["MALE" "FEMALE" "NONE"]]
           (for [item items]
             ^{:key item} [select/select-option {:value item} item]))]]

       [form/form-item {:name         "patient/address"
                        :placeholder  "Patient address"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Choose patient gender"}]}
        [input/input {:size        :middle
                      :placeholder "Patient address"}]]

       [form/form-item {:name         "patient/birth-date"
                        :has-feedback true
                        :rules        [{:required true
                                        :message  "Input birth date"}]}
        [date-picker/date-picker {:size        :middle
                                  :placeholder "Birth date"
                                  :style       {:width "100%"}}]]

       [form/form-item {:style {:margin-left "80%"}}
        [button/button {:type      :primary
                        :html-type :submit} "OK"]

        [button/button {:type      :default
                        :on-click  #(>evt [::event/new-patient-modal false])
                        :html-type :submit} "Cancel"]]]]]))
