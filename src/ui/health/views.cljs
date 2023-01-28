(ns ui.health.views
  (:require
    [reagent.core :as r]
    [ui.health.lin :refer [<sub >evt]]
    [ui.health.events :as events]
    [ui.health.subs :as subs]
    [syn-antd.button :as button]
    [syn-antd.layout :as layout]
    [syn-antd.menu :as menu]
    [syn-antd.table :as table]
    [syn-antd.icons.delete-outlined :as delete-outlined]
    [syn-antd.icons.edit-outlined :as edit-outlined]
    [ui.health.modals.core :refer [modals]]
    [syn-antd.form :as form]))


(defn add-actions-column
  [columns]
  (conj columns
        {:title "Actions"
         :render
         (fn [_ record]
           (let [record (js->clj record :keywordize-keys true)
                 {patient-id :id} record]
             (r/as-element
               [form/form

                [button/button {:icon     (r/as-element [edit-outlined/edit-outlined])
                                :type     :default
                                :style    {:margin 5}
                                :size     :middle
                                :on-click (fn []
                                            (>evt [::events/update-patient-modal true record]))}]

                [button/button {:icon     (r/as-element [delete-outlined/delete-outlined])
                                :type     :danger
                                :size     :middle
                                :style    {:margin 5}
                                :on-click (fn []
                                            (>evt [::events/delete-patient patient-id]))}]])))}))


(def columns [{:title "Insurance Policy" :dataIndex "insurance_policy"}
              {:title "First Name" :dataIndex "fname"}
              {:title "Middle Name" :dataIndex "mname"}
              {:title "Last Name" :dataIndex "lname"}
              {:title "Gender" :dataIndex "gender"}
              {:title "Birth Date" :dataIndex "birth_date"}
              {:title "Address" :dataIndex "address"}])


(defn footer []
  [:footer
   [:div.container
    [:span
     [:p "test app on Clojure"]]]])


(defn on-row-handler
  [record _row_index]
  (clj->js
    {:onDoubleClick
     (fn []
       (let [patient (js->clj record :keywordize-keys true)]
         (>evt [::events/update-patient-modal true patient])))}))


(defn patient-table []
  (fn []
    (let [patients (<sub [::subs/patient])]
      [:div
       [:h2 {:style {:margin-left 10}} "Patient"]
       [table/table
        {:columns    (add-actions-column columns)
         :dataSource patients
         :pagination false
         :onRow      on-row-handler
         :rowKey     "id"}]])))


(defn menu []
  [menu/menu {:theme "dark"
              :mode  "horizontal"}
   [menu/menu-item {:icon     "SHOW ALL"
                    :on-click #(>evt [::events/list-patient])}]

   [menu/menu-item {:icon     "CREATE"
                    :on-click #(>evt [::events/new-patient-modal true])}]

   [menu/menu-item {:icon     "SEARCH"
                    :on-click #(>evt [::events/search-patient-modal true])}]])


(defn main-panel []
  [layout/layout {:class "layout"}
   [layout/layout-header
    [menu]]

   [layout/layout-content
    [layout/layout-content {:style {:background-color "#ffffff"
                                    :margin           25}}
     [patient-table]
     [modals]]]

   [layout/layout
    [layout/layout-footer
     [footer]]]])


