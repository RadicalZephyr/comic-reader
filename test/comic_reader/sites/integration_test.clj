(ns comic-reader.sites.integration-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.protocol  :refer :all]
            [comic-reader.sites.manga-fox :refer [manga-fox]]))

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

(deftest ^:integration get-chapter-list-test
  (let [chapters (get-chapter-list manga-fox "the_gamer")]
    (is (>= (count chapters)
            97))))
