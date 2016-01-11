(ns comic-reader.sites.util-test
  (:require [comic-reader.sites.util :as sut]
            [clojure.test            :as t]))

(t/deftest walk-for-symbols-test
  (t/is (= (sut/walk-for-symbols '{:a 1})
           []))

  (t/is (= (sut/walk-for-symbols '{a 1})
           '[a]))

  (t/is (= (sut/walk-for-symbols '{{name :url} :attrs})
           '[name]))

  (t/is (= (sut/walk-for-symbols '{{{:other symbol} :url} :attrs 1 sym})
           '[symbol sym])))

(t/deftest html-fn-test
  (t/is (= ((sut/html-fn {[name] :content} name)
            {:content ["stuff"]})
           "stuff"))

  (t/is (= ((sut/html-fn {{val :val} :attrs} [val])
            {:content ["stuff"]})
           nil)))

(t/deftest gen-link->map-test
  (t/is (= ((sut/gen-link->map identity identity) {})
           nil))

  (let [link {:attrs {:href 'things} :content ['n 'stuff]}]
    (t/is (= ((sut/gen-link->map identity identity) link)
             {:name ['n 'stuff]
              :url  'things}))

    (t/is (= ((sut/gen-link->map str str) link)
             {:name "[n stuff]"
              :url "things"}))))

(t/deftest gen-add-key-from-url-test
  (t/is (= ((sut/gen-add-key-from-url :foo #"^(.*)$") {:url "bar"})
           {:url "bar"
            :foo "bar"}))

  (t/is (= ((sut/gen-add-key-from-url :foo #"^(.+?)") {:url "bar"})
           {:url "bar"
            :foo "b"}))

  (t/is (= ((sut/gen-add-key-from-url :foo #"(ar)") {:url "bar"
                                                     :all 'other
                                                     :keys 'are
                                                     :still 'present})
           {:url "bar"
            :foo "ar"
            :all 'other
            :keys 'are
            :still 'present})))

(t/deftest symbolize-keys-test
  (t/is (= (sut/symbolize-keys {:foo 1 :bar 2})
           '{foo 1 bar 2}))

  (t/is (= (sut/symbolize-keys {:foo 2 :bar 1})
           '{foo 2 bar 1}))

  (t/is (= (sut/symbolize-keys {"foo" 2 "bar" 1})
           '{foo 2 bar 1}))

  (t/is (= (sut/symbolize-keys '{foo 2 bar 1})
           '{foo 2 bar 1})))
