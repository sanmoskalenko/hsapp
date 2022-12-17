(ns server.health.api
  (:require
    [compojure.core]
    [ring.util.response :as util.response]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.util.http-response :as response]
    [mount.core :refer [defstate]]
    [muuntaja.middleware :as middleware]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [unifier.response :as r]
    [server.health.patient.api :as api]
    [server.health.config :refer [ctx]]
    [server.health.dao.pg :refer [ds]]
    [cambium.core :as log]))

(def home-page
  (util.response/redirect "/index.html"))

(defn create-patient
  "Create patient request handler"
  [ds request]
  (log/info {:msg    "Receive request to add new patient"
             :params (:body-params request)})
  (let [patient (api/create-patient ds request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/bad-request))))


(defn list-patient
  "Request handler for getting a list of patient"
  [ds]
  (log/info {:msg "Receive request to list patients"})
  (let [patient (api/list-patient ds)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn search-patient
  "Request handler for search patient"
  [ds request]
  (log/info {:msg "Receive request to search patient"})
  (let [patient (api/search-patient ds request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn update-patient
  "Request handler for update of patient"
  [ds request]
  (log/info {:msg "Receive request to update patient"})
  (let [patient (api/update-patient ds request)]
    (if (r/success? patient)
      (-> patient r/get-data response/ok)
      (-> patient r/get-data response/not-found))))


(defn delete-patient
  "Request handler for delete patient"
  [ds request]
  (log/info {:msg "Receive request to delete patient"})
  (let [res (api/delete-patient ds request)]
    (if (r/success? res)
      (-> res r/get-data response/ok)
      (-> res r/get-data response/not-found))))


;(defroutes app-routes
;  (GET "/" [] (home-page))
;  (context "/api" []
;   (GET "/patient" [] (list-patient))
;   (GET "/search" request (search-patient request))
;   (POST "/patient" request (create-patient request))
;   (PUT "/patient" request (update-patient request))
;   (DELETE "/patient" request (delete-patient request)))
;  (route/not-found (response/not-found)))


;; Datasource was added into f for the tests
(defn app-routes
  ([request wrapper _]
   (app-routes request wrapper _ ds))

  ([request wrapper _ ds]
   (let [{:keys [uri request-method]} request]
     (case [request-method uri]
       [:get "/"] (-> home-page wrapper)
       [:get "/api/patient"] (wrapper (list-patient ds) )
       [:get "/api/search"] (wrapper (search-patient ds request))
       [:put "/api/patient"] (wrapper (update-patient ds request))
       [:post "/api/patient"] (wrapper (create-patient ds request))
       [:delete "/api/patient"] (wrapper (delete-patient ds request))
       (wrapper (response/not-found))))))


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
