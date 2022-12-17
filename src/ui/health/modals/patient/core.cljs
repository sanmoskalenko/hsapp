(ns ui.health.modals.patient.core
  (:require
    [ui.health.lin :refer [<sub >evt]]
    [ui.health.modals.patient.subs :as subs]
    [ui.health.events :as event]
    [syn-antd.form :as form]
    [syn-antd.button :as button]
    [syn-antd.modal :as modal]
    [ui.syn-antd-fixed.input :as input]
    [syn-antd.select :as select]))


(defn on-finish-handler
  [id data]
  (let [data*      (js->clj data :keywordize-keys true)
        id*        (js->clj id :keywordize-keys true)
        data-map   (assoc data* :patient/id id*)]
    (>evt [::event/update-patient data-map])
    (>evt [::event/update-patient-modal false])
    (>evt [::event/list-patient])))


(defn update-patient []
  (fn []
    (let [patient-id (<sub [::subs/id])]
      [:div.container
       [modal/modal {:title            "Update patient"
                     :style            {:top 50}
                     :visible          (<sub [::subs/update-patient-modal-visible?])
                     :destroy-on-close true
                     :width            "50%"
                     :footer           nil
                     :on-cancel        #(>evt [::event/update-patient-modal false nil])}

        [form/form {:layout    :vertical
                    :style     {:padding 50}
                    :name      :new-patient
                    :on-finish (partial on-finish-handler patient-id)}

         [form/form-item {:name          "patient/fname"
                          :initial-value (<sub [::subs/fname])
                          :has-feedback  true
                          :rules         [{:required true
                                           :message  "Input first patient name"}]}
          [input/input {:size        :middle
                        :placeholder "First name"}]]

         [form/form-item {:name          "patient/lname"
                          :initial-value (<sub [::subs/lname])
                          :has-feedback  true
                          :rules         [{:required true
                                           :message  "Input last patient name"}]}
          [input/input {:size        :middle
                        :placeholder "Last name"}]]

         [form/form-item {:name          "patient/mname"
                          :has-feedback  true
                          :initial-value (<sub [::subs/mname])
                          :rules         [{:required false
                                           :message  "Input last patient name"}]}
          [input/input {:size        :middle
                        :placeholder "Middle name"}]]

         [form/form-item {:name          "patient/insurance-policy"
                          :initial-value (<sub [::subs/insurance-policy])
                          :has-feedback  true
                          :rules         [{:required true
                                           :message  "Input insurance policy"}]}
          [input/input {:size        :middle
                        :placeholder "Insurance policy"}]]

         [form/form-item {:name          "patient/gender"
                          :initial-value (<sub [::subs/gender])
                          :has-feedback  true
                          :rules         [{:required true
                                           :message  "Choose patient gender"}]}
          [select/select {:placeholder "Gender"}
           (let [items ["MALE" "FEMALE" "NONE"]]
             (for [item items]
               ^{:key item} [select/select-option {:value item} item]))]]

         [form/form-item {:name          "patient/address"
                          :placeholder   "Patient address"
                          :initial-value (<sub [::subs/address])
                          :has-feedback  true
                          :rules         [{:required true
                                           :message  "Choose patient gender"}]}
          [input/input {:size        :middle
                        :placeholder "Patient address"}]]

         [form/form-item {:style {:margin-left "80%"}}
          [button/button {:type      :primary
                          :html-type :submit} "OK"]

          [button/button {:type      :default
                          :on-click  #(>evt [::event/update-patient-modal false nil])
                          :html-type :submit} "Cancel"]]]]])))

