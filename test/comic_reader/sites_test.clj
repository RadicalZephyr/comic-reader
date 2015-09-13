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
         ["manga-fox" "test-site"])))

(deftest read-site-options-test
  (is (thrown?
       java.lang.IllegalArgumentException
       (read-site-options "non-existent")))
  (is (= (class (read-site-options "test-site"))
         clojure.lang.PersistentArrayMap)))

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         [nil nil]))

  (let [[label opts] (make-site-entry "test-site")]
    (is (= label
           "test-site"))
    (is (= (class opts)
           comic_reader.sites.MangaSite))))

(defn expect-opts-are-map [site]
  (try
    (let [opts (read-site-options site)]
      (is (= (class opts)
             clojure.lang.PersistentHashMap)
          (str "Contents of `sites/" site ".clj'"
               " must be a map literal")))
    (catch java.lang.RuntimeException re
      nil)))

(defn testdef-form [site-name]
  `(deftest ~(symbol (str site-name "-test"))
     (expect-opts-are-map ~site-name)))

(defmacro defsite-tests []
  (try
   (let [site-names (map first sites)]
     `(do ~@(map testdef-form site-names)))
   (catch RuntimeException e
     `(do))))

(defsite-tests)
