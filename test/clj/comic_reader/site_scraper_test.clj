(ns comic-reader.site-scraper-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [comic-reader.site-scraper :as sut])
  (:import comic_reader.sites.MangaSite))

(t/deftest make-site-entry-test
  (t/is (= (sut/make-site-entry "non-existent")
           ["non-existent" nil]))

  (let [test-file (io/as-file "resources/sites/test.site.edn")]
    (spit test-file "{}")
    (let [[label opts] (sut/make-site-entry "test")]
      (t/is (= label
               "test"))
      (t/is (= (class opts)
               MangaSite)))
    (io/delete-file test-file)))
