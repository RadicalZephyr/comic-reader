(ns comic-reader.resources-test
  (:require [clojure.test :refer :all]
            [comic-reader.resources :as sut]
            [clojure.java.io :as io]))

(deftest resource-seq-test
  (is (nil?
       (sut/resource-seq "database/no-test-norms")))

  (is (= (set
          ["database/test-norms/this-fake-norm.edn"
           "database/test-norms/enumeration-norm.edn"])
         (set (sut/resource-seq "database/test-norms")))))

(deftest read-resource-test
  (is (nil? (sut/read-resource "non/existent")))

  (is (= []
         (sut/read-resource "database/test-norms/this-fake-norm.edn"))))
