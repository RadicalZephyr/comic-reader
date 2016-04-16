(ns comic-reader.database.norms-test
  (:require  [clojure.test :refer :all]
             [clojure.java.io :as io]
             [comic-reader.database.norms :as sut]
             [comic-reader.resources :as resources]))

(deftest norms-seq-test

  (is (nil? (sut/norms-seq "database/no-norms")))

  (is (= (set (map resources/resource-file
                   ["database/test-norms/this-fake-norm.edn"
                    "database/test-norms/enumeration-norm.edn"]))
         (set (sut/norms-seq "database/test-norms")))))

(deftest files->norms-map-test

  (is (= {:this-fake-norm {:txes [[]]}}
         (sut/files->norms-map [(resources/resource-file
                                 "database/test-norms/this-fake-norm.edn")]))))

(deftest norms-map-test
  (is nil? (sut/norms-map "database/no-norms"))

  (is (= {:this-fake-norm {:txes [[]]}}
         (select-keys (sut/norms-map "database/test-norms")
                      [:this-fake-norm]))))
