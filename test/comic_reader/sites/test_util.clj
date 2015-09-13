(ns comic-reader.sites.test-util
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [clj-http.client :as client]))

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
