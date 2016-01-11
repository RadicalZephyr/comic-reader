(ns comic-reader.util-test
  (:require [comic-reader.util :as sut]
            [clojure.test :as t]))

(t/deftest keyword->words-test
  (t/is (= (sut/keyword->words :manga-fox)
           ["manga" "fox"]))
  (t/is (= (sut/keyword->words :thingy-doer)
           ["thingy" "doer"])))

(t/deftest keyword->title-test
  (t/is (= (sut/keyword->title :manga-fox)
           "Manga Fox"))
  (t/is (= (sut/keyword->title :manga-reader)
           "Manga Reader")))
