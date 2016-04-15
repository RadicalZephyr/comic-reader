(ns comic-reader.database.norms-test
  (:require  [clojure.test :refer :all]
             [clojure.java.io :as io]
             [comic-reader.database.norms :as sut]))

(defn- file-resource [name]
  (-> name io/resource io/as-file))

(deftest test-norms-seq

  (is (nil? (sut/norms-seq "database/no-norms")))

  (is (= [(io/as-file
           (io/resource
            "database/test-norms/this-fake-norm.edn"))]
         (sut/norms-seq "database/test-norms"))))

(deftest test-files->norms-map

  (is (= {:this-fake-norm []}
         (sut/files->norms-map [(file-resource "database/test-norms/this-fake-norm.edn")]))))

(deftest test-norms-map
  (is nil? (sut/norms-map "database/no-norms"))

  (is (= {:this-fake-norm []}
         (sut/norms-map "database/test-norms"))))
