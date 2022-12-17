(ns server.health.api-test
  (:require
    [clojure.test :refer [deftest is testing use-fixtures]]
    [matcho.core :refer [match]]
    [server.health.api :as sut]
    [server.health.patient.db :as db]
    [server.health.test.fixtures :as test.fixtures]))


(use-fixtures
  :each test.fixtures/system-fixtures)


(deftest app-routes-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

    (testing "Request to the root of the site redirects to the correct path"
      (let [exp {:body "" :headers {"Location" "/index.html"} :status 302}
            res (sut/app-routes {:uri "/" :request-method :get} wrapper jetty-async-fn ds)]
        (is (= res exp))))))


(deftest create-patient-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

    (testing "Request with data to create an patient is processed correctly"
      (let [res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :post
                                 :body-params    {:fname            "Roy"
                                                  :mname            "Philip"
                                                  :lname            "Jones"
                                                  :address          "61 9th Ave Street"
                                                  :gender           "MALE"
                                                  :insurance-policy "AA-364-2319"
                                                  :birth-date       "2022-07-23T21:00:00.000-00:00"}}
                                wrapper jetty-async-fn ds)
            exp {:body    [#:patient{:address          "61 9th Ave Street"
                                     :birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                                     :created_at       (-> res :body first :patient/created_at)
                                     :fname            "Roy"
                                     :gender           "MALE"
                                     :id               (-> res :body first :patient/id)
                                     :insurance_policy "AA-364-2319"
                                     :lname            "Jones"
                                     :mname            "Philip"
                                     :updated_at       (-> res :body first :patient/updated_at)}]
                 :headers {}
                 :status  200}]
        (is (match res exp))))

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
        (is (= res exp))))))


(deftest update-patient-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

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

    (testing "If an empty field is passed, then the update is not possible"
      (let [exp {:body    #:error{:msg "Required fields must not be empty"}
                 :headers {}
                 :status  404}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :put
                                 :body-params    {:id               "692f57f9-0b01-47df-b928-46ce92b8ea83"
                                                  :fname            nil
                                                  :gender           "MALE"
                                                  :insurance-policy 987654321}}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))

    (testing "If the request is correct, the update occurs correctly"
      (let [exp {:body    [#:patient{:address          "160 Broadway"
                                     :birth_date       inst?
                                     :created_at       inst?
                                     :fname            "TEST_NAME"
                                     :gender           "MALE"
                                     :id               #uuid "692f57f9-0b01-47df-b928-46ce92b8ea83"
                                     :insurance_policy "987654321"
                                     :lname            "Smith"
                                     :mname            "Alice"
                                     :updated_at       inst?}]
                 :headers {}
                 :status  200}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :put
                                 :body-params    {:id               "692f57f9-0b01-47df-b928-46ce92b8ea83"
                                                  :fname            "TEST_NAME"
                                                  :gender           "MALE"
                                                  :insurance-policy 987654321}}
                                wrapper jetty-async-fn ds)]
        (is (match res exp))))))


(deftest delete-patient-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

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
      (let [exp {:body    {:msg "Patient with id 692f57f9-0b01-47df-b928-46ce92b8ea83 deleted"}
                 :headers {}
                 :status  200}
            res (sut/app-routes {:uri            "/api/patient"
                                 :request-method :delete
                                 :body-params    "692f57f9-0b01-47df-b928-46ce92b8ea83"}
                                wrapper jetty-async-fn ds)]
        (is (= res exp))))))


(deftest search-patient-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

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
                                 :params         {:fname "Sam"}}
                                wrapper jetty-async-fn ds)

            exp {:body    [#:patient{:address          "160 Broadway"
                                     :birth_date       #inst "2022-08-04T09:33:02.545944000-00:00"
                                     :created_at       inst?
                                     :fname            "Sam"
                                     :gender           "FEMALE"
                                     :id               #uuid "692f57f9-0b01-47df-b928-46ce92b8ea83"
                                     :insurance_policy "9876543"
                                     :lname            "Smith"
                                     :mname            "Alice"
                                     :updated_at       inst?}]
                 :headers {}
                 :status  200}]
        (is (match res exp))))))


(deftest list-patient-test
  (let [ds             test.fixtures/ds
        jetty-async-fn (constantly nil)
        wrapper        (fn [x] x)]

    (testing "If the list of patients is empty the correct error is returned"
      (with-redefs [db/list-all (constantly nil)]
        (let [exp {:body {:error/msg "Patients not-found"} :headers {} :status 404}
              res (sut/app-routes {:uri "/api/patient" :request-method :get} wrapper jetty-async-fn ds)]
          (is (= res exp)))))

    (testing "If there are patients in the database the list is returned correctly"
      (let [res (sut/app-routes {:uri "/api/patient" :request-method :get} wrapper jetty-async-fn ds)
            exp {:body    [#:patient{:address          "61 9th Ave"
                                     :birth_date       #inst "2022-08-04T09:33:02.545944000-00:00"
                                     :created_at       inst?
                                     :fname            "James"
                                     :gender           "MALE"
                                     :id               #uuid "692f57f9-0b01-47df-b928-46ce92b8ea82"
                                     :insurance_policy "123143141"
                                     :lname            "Greeen"
                                     :mname            "Alice"
                                     :updated_at       inst?}
                           #:patient{:address          "160 Broadway"
                                     :birth_date       #inst "2022-08-04T09:33:02.545944000-00:00"
                                     :created_at       inst?
                                     :fname            "Sam"
                                     :gender           "FEMALE"
                                     :id               #uuid "692f57f9-0b01-47df-b928-46ce92b8ea83"
                                     :insurance_policy "9876543"
                                     :lname            "Smith"
                                     :mname            "Alice"
                                     :updated_at       inst?}]
                 :headers {}
                 :status  200}]
        (is (match res exp))))))