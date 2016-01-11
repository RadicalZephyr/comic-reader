(ns comic-reader.sites.read-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
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
  (let [test-file (io/as-file "resources/sites/test-site.clj")]
    (spit test-file "{}")
    (is (some #{"test-site"} (get-all-sites)))
    (io/delete-file test-file))

  (let [test-file (io/as-file "resources/sites/abc")]
    (spit test-file "nothing important")
    (is (= (not-any? #{"abc"} (get-all-sites))))
    (io/delete-file test-file)))

(deftest read-site-options-test
  (is (thrown?
       java.lang.IllegalArgumentException
       (read-site-options "non-existent")))

  (let [test-file (io/as-file "resources/sites/test-site.clj")]
    (spit test-file "{}")
    (is (= (class (read-site-options "test-site"))
           clojure.lang.PersistentArrayMap))
    (io/delete-file test-file)))
