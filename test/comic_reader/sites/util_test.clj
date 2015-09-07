(ns comic-reader.sites.util-test
  (:require [comic-reader.sites.util :refer :all]
            [clojure.test            :refer :all]))

(deftest walk-for-symbols-test
  (is (= (walk-for-symbols '{:a 1})
         []))

  (is (= (walk-for-symbols '{a 1})
         '[a]))

  (is (= (walk-for-symbols '{{name :url} :attrs})
         '[name]))

  (is (= (walk-for-symbols '{{{:other symbol} :url} :attrs 1 sym})
         '[symbol sym])))

(deftest html-fn-test
  (is (= ((html-fn {[name] :content} name)
          {:content ["stuff"]})
         "stuff"))

  (is (= ((html-fn {{val :val} :attrs} [val])
          {:content ["stuff"]})
         nil)))

(deftest gen-link->map-test
  (is (= ((gen-link->map identity identity) {})
         nil))

  (let [link {:attrs {:href 'things} :content ['n 'stuff]}]
    (is (= ((gen-link->map identity identity) link)
           {:name ['n 'stuff]
            :url  'things}))

    (is (= ((gen-link->map str str) link)
           {:name "[n stuff]"
            :url "things"}))))

(deftest gen-add-key-from-url-test
  (is (= ((gen-add-key-from-url :foo #"^(.*)$") {:url "bar"})
         {:url "bar"
          :foo "bar"}))

  (is (= ((gen-add-key-from-url :foo #"^(.+?)") {:url "bar"})
         {:url "bar"
          :foo "b"}))

  (is (= ((gen-add-key-from-url :foo #"(ar)") {:url "bar"
                                               :all 'other
                                               :keys 'are
                                               :still 'present})
         {:url "bar"
          :foo "ar"
          :all 'other
          :keys 'are
          :still 'present})))

(deftest symbolize-keys-test
  (is (= (symbolize-keys {:foo 1 :bar 2})
         '{foo 1 bar 2}))

  (is (= (symbolize-keys {:foo 2 :bar 1})
         '{foo 2 bar 1}))

  (is (= (symbolize-keys {"foo" 2 "bar" 1})
         '{foo 2 bar 1}))

  (is (= (symbolize-keys '{foo 2 bar 1})
         '{foo 2 bar 1})))
