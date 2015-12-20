(ns comic-reader.macro-util-test
  (:require [comic-reader.macro-util :as sut]
            [clojure.test :refer [deftest is]]
            [schema.core :as s]))

(def valid-spec?
  (comp not (s/checker sut/SubscriptionSpec)))

(deftest valid-spec?-test
  (is (not (valid-spec? nil)))
  (is (not (valid-spec? 10)))
  (is (not (valid-spec? [])))
  (is (not (valid-spec? ['a 'b 'c])))
  (is (not (valid-spec? [1 2])))
  (is (not (valid-spec? [[] 2])))
  (is (not (valid-spec? ['a []])))
  (is (not (valid-spec? ['a [:key] [:key]])))

  (is (valid-spec? 'abc))
  (is (valid-spec? 'def))
  (is (valid-spec? '[ghi :abc]))
  (is (valid-spec? '[ghi [:abc 1 2 3]])))

(deftest subscription-binding-test
  (is (thrown? clojure.lang.ExceptionInfo
               (sut/subscription-binding '(a b))))

  (is (= '[abc (re-frame.core/subscribe [:abc])]
         (sut/subscription-binding 'abc)))
  (is (= '[abc (re-frame.core/subscribe [:def])]
         (sut/subscription-binding '[abc :def]))))

(deftest container-name-test
  (is (= 'abc-container
         (sut/container-name 'abc)))
  (is (= 'abc-container
         (sut/container-name :abc))))
