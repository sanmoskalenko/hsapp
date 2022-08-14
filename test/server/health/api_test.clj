(ns server.health.api-test
  (:require
    [clojure.test :refer [is use-fixtures deftest testing]]
    [server.health.api :as sut]
    [server.health.test.fixtures :as test.fixtures]
    [matcho.core :refer [match]]))

(use-fixtures
  :once test.fixtures/system-fixtures)

(deftest ^:integrations app-routes-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

    (testing "Request to the root of the site redirects to the correct path"
      (let [exp {:body "" :headers {"Location" "/index.html"} :status 302}
            res (sut/app-routes {:uri "/" :request-method :get} wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the list of patients is empty the correct error is returned"
      (let [exp {:body {:error/msg "Patients not-found"} :headers {} :status 404}
            res (sut/app-routes {:uri "/api/patient" :request-method :get} wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "Request with data to create an patient is processed correctly"
      (let [patient-first      (sut/app-routes {:uri            "/api/patient"
                                                :request-method :post
                                                :body-params    {:fname            "FNAME"
                                                                 :mname            "MNAME"
                                                                 :lname            "LNAME"
                                                                 :address          "ADDRESS"
                                                                 :gender           "MALE"
                                                                 :insurance-policy 123143141
                                                                 :birth-date       "2022-07-23T21:00:00.000-00:00"}}
                                               wrapper jetty-async-fn ds)

            patient-second     (sut/app-routes {:uri            "/api/patient"
                                                :request-method :post
                                                :body-params    {:fname            "Roy"
                                                                 :mname            "Philip"
                                                                 :lname            "Jones"
                                                                 :address          "61 9th Ave Street"
                                                                 :gender           "MALE"
                                                                 :insurance-policy "AA-364-2319"
                                                                 :birth-date       "2022-07-23T21:00:00.000-00:00"}}
                                               wrapper jetty-async-fn ds)


            exp-patient-first  {:body    [#:patient{:address          "ADDRESS"
                                                    :birth_date       #inst "2022-07-23T21:00:00.000000000-00:00"
                                                    :created_at       (-> patient-first :body first :patient/created_at)
                                                    :fname            "FNAME"
                                                    :gender           "MALE"
                                                    :id               (-> patient-first :body first :patient/id)
                                                    :insurance_policy "123143141"
                                                    :lname            "LNAME"
                                                    :mname            "MNAME"
                                                    :updated_at       (-> patient-first :body first :patient/updated_at)}]
                                :headers {}
                                :status  200}
            exp-patient-second {:body    [#:patient{:address          "61 9th Ave Street"
                                                    :birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                                                    :created_at       (-> patient-second :body first :patient/created_at)
                                                    :fname            "Roy"
                                                    :gender           "MALE"
                                                    :id               (-> patient-second :body first :patient/id)
                                                    :insurance_policy "AA-364-2319"
                                                    :lname            "Jones"
                                                    :mname            "Philip"
                                                    :updated_at       (-> patient-second :body first :patient/updated_at)}]
                                :headers {}
                                :status  200}]
        (is (match patient-first exp-patient-first))
        (is (match patient-second exp-patient-second))))

    (testing "It is not possible to create a user without the required fields filled in"
      (let [exp {:body    #:error {:msg "Required fields must not be empty"}
                 :headers {}
                 :status  400}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :post
                                 :body-params    {:fname            "FNAME"
                                                  :mname            nil
                                                  :lname            nil
                                                  :address          nil
                                                  :gender           "MALE"
                                                  :insurance-policy "123143141"
                                                  :birth-date       "2022-07-23T21:00:00.000-00:00"}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "Errors when inserting data into the database are handled correctly"
      (let [exp {:body    #:error {:msg "User creation errors, check server logs"},
                 :headers {},
                 :status  400}
            res (sut/create-patient ds {:uri            "/api/patient"
                                        :request-method :post
                                        :body-params    {:fname            "FNAME"
                                                         :gender           "MALE"
                                                         :insurance-policy "123143141"
                                                         :birth-date       "2022-07-23T21:00:00.000-00:00"}})]
        (is (= res exp))))

    (testing "If there are patients in the database the list is returned correctly"
      (let [res (sut/app-routes {:uri "/api/patient" :request-method :get} wrapper jetty-async-fn ds)
            exp {:body    [#:patient{:address          "ADDRESS"
                                     :birth_date       #inst "2022-07-23T21:00:00.000000000-00:00"
                                     :created_at       inst?
                                     :fname            "FNAME"
                                     :gender           "MALE"
                                     :id               uuid?
                                     :insurance_policy "123143141"
                                     :lname            "LNAME"
                                     :mname            "MNAME"
                                     :updated_at       inst?}
                           #:patient{:address          "61 9th Ave Street"
                                     :birth_date       #inst "2022-07-23T21:00:00.000000000-00:00"
                                     :created_at       inst?
                                     :fname            "Roy"
                                     :gender           "MALE"
                                     :id               uuid?
                                     :insurance_policy "AA-364-2319"
                                     :lname            "Jones"
                                     :mname            "Philip"
                                     :updated_at       inst?}]
                 :headers {}
                 :status  200}]
        (is (match res exp))))

    (testing "If the user is not found then the correct message is returned"
      (let [exp {:body {:msg "Patient not found"} :headers {} :status 404}
            res (sut/app-routes {:uri            "/api/search"
                                 :request-method :get
                                 :params         {:fname "NO-NAME"}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the search parameters are not set, the correct error is returned"
      (let [exp {:body {:msg "Patient not found"} :headers {} :status 404}
            res (sut/app-routes {:uri            "/api/search"
                                 :request-method :get
                                 :params         {}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the patient matches the search condition then it is returned correctly"
      (let [res (sut/app-routes {:uri            "/api/search"
                                 :request-method :get
                                 :params         {:fname "FNAME"}}
                                wrapper jetty-async-fn ds)
            exp {:headers {}
                 :status  200
                 :body    [{:patient/address          "ADDRESS"
                            :patient/birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                            :patient/created_at       inst?
                            :patient/fname            "FNAME"
                            :patient/gender           "MALE"
                            :patient/id               uuid?
                            :patient/insurance_policy "123143141"
                            :patient/lname            "LNAME"
                            :patient/mname            "MNAME"
                            :patient/updated_at       inst?}]}]
        (is (match res exp))))

    (testing "If no patient ID is passed then a valid error is returned"
      (let [exp {:body    #:error {:msg "Impossible update patient, because id is null"}
                 :headers {}
                 :status  404}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :put
                                 :body-params    {:fname            "FNAME"
                                                  :gender           "MALE"
                                                  :insurance-policy 123143141}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the ID is not passed when updating the patient, the correct error is returned"
      (let [exp {:body    #:error{:msg "Impossible update patient, because id is null"}
                 :headers {}
                 :status  404}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :put
                                 :body-params    {:fname            "FNAME"
                                                  :gender           "MALE"
                                                  :insurance-policy 123143141}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the request is correct, the patient is updated correctly"
      (let [patient (sut/app-routes {:uri            "/api/search"
                                     :request-method :get
                                     :params         {:fname "FNAME"}}
                                    wrapper jetty-async-fn ds)
            exp     {:body    [#:patient{:address          "ADDRESS"
                                         :birth_date       #inst "2022-07-23T21:00:00.000000000-00:00"
                                         :created_at       inst?
                                         :fname            "FNAME"
                                         :gender           "MALE"
                                         :id               (-> patient :body first :patient/id)
                                         :insurance_policy "987654321"
                                         :lname            "LNAME"
                                         :mname            "MNAME"
                                         :updated_at       inst?}]
                     :headers {}
                     :status  200}
            res     (sut/app-routes {:uri            "/api/patient"
                                     :request-method :put
                                     :body-params    {:id               (str (-> patient :body first :patient/id))
                                                      :fname            "FNAME"
                                                      :gender           "MALE"
                                                      :insurance-policy 987654321}}
                                    wrapper jetty-async-fn ds)]
        (is (match res exp))))

    (testing "If an empty field is passed, then the update is not possible"
      (let [patient (sut/app-routes {:uri            "/api/search"
                                     :request-method :get
                                     :params         {:fname "FNAME"}}
                                    wrapper jetty-async-fn ds)
            exp     {:body    #:error{:msg "Required fields must not be empty"}
                     :headers {}
                     :status  404}
            res     (sut/app-routes {:uri            "/api/patient"
                                     :request-method :put
                                     :body-params    {:id               (str (-> patient :body first :patient/id))
                                                      :fname            nil
                                                      :gender           "MALE"
                                                      :insurance-policy 987654321}}
                                    wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If no patient ID is passed then a valid error is returned"
      (let [exp {:body    #:error {:msg "ID must not be null"}
                 :headers {}
                 :status  404}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :delete}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "Patient with non-existent ID cannot be deleted correct error returned"
      (let [exp {:body    #:error {:msg "Patient with id f5e8f146-ccfd-4750-866a-0620baec9774 not found"}
                 :headers {}
                 :status  404}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :delete
                                 :body-params    "f5e8f146-ccfd-4750-866a-0620baec9774"}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))


    (testing "A patient with an existing id is deleted correctly"
      (let [patient (sut/app-routes {:uri            "/api/search"
                                     :request-method :get
                                     :params         {:fname "FNAME"}}
                                    wrapper jetty-async-fn ds)
            exp {:body    {:msg (format "Patient with id %s deleted" (str (-> patient :body first :patient/id)))}
                 :headers {}
                 :status  200}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :delete
                                 :body-params    (str (-> patient :body first :patient/id))}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))))
