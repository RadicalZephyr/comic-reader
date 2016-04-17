(ns comic-reader.ui.site-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.site-list :as sut]))

(deftest test-set
  (is (= {:site-list []}
         (sut/set* {} [])))

  (is (= {:site-list [1 2 3]}
         (sut/set* {} [1 2 3]))))

(deftest test-get
  (is (= []
         (sut/get* {:site-list []})))

  (is (= [:a :b :c]
         (sut/get* {:site-list [:a :b :c]}))))

(deftest test-get-set-wiring
  (sut/setup!)
  (let [sites-list [:a :b :c]]
    (sut/set sites-list)
    (is (= sites-list
           @(sut/get)))))

(defcard-rg site-list
  (fn [data _]
    (let [view-site (fn [site-id]
                      (base/do-later
                       #(swap! data assoc
                               :site site-id)))]
      [sut/site-list view-site [{:id :a :name "Comic A"}
                                {:id :b :name "Comic B"}
                                {:id :c :name "Comic C"}]]))
  (reagent/atom {:site nil})
  {:inspect-data true})
