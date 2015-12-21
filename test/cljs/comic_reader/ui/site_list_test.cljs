(ns comic-reader.ui.site-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.site-list :as sut]
            [comic-reader.test-helper :refer [strip-classes]]))

(deftest test-site-list
  (is (= [:div [:h1 "Comic Sites"]]
         (sut/site-list nil)))

  (is (= [:div [:h1 "Comic Sites"]
          [base/loading]]
         (sut/site-list :loading)))

  (is (= [:div [:h1 "Comic Sites"]
          [:ul
           [:li [base/large-button "A"]]
           [:li [base/large-button "B"]]
           [:li [base/large-button "C"]]]]
         (strip-classes
          (sut/site-list [{:name "A" :id "a"}
                         {:name "B" :id "b"}
                         {:name "C" :id "c"}])))))

(deftest test-set-site-list
  (is (= {:site-list []}
         (sut/set-site-list {} [:_ []])))

  (is (= {:site-list [1 2 3]}
         (sut/set-site-list {} [:_ [1 2 3]]))))

(deftest test-get-site-list
  (is (= []
         (sut/get-site-list {:site-list []})))

  (is (= [:a :b :c]
         (sut/get-site-list {:site-list [:a :b :c]}))))

(sut/setup-site-list!)

(defcard-rg site-list
  (do
    (re-frame/dispatch [:set-site-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
    [sut/site-list-container]))
