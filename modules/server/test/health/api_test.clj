(ns health.api-test
  (:require
    [clojure.test :refer :all]
    [health.api :as sut]
    [health.patient.db :as db]
    [unifier.response :as r]))

;; TODO It would be nice to cover with integration tests, but is it worth it for a test case??

(deftest ^:unit create-patient-test
  (testing "Request with data to create an patient is processed correctly"
    (with-redefs [db/create-patient (constantly
                                      (r/get-data (r/as-success
                                                    [{:patient/insurance_policy "123143141"
                                                      :patient/mname            "MNAME"
                                                      :patient/id               #uuid"20259864-b35e-4dfc-94ae-6845fc955a6b"
                                                      :patient/updated_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                      :patient/lname            "LNAME"
                                                      :patient/gender           "MALE"
                                                      :patient/birth_date       #inst"2022-07-23T21:00:00.000-00:00"
                                                      :patient/created_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                      :patient/fname            "FNAME"
                                                      :patient/address          "ADDRESS"}])))]
      (let [exp {:status  200
                 :headers {}
                 :body    [{:patient/insurance_policy "123143141"
                            :patient/mname            "MNAME"
                            :patient/id               #uuid"20259864-b35e-4dfc-94ae-6845fc955a6b"
                            :patient/updated_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                            :patient/lname            "LNAME"
                            :patient/gender           "MALE"
                            :patient/birth_date       #inst"2022-07-23T21:00:00.000-00:00"
                            :patient/created_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                            :patient/fname            "FNAME"
                            :patient/address          "ADDRESS"}]}
            res (sut/create-patient {:patient/fname            "FNAME"
                                     :patient/mname            "MNAME"
                                     :patient/lname            "LNAME"
                                     :patient/address          "ADDRESS"
                                     :patient/gender           "MALE"
                                     :patient/insurance-policy "123143141"
                                     :patient/birth-date       #inst"2022-07-23T21:00:00.000-00:00"})]
        (is (= res exp)))))

  (testing "It is not possible to create a user without the required fields filled in"
    (let [exp {:body    #:error {:msg "Required fields must not be empty"}
               :headers {}
               :status  400}
          res (sut/create-patient {:body-params {:patient/fname            "FNAME"
                                                 :patient/mname            nil
                                                 :patient/lname            nil
                                                 :patient/address          nil
                                                 :patient/gender           "MALE"
                                                 :patient/insurance-policy "123143141"
                                                 :patient/birth-date       #inst"2022-07-23T21:00:00.000-00:00"}})]
      (is (= res exp))))

  (testing "Errors when inserting data into the database are handled correctly"
    (let [exp {:body    #:error {:msg "User creation errors, check server logs"},
               :headers {},
               :status  400}
          res (sut/create-patient {:body-params {:patient/fname            "FNAME"
                                                 :patient/gender           "MALE"
                                                 :patient/insurance-policy "123143141"
                                                 :patient/birth-date       #inst"2022-07-23T21:00:00.000-00:00"}})]
      (is (= res exp)))))


(deftest ^:unit list-patient-test
  (testing "If the list of patients is empty the correct error is returned"
    (with-redefs [db/list-all (constantly
                                (r/get-data (r/as-not-found [])))]
      (let [exp {:body {:error/msg "Patients not-found"} :headers {} :status 404}
            res (sut/list-patient)]
        (is (= res exp)))))

  (testing "If there are patients in the database the list is returned correctly"
    (with-redefs [db/list-all (constantly
                                (r/get-data (r/as-success
                                              [{:patient/insurance_policy "123143141"
                                                :patient/mname            "MNAME"
                                                :patient/id               #uuid"20259864-b35e-4dfc-94ae-6845fc955a6b"
                                                :patient/updated_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                :patient/lname            "LNAME"
                                                :patient/gender           "MALE"
                                                :patient/birth_date       #inst"2022-07-23T21:00:00.000-00:00"
                                                :patient/created_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                :patient/fname            "FNAME"
                                                :patient/address          "ADDRESS"}])))]
      (let [exp {:headers {}
                 :status  200
                 :body    [{:patient/address          "ADDRESS"
                            :patient/birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                            :patient/created_at       #inst "2022-07-24T19:28:20.122-00:00"
                            :patient/fname            "FNAME"
                            :patient/gender           "MALE"
                            :patient/id               #uuid "20259864-b35e-4dfc-94ae-6845fc955a6b"
                            :patient/insurance_policy "123143141"
                            :patient/lname            "LNAME"
                            :patient/mname            "MNAME"
                            :patient/updated_at       #inst "2022-07-24T19:28:20.122-00:00"}]}
            res (sut/list-patient)]
        (is (= res exp))))))


(deftest ^:unit search-patient-test
  (testing "If the user is not found then the correct message is returned"
    (with-redefs [db/get-patient (constantly
                                   (r/get-data (r/as-not-found [])))]
      (let [exp {:body {:msg "Patient not found"} :headers {} :status 404}
            res (sut/search-patient {:body-params {:patient/fname "FNAME"}})]
        (is (= res exp)))))

  (testing "If the patient matches the search condition then it is returned correctly"
    (with-redefs [db/get-patient (constantly
                                   (r/get-data (r/as-success
                                                 [{:patient/insurance_policy "123143141"
                                                   :patient/mname            "MNAME"
                                                   :patient/id               #uuid"20259864-b35e-4dfc-94ae-6845fc955a6b"
                                                   :patient/updated_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                   :patient/lname            "LNAME"
                                                   :patient/gender           "MALE"
                                                   :patient/birth_date       #inst"2022-07-23T21:00:00.000-00:00"
                                                   :patient/created_at       #inst"2022-07-24T19:28:20.122550000-00:00"
                                                   :patient/fname            "FNAME"
                                                   :patient/address          "ADDRESS"}])))]
      (let [exp {:headers {}
                 :status  200
                 :body    [{:patient/address          "ADDRESS"
                            :patient/birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                            :patient/created_at       #inst "2022-07-24T19:28:20.122-00:00"
                            :patient/fname            "FNAME"
                            :patient/gender           "MALE"
                            :patient/id               #uuid "20259864-b35e-4dfc-94ae-6845fc955a6b"
                            :patient/insurance_policy "123143141"
                            :patient/lname            "LNAME"
                            :patient/mname            "MNAME"
                            :patient/updated_at       #inst "2022-07-24T19:28:20.122-00:00"}]}
            res (sut/search-patient {:body-params {:patient/fname "FNAME"}})]
        (is (= res exp))))))


(deftest ^:unit update-patient-test
  (testing "If no patient ID is passed then a valid error is returned"
    (let [exp {:body    #:error {:msg "Impossible update patient, because id is null"}
               :headers {}
               :status  404}
          res (sut/update-patient {:body-params {:patient/fname            "FNAME"
                                                 :patient/gender           "MALE"
                                                 :patient/insurance-policy "123143141"}})]
      (is (= res exp))))

  (testing "If an empty field is passed, then the update is not possible"
    (with-redefs [db/get-by-id (constantly "some-id")]
      (let [exp {:body    #:error {:msg "Required fields must not be empty"}
                 :headers {}
                 :status  404}
            res (sut/update-patient {:body-params {:patient/id     #uuid "20259864-b35e-4dfc-94ae-6845fc955a6b"
                                                   :patient/fname  "FNAME"
                                                   :patient/gender nil}})]
        (is (= res exp)))))

  (testing "If an empty field is passed, then the update is not possible"
    (with-redefs [db/get-by-id      (constantly "some-id")
                  db/update-patient (constantly
                                      (r/get-data (r/as-success
                                                    [{:patient/insurance_policy "123143141"
                                                      :patient/mname            "MNAME"
                                                      :patient/id               #uuid"f5e8f146-ccfd-4750-866a-0620baec9774"
                                                      :patient/updated_at       #inst"2022-07-24T20:23:14.536982000-00:00"
                                                      :patient/lname            "LNAME1"
                                                      :patient/gender           "FEMALE"
                                                      :patient/birth_date       #inst"2022-07-23T21:00:00.000-00:00"
                                                      :patient/created_at       #inst"2022-07-24T20:22:52.323822000-00:00"
                                                      :patient/fname            "FNAME"
                                                      :patient/address          "ADDRESS"}])))]
      (let [exp {:headers {},
                 :status  200
                 :body    [{:patient/address          "ADDRESS"
                            :patient/birth_date       #inst "2022-07-23T21:00:00.000-00:00"
                            :patient/created_at       #inst "2022-07-24T20:22:52.323-00:00"
                            :patient/fname            "FNAME"
                            :patient/gender           "FEMALE"
                            :patient/id               #uuid "f5e8f146-ccfd-4750-866a-0620baec9774"
                            :patient/insurance_policy "123143141"
                            :patient/lname            "LNAME1"
                            :patient/mname            "MNAME"
                            :patient/updated_at       #inst "2022-07-24T20:23:14.536-00:00"}]}
            res (sut/update-patient {:body-params {:patient/id    #uuid"f5e8f146-ccfd-4750-866a-0620baec9774"
                                                   :patient/lname "LNAME1"}})]
        (is (= res exp))))))

(deftest delete-patient-test
  (testing "If no patient ID is passed then a valid error is returned"
    (let [exp {:body    #:error {:msg "ID must not be null"}
               :headers {}
               :status  404}
          res (sut/delete-patient {:body-params {}})]
      (is (= res exp))))

  (testing "Patient with non-existent ID cannot be deleted correct error returned"
    (with-redefs [db/get-by-id (constantly nil)]
      (let [exp {:body    #:error {:msg "Patient with id f5e8f146-ccfd-4750-866a-0620baec9774 not found"}
                 :headers {}
                 :status  404}
            res (sut/delete-patient {:body-params {:patient/id #uuid "f5e8f146-ccfd-4750-866a-0620baec9774"}})]
        (is (= res exp)))))

  (testing "Patient with non-existent ID cannot be deleted correct error returned"
    (with-redefs [db/get-by-id      (constantly "some-value")
                  db/delete-patient (constantly nil)]
      (let [exp {:body    {:msg "Patient with id f5e8f146-ccfd-4750-866a-0620baec9774 deleted"}
                 :headers {}
                 :status  200}
            res (sut/delete-patient {:body-params {:patient/id #uuid "f5e8f146-ccfd-4750-866a-0620baec9774"}})]
        (is (= res exp))))))
