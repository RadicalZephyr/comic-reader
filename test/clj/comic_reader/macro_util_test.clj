(ns comic-reader.macro-util-test
  (:require [comic-reader.macro-util :as sut]
            [clojure.test :refer [deftest is]]))

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
