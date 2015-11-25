(ns comic-reader.site-scraper-test
  (:require [clojure.test :refer :all]
            [comic-reader.site-scraper :refer :all])
  (:import comic_reader.sites.MangaSite))

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         ["non-existent" nil]))

  (let [[label opts] (make-site-entry "test-site")]
    (is (= label
           "test-site"))
    (is (= (class opts)
           MangaSite))))
