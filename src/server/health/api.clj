(ns server.health.api
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.util.http-response :as response]
    [mount.core :refer [defstate]]
    [muuntaja.middleware :as middleware]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [unifier.response :as r]
    [server.health.patient.api :as api]
    [server.health.config :refer [ctx]]
    [cambium.core :as log]
    [selmer.parser :as parser]))

(parser/set-resource-path!  (clojure.java.io/resource "public"))

(defn home-page []
  (parser/render-file "index.html" {}))

(defn create-patient
  "Create patient request handler"
  [request]
  (log/info {:msg    "Receive request to add new patient"
             :params (:body-params request)})
  (let [patient (api/create-patient request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/bad-request))))


(defn list-patient []
  "Request handler for getting a list of patient"
  (log/info {:msg "Receive request to list patients"})
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
  (GET "/" [] (home-page))
  (context "/api" []
   (GET "/patient" [] (list-patient))
   (GET "/search" request (search-patient request))
   (POST "/patient" request (create-patient request))
   (PUT "/patient" request (update-patient request))
   (DELETE "/patient" request (delete-patient request)))
  (route/not-found (response/not-found)))


(def app
  (-> app-routes
      middleware/wrap-format
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))


(defstate webserver
  :start (do
           (log/info {:msg "Start webserver!"})
           (run-jetty #'app (:webserver ctx)))
  :stop (do
          (log/info {:msg "Stop webserver"})
          (.stop webserver)))

(comment

  (app-routes {:uri            "/api/patient"
               :request-method :get})

  (app-routes {:uri            "/api/patient"
               :request-method :put
               :body-params    {:id     "00c36cf6-27b2-4533-8224-eff92b906206",
                                :lname  "LNAME5",
                                :gender "FEMALE",}})

  (app-routes {:uri            "/api/patient"
               :request-method :post
               :body-params    {:fname            "James"
                                :mname            "Alice"
                                :lname            "Greeen"
                                :address          "61 9th Ave"
                                :gender           "MALE"
                                :insurance-policy 123143141
                                :birth-date       "2022-08-04T09:33:02.545944000-00:00"}})

  (app-routes {:uri            "/api/patient"
               :request-method :delete
               :body-params    "20259864-b35e-4dfc-94ae-6845fc955a6b"})

  (app-routes {:uri            "/"
               :request-method :get})



  )