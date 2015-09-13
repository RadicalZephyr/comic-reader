(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]))

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
  (is (= (get-all-sites)
         ["manga-fox"])))

(deftest read-site-options-test
  (is (thrown?
       java.lang.IllegalArgumentException
       (read-site-options "non-existent")))
  (is (= (class (read-site-options "manga-fox"))
         clojure.lang.PersistentHashMap)))

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         [nil nil]))

  (let [[label opts] (make-site-entry "manga-fox")]
    (is (= label
           "manga-fox"))
    (is (= (class opts)
           comic_reader.sites.MangaSite))))
