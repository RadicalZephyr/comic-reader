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
