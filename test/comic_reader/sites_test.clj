(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [comic-reader.scrape :as scrape]
            [net.cgrand.enlive-html :as html]
            [clj-http.client :as client]))

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

(defmacro test-data-functions-not-nil []
  `(are [selector] (is (not= (selector)
                             nil))
     root-url
     manga-list-format
     manga-url-format
     manga-pattern-match-portion

     comic->url-format

     chapter-list-selector
     comic-list-selector
     image-selector
     page-list-selector

     chapter-number-pattern
     chapter-number-match-pattern

     link-name-normalize
     link-url-normalize

     page-normalize-format
     page-normalize-pattern))

(defmacro test-url-format-strings []
  `(are [url-fn] (is (= (:status (client/head (url-fn)))
                        200)
                     (str (url-fn) "does not appear to exist."))
     root-url
     manga-url
     manga-list-url))
