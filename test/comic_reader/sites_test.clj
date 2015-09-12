(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [comic-reader.scrape :as scrape]
            [net.cgrand.enlive-html :as html]
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

(deftest extract-comics-list-test
  (binding [options (read-site-options "manga-fox")]
    (test-data-functions-not-nil)
    (test-url-format-strings)

    (let [html (html/html [:div {}])
          comics (extract-comics-list html)]
      (is (= (seq comics)
             nil)))

    (let [html (html/html-resource "test/manga_fox/comic_list.html")]
      (let [url-list (scrape/extract-list html
                                          (comic-list-selector)
                                          identity)]
        (is (= (count url-list)
               15))

        (is (= (first url-list)
               '{:tag :a,
                 :attrs
                 {:href "http://mangafox.me/manga/6mm_no_taboo/",
                  :rel "4558",
                  :class "series_preview manga_open"},
                 :content ("-6mm no Taboo")})))

      (let [link-6mm '{:tag :a,
                       :attrs
                       {:href "http://mangafox.me/manga/6mm_no_taboo/",
                        :rel "4558",
                        :class "series_preview manga_open"},
                       :content ("-6mm no Taboo")}]

        (is (= (link->map link-6mm)
               '{:name "-6mm no Taboo"
                 :url  "http://mangafox.me/manga/6mm_no_taboo/"}))

        (is (= (comic-link-normalize link-6mm)
               '{:name "-6mm no Taboo",
                 :url "http://mangafox.me/manga/6mm_no_taboo/",
                 :id "6mm_no_taboo"})))

      (let [comics (extract-comics-list html)]
        (is (= (count comics)
               15))
        (is (= (first comics)
               {:name "-6mm no Taboo",
                :url "http://mangafox.me/manga/6mm_no_taboo/",
                :id "6mm_no_taboo"}))))))
