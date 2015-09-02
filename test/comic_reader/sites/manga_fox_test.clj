(ns comic-reader.sites.manga-fox-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.manga-fox :refer :all]
            [comic-reader.sites.protocol  :refer :all]
            [comic-reader.scrape :as scrape]
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

(deftest extract-chapters-list-test
  (let [html (html/html [:div {}])
        chapters (extract-chapters-list html "root.url")]
    (is (= (seq chapters)
           nil)))

  (let [html (html/html-resource "test/manga_fox/chapter_list.html")
        chapters (extract-chapters-list html "root.url")]
    (is (= (count chapters)
           97))
    (is (= (first chapters)
           {:name "The Gamer 96",
            :url "http://mangafox.me/manga/the_gamer/v02/c096/1.html",
            :ch-num 96}))))

(deftest extract-comics-list-test
  (let [html (html/html [:div {}])
        comics (extract-comics-list html)]
    (is (= (seq comics)
           nil)))

  (let [html (html/html-resource "test/manga_fox/comic_list.html")
        comics (extract-comics-list html)]
    (is (= (count comics)
           15))
    (is (= (first comics)
           {:name "-6mm no Taboo",
            :url "http://mangafox.me/manga/6mm_no_taboo/",
            :id "6mm_no_taboo"}))))

(deftest ^:integration get-comics-list-test
  (let [comics (get-comic-list manga-fox)]
    (is (> (count comics)
           15000))
    (let [comic-6mm {:name "-6mm no Taboo",
                     :url "http://mangafox.me/manga/6mm_no_taboo/",
                     :id "6mm_no_taboo"}]
      (is (some #{comic-6mm}
                comics)
          comic-6mm))))
