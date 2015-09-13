(ns comic-reader.sites.test-util
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [clj-http.client :as client]
            [loom.graph :as graph]
            [loom.alg :as alg]))

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

(def dependecy-dag
  (graph/digraph
   [:get-image-data :extract-image-tag]

   [:get-page-list :extract-pages-list]

   [:get-chapter-list :comic->url]
   [:get-chapter-list :extract-chapters-list]

   [:get-comic-list :manga-list-url]
   [:get-comic-list :extract-comics-list]

   [:extract-image-tag :image-selector]

   [:extract-pages-list :chapter-number-pattern]
   [:extract-pages-list :page-list-selector]
   [:extract-pages-list :gen-extract-pages-list-normalize]

   [:gen-extract-pages-list-normalize :page-normalize-pattern]
   [:gen-extract-pages-list-normalize :page-normalize-format]

   [:comic->url :comic->url-format]
   [:comic->url :manga-url]

   [:extract-chapters-list :chapter-list-selector]
   [:extract-chapters-list :chapter-link-normalize]

   [:chapter-link-normalize :link->map]
   [:chapter-link-normalize :chapter-link-add-ch-num]

   [:chapter-link-add-ch-num :chapter-number-match-pattern]

   [:extract-comics-list :comic-list-selector]
   [:extract-comics-list :comic-link-normalize]

   [:comic-link-normalize :link->map]
   [:comic-link-normalize :comic-link-add-id]

   [:comic-link-add-id :manga-pattern]

   [:link->map :link-name-normalize]
   [:link->map :link-url-normalize]

   [:manga-pattern :manga-url]
   [:manga-pattern :manga-pattern-match-portion]

   [:manga-list-url :root-url]
   [:manga-list-url :manga-list-format]

   [:manga-url :root-url]
   [:manga-url :manga-url-format]
   ))

(def data-function?
  #{:root-url
    :manga-list-format
    :manga-url-format
    :manga-pattern-match-portion

    :comic->url-format

    :chapter-list-selector
    :comic-list-selector
    :image-selector
    :page-list-selector

    :chapter-number-pattern
    :chapter-number-match-pattern

    :link-name-normalize
    :link-url-normalize

    :page-normalize-format
    :page-normalize-pattern})

(defmacro ensure-dependencies-defined [fn-name]
  `(are [selector] (is (not= (selector)
                             nil))
     ~@(->> (keyword fn-name)
            (alg/topsort dependecy-dag)
            (filter data-function?)
            (map (comp symbol name)))))
