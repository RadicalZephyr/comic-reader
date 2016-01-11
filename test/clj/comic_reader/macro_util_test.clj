(ns comic-reader.macro-util-test
  (:require [comic-reader.macro-util :as sut]
            [clojure.test :as t]
            [schema.core :as s]))

(def valid-spec?
  (comp not (s/checker sut/SubscriptionSpec)))

(t/deftest valid-spec?-test
  (t/is (not (valid-spec? nil)))
  (t/is (not (valid-spec? 10)))
  (t/is (not (valid-spec? [])))
  (t/is (not (valid-spec? ['a 'b 'c])))
  (t/is (not (valid-spec? [1 2])))
  (t/is (not (valid-spec? [[] 2])))
  (t/is (not (valid-spec? ['a []])))
  (t/is (not (valid-spec? ['a [:key] [:key]])))

  (t/is (valid-spec? 'abc))
  (t/is (valid-spec? 'def))
  (t/is (valid-spec? '[ghi :abc]))
  (t/is (valid-spec? '[ghi [:abc 1 2 3]])))

(t/deftest subscription-binding-test
  (t/is (thrown? clojure.lang.ExceptionInfo
                 (sut/subscription-binding '(a b))))

  (t/is (= '[abc (re-frame.core/subscribe [:abc])]
           (sut/subscription-binding 'abc)))
  (t/is (= '[abc (re-frame.core/subscribe [:def])]
           (sut/subscription-binding '[abc :def]))))

(t/deftest container-name-test
  (t/is (= 'abc-container
           (sut/container-name 'abc)))
  (t/is (= 'abc-container
           (sut/container-name :abc))))
