(ns server.health.patient.api.v1-test
  (:require
    [clojure.test :refer :all]
    [server.health.patient.api.v1 :as sut]))


(deftest ^:unit empty-required-fields-test
  (testing "Empty values are returned correctly"
    (let [exp {:some-value1 nil}
          res (#'sut/empty-required-fields {:some-value1 nil
                                            :some-value2 "some-value"})]
      (is (= res exp)))))


(deftest normalize-params-test
  (testing "Transforming map keys from strings to keywords is working correctly"
    (let [exp {:some-k-1 2
               :some-k-2 "some-value"}
          res (sut/normalize-params {"some-k-1" 2
                                     "some-k-2" "some-value"})]
      (is (= exp res)))))


(deftest str->inst-test
  (testing "Transforming date with string to inst is working correctly"
    (let [exp #inst"2022-08-04T09:33:02.545944000-00:00"
          res (sut/str->inst "2022-08-04T09:33:02.545944000-00:00")]
      (is (= exp res)))))
