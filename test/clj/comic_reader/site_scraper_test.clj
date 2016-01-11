(ns comic-reader.site-scraper-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [comic-reader.site-scraper :refer :all])
  (:import comic_reader.sites.MangaSite))

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         ["non-existent" nil]))

  (let [test-file (io/as-file "resources/sites/test-site.clj")]
    (spit test-file "{}")
    (let [[label opts] (make-site-entry "test-site")]
      (is (= label
             "test-site"))
      (is (= (class opts)
             MangaSite)))
    (io/delete-file test-file)))
