(ns comic-reader.sites.integration-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.protocol  :refer :all]
            [comic-reader.sites :refer :all]))

(def manga-fox (sites "manga-fox"))

(deftest ^:integration get-comics-list-test
  (let [comics (get-comic-list manga-fox)]
    (is (> (count comics)
           15000))
    (let [comic-6mm {:name "-6mm no Taboo",
                     :url "http://mangafox.me/manga/6mm_no_taboo/",
                     :id "6mm_no_taboo"}]
      (is (= (some #{comic-6mm}
                   comics)
             comic-6mm))))

  #_(let [comics (get-comic-list manga-reader)]
      (is (> (count comics)
             4000))
      (let [comic-mfm {:name "A Man for Megan",
                       :url "http://mangareader.net/a-man-for-megan",
                       :id "a-man-for-megan"}]
        (is (= (some #{comic-mfm}
                     comics)
               comic-mfm)))))

(deftest ^:integration get-chapter-list-test
  (let [chapters (get-chapter-list "the_gamer" manga-fox)]
    (is (>= (count chapters)
            97)))

  #_(let [chapters (get-chapter-list manga-reader "the-gamer")]
      (is (>= (count chapters)
              97))))

(deftest ^:integration get-page-list-test
  (let [pages (get-page-list manga-fox
                             {:name "The Gamer",
                              :url "http://mangafox.me/manga/the_gamer/v01/c001/1.html"
                              :id "the_gamer"})]
    (is (= (count pages)
           23)))

  #_(let [pages (get-page-list manga-reader {:name "The Gamer",
                                             :url "http://www.mangareader.net/the-gamer/1"
                                             :id "the-gamer"})]
      (is (= (count pages)
             23))))
