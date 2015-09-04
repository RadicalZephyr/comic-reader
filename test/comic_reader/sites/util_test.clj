(ns comic-reader.sites.util-test
  (:require [comic-reader.sites.util :refer :all]
            [clojure.test            :refer :all]))

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
