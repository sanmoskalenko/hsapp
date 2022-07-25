(ns health.api
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.util.response :refer [redirect response]]
    [ring.util.http-response :as response]
    [health.config :refer [ctx]]
    [mount.core :refer [defstate]]
    [muuntaja.middleware :as middleware]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [unifier.response :as r]
    [health.patient.api :as api]
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]))

(parser/set-resource-path! (clojure.java.io/resource "public"))

(defn home-page []
  (parser/render-file "index.html" {}))


(defn error-page [request]
  (let [error-details {:status (:status request)
                       :header {"Content-Type" "text/html; charset=utf-8"}}]
    (parser/render-file "error.html" error-details)))


(defn create-patient
  "Create patient request handler"
  [request]
  (log/info {:msg    "Receive request to add new order"
             :params (:body-params request)})
  (let [patient (api/create-patient request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/bad-request))))


(defn list-patient []
  "Request handler for getting a list of patient"
  (log/info {:msg "Receive request to list orders"})
  (let [patient (api/list-patient)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn search-patient
  "Request handler for search patient"
  [request]
  (log/info {:msg "Receive request to search patient"})
  (let [patient (api/search-patient request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn update-patient
  "Request handler for update of patient"
  [request]
  (log/info {:msg "Receive request to update patient"})
  (let [patient (api/update-patient request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn delete-patient
  "Request handler for delete patient"
  [request]
  (log/info {:msg "Receive request to delete patient"})
  (let [res (api/delete-patient request)]
    (if (r/success? res)
      (-> res r/get-data response/ok)
      (-> res r/get-data response/not-found))))


(defroutes app-routes
  (ANY "/" [] (redirect "/hsapp"))
  (GET "/hsapp" [] (home-page))
  (context "/api" []
   (GET "/patients" [] (list-patient))
   (GET "/search" request (search-patient request))
   (POST "/patient" request (create-patient request))
   (PUT "/patient" request (update-patient request))
   (DELETE "/patient" request (delete-patient request)))
  (route/not-found (error-page (response/not-found))))


(def app
  (-> app-routes
      middleware/wrap-format
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post])))


(defstate webserver
  :start (do
           (log/info {:msg "Start webserver!"})
           (run-jetty #'app (:webserver ctx)))
  :stop (do
          (log/info {:msg "Stop webserver"})
          (.stop webserver)))

(comment
  (require '[java-time :as jt])

  (app-routes {:uri            "/api/patients"
               :request-method :get})

  (app-routes {:uri            "/api/patient"
               :request-method :put
               :body-params    {:patient/id     #uuid"f5e8f146-ccfd-4750-866a-0620baec9774",
                                :patient/lname  "LNAME1",
                                :patient/gender "FEMALE",}})

  (app-routes {:uri            "/api/search"
               :request-method :get
               :body-params    {:patient/lname  "LNAME1"
                                :patient/gender "MALE",}})

  (app-routes {:uri            "/api/patient"
               :request-method :post
               :body-params    {:patient/fname            "FNAME"
                                :patient/mname            "MNAME"
                                :patient/lname            "LNAME"
                                :patient/address          "ADDRESS"
                                :patient/gender           "MALE"
                                :patient/insurance-policy 123143141
                                :patient/birth-date       (jt/local-date)}})

  (app-routes {:uri            "/api/patient"
               :request-method :delete
               :body-params    {:patient/id #uuid"54a89501-9dd4-4179-9563-b7cae17c763c",}})

  (app-routes {:uri            "/"
               :request-method :get})
  )