(ns comic-reader.sites.read-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.read :refer :all]))


(deftest base-name-test
  (is (= (base-name "abc.123")
         "abc"))
  (is (= (base-name "thingy.clj")
         "thingy"))
  (is (= (base-name "thing.part.two.clj")
         "thing.part.two"))
  (is (= (base-name "dir/two/three/four.clj")
         "four")))

(deftest get-all-sites-test
  (is (some #{"test-site"} (get-all-sites))))

(deftest read-site-options-test
  (is (thrown?
       java.lang.IllegalArgumentException
       (read-site-options "non-existent")))
  (is (= (class (read-site-options "test-site"))
         clojure.lang.PersistentArrayMap)))
