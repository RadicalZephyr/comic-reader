(ns comic-reader.ui.site-list-test
  (:require [cljs.test :refer-macros [async is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.site-list :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

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

(defcard-rg test-get-set-wiring
  (fn [_ _]
    (let [sites-list [:a :b :c]]
      (sut/setup!)
      (sut/set sites-list)
      (reactively
       [:div
        [:p (prn-str @(sut/get))
         "should be equal to "
         (prn-str sites-list)]]))))

(defcard-rg site-list
  (fn [data _]
    (let [view-site (fn [site-id]
                      (base/do-later
                       #(swap! data assoc
                               :site site-id)))]
      [sut/site-list view-site [{:site/id :a :site/name "Comic A"}
                                {:site/id :b :site/name "Comic B"}
                                {:site/id :c :site/name "Comic C"}]]))
  (reagent/atom {:site nil})
  {:inspect-data true})
