(ns comic-reader.util-test
  (:require [comic-reader.util :refer :all]
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

(deftest subscription-binding-test
  (is (thrown? clojure.lang.ExceptionInfo
               (subscription-binding '(a b))))

  (is (= '[abc (re-frame.core/subscribe [:abc])]
         (subscription-binding 'abc)))
  (is (= '[abc (re-frame.core/subscribe [:def])]
         (subscription-binding '[abc [:def]]))))

(deftest container-name-test
  (is (= 'abc-container
         (container-name 'abc)))
  (is (= 'abc-container
         (container-name :abc))))
