(ns comic-reader.macro-util-test
  (:require [comic-reader.macro-util :as sut]
            [clojure.test :refer [deftest is]]))

(deftest valid-spec?-test
  (is (not (sut/valid-spec? nil)))
  (is (not (sut/valid-spec? 10)))
  (is (not (sut/valid-spec? [])))
  (is (not (sut/valid-spec? ['a 'b 'c])))
  (is (not (sut/valid-spec? [1 2])))
  (is (not (sut/valid-spec? [[] 2])))
  (is (not (sut/valid-spec? ['a []])))

  (is (sut/valid-spec? 'abc))
  (is (sut/valid-spec? 'def))
  (is (sut/valid-spec? '[ghi [:abc]])))

(deftest subscription-binding-test
  (is (thrown? clojure.lang.ExceptionInfo
               (sut/subscription-binding '(a b))))

  (is (= '[abc (re-frame.core/subscribe [:abc])]
         (sut/subscription-binding 'abc)))
  (is (= '[abc (re-frame.core/subscribe [:def])]
         (sut/subscription-binding '[abc [:def]]))))

(deftest container-name-test
  (is (= 'abc-container
         (sut/container-name 'abc)))
  (is (= 'abc-container
         (sut/container-name :abc))))
