(ns comic-reader.utils-test
  (:require [comic-reader.utils :refer :all]
            [clojure.test :refer :all]))

(deftest keyword->words-test
  (is (= (keyword->words :manga-fox)
         ["manga" "fox"]))
  (is (= (keyword->words :thingy-doer)
         ["thingy" "doer"])))

(deftest keyword->title-test
  (is (= (keyword->title :manga-fox)
         "Manga Fox"))
  (is (= (keyword->title :manga-reader)
         "Manga Reader")))
