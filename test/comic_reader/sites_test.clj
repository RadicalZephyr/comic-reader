(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites :refer :all]
            [comic-reader.scrape :as scrape]
            [net.cgrand.enlive-html :as html]))

(defn extract-comics-list-test []
  (binding [options (read-site-options "manga-fox")]
    (is (not= (comic-list-selector)
              nil))

    (let [html (html/html [:div {}])
          comics (extract-comics-list html)]
      (is (= (seq comics)
             nil)))

    (let [html (html/html-resource "test/manga_fox/comic_list.html")
          url-list (scrape/extract-list html
                                        (comic-list-selector)
                                        identity)
          comics (extract-comics-list html)]
      (is (= (count url-list)
             15))

      (is (= (first url-list)
             '{:tag :a,
               :attrs
               {:href "http://mangafox.me/manga/6mm_no_taboo/",
                :rel "4558",
                :class "series_preview manga_open"},
               :content ("-6mm no Taboo")}))

      (is (= clojure.core/first (link-name-normalize)))

      (is (= (link->map '{:tag :a,
                          :attrs
                          {:href "http://mangafox.me/manga/6mm_no_taboo/",
                           :rel "4558",
                           :class "series_preview manga_open"},
                          :content ("-6mm no Taboo")})
             '{:name "-6mm no Taboo"
               :url  "http://mangafox.me/manga/6mm_no_taboo/"}))

      (is (= (comic-link-normalize '{:tag :a,
                                     :attrs
                                     {:href "http://mangafox.me/manga/6mm_no_taboo/",
                                      :rel "4558",
                                      :class "series_preview manga_open"},
                                     :content ("-6mm no Taboo")})
             '{:name "-6mm no Taboo",
               :url "http://mangafox.me/manga/6mm_no_taboo/",
               :id "6mm_no_taboo"}))

      (is (= (count comics)
             15))
      (is (= (first comics)
             {:name "-6mm no Taboo",
              :url "http://mangafox.me/manga/6mm_no_taboo/",
              :id "6mm_no_taboo"})))))

(deftest unit-tests
  (extract-comics-list-test))
