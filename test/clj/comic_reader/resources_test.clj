(ns comic-reader.resources-test
  (:require [clojure.test :refer :all]
            [comic-reader.resources :as sut]
            [clojure.java.io :as io]))

(defn file-resource [name]
  (-> name io/resource io/as-file))

(deftest file-seq-test
  (is (nil?
       (sut/file-seq "database/no-test-norms")))

  (is (= (set
          (map file-resource
               ["database/test-norms/this-fake-norm.edn"
                "database/test-norms/enumeration-norm.edn"]))
         (set (sut/file-seq "database/test-norms")))))

(deftest read-resource-test
  (is (nil? (sut/read-resource "non/existent")))

  (is (= []
         (sut/read-resource "database/test-norms/this-fake-norm.edn"))))
