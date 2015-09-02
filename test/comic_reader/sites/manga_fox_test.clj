(ns comic-reader.sites.manga-fox-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.manga-fox :refer :all]
            [net.cgrand.enlive-html :as html]))

(deftest extract-image-tag-test
  (is (= (extract-image-tag (html/html [:div {}]))
         nil))

  (let [html (html/html-resource "test/manga_fox/image.html")]
    (is (= (extract-image-tag html)
           [:img {:src "http://a.mfcdn.net/store/manga/13088/02-095.0/compressed/m001.jpg"
                  :alt "The Gamer 95 at MangaFox.me"}]))))

(deftest extract-pages-list-test
  (let [html (html/html [:div {} [:img {}]])
        pages (extract-pages-list html "root.url")]
    (is (= (seq pages)
           nil)))

  (let [html (html/html-resource "test/manga_fox/image.html")
        pages (extract-pages-list html "root.url")]
    (is (= (count pages)
           24))
    (is (= (first pages)
           {:name "1" :url "root.url/1.html"}))))
