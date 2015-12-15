(ns comic-reader.util-test
  (:require [comic-reader.util :as sut]
            [clojure.test :refer [deftest is]]))

(deftest keyword->words-test
  (is (= (sut/keyword->words :manga-fox)
         ["manga" "fox"]))
  (is (= (sut/keyword->words :thingy-doer)
         ["thingy" "doer"])))

(deftest keyword->title-test
  (is (= (sut/keyword->title :manga-fox)
         "Manga Fox"))
  (is (= (sut/keyword->title :manga-reader)
         "Manga Reader")))
