(ns comic-reader.api-test
  (:require [comic-reader.api :as sut]
            [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]))

(deftest test-add-error
  (is (= [{}] (sut/add-error nil {})))
  (is (= [{}] (sut/add-error [] {})))
  (is (= [{:a 1} {:b 2}] (sut/add-error [{:a 1}] {:b 2}))))

(deftest test-error-wiring
  (let [error {:a "bad error"
               :thing "happened"}]
    (sut/setup!)
    (sut/report-error error)
    (is (= error @sut/*last-error*))))
