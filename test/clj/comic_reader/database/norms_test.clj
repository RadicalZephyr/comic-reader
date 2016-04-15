(ns comic-reader.database.norms-test
  (:require  [clojure.test :refer :all]
             [comic-reader.database.norms :as sut]
             [clojure.java.io :as io]))

(deftest test-norms-seq

  (is (nil? (sut/norms-seq "database/no-norms")))

  (is (= [(io/as-file
           (io/resource
            "database/test-norms/this-fake-norm.edn"))]
         (sut/norms-seq "database/test-norms"))))
