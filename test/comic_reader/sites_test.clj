(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [clojure.java.io :as io]))

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

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         ["non-existent" nil]))

  (let [[label opts] (make-site-entry "test-site")]
    (is (= label
           "test-site"))
    (is (= (class opts)
           comic_reader.sites.MangaSite))))

(defn expect-opts-are-map [site]
  (try
    (let [opts (read-site-options site)]
      (is (map? opts)
          (str "Contents of `sites/" site ".clj'"
               " must be a map literal")))
    (catch java.lang.RuntimeException re
      (is false
          (str "Contents of `sites/" site ".clj'"
               " cannot be empty")))))

(defn site-test-folder [site-name]
  (format "test/%s" site-name))

(defn has-test-folder? [site-name]
  (some->
   site-name
   site-test-folder
   io/resource
   io/as-file
   .exists))

(defn error-must-have-test-data [site-name]
  (is false
      (str "There must be a site test data folder at "
           "`resources/test/" site-name "'")))

(defn testdef-form [site-name]
  `(deftest ~(symbol (str site-name "-test"))
     (expect-opts-are-map ~site-name)
     (if (has-test-folder? ~site-name)
       (do
         )
       (error-must-have-test-data ~site-name))))

(defmacro defsite-tests []
  (try
    (let [site-names (->> sites
                          (map first)
                          (filter (complement #{"test-site"})))]
      `(do ~@(map testdef-form site-names)))
    (catch RuntimeException e
      `(do))))

(defsite-tests)
