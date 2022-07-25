(ns health.patient.api.v1-test
  (:require
    [clojure.test :refer :all]
    [health.patient.api.v1 :as sut]))


(deftest ^:unit empty-required-fields-test
  (testing "Empty values are returned correctly"
    (let [exp {:some-value1 nil}
          res (#'sut/empty-required-fields {:some-value1 nil
                                            :some-value2 "some-value"})]
      (is (= res exp)))))

(deftest ^:unit not-empty-fields-test
  (testing "Non empty values are returned correctly"
    (let [exp {:some-value2 "some-value"}
          res (#'sut/not-empty-fields {:some-value1 nil
                                       :some-value2 "some-value"})]
      (is (= res exp)))))
