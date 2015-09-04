(ns comic-reader.sites.manga-reader-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.manga-reader :refer :all]
            [comic-reader.sites.protocol     :refer :all]
            [comic-reader.scrape :as scrape]
            [net.cgrand.enlive-html :as html]))

(deftest extract-image-tag-test
  (is (= (extract-image-tag (html/html [:div {}]))
         nil))

  (let [html (html/html-resource "test/manga_reader/image.html")]
    (is (= (extract-image-tag html)
           [:img {:src "http://i995.mangareader.net/the-gamer/95/the-gamer-5978527.jpg"
                  :alt "The Gamer 95 - Page 1"}]))))

(deftest extract-pages-list-test
  (let [html (html/html [:div {} [:img {}]])
        pages (extract-pages-list html "root.url")]
    (is (= (seq pages)
           nil)))

  (let [html (html/html-resource "test/manga_reader/image.html")
        pages (extract-pages-list html "root.url")]
    (is (= (count pages)
           24))
    (is (= (first pages)
           {:name "1" :url "root.url/1"}))))
