(ns comic-reader.ui.comic-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.comic-list :as sut]
            [comic-reader.test-helper :refer [strip-classes]]))

(deftest test-comic-list
  (is (= [:div [:h1 "Comics"]]
         (sut/comic-list nil)))

  (is (= [:div [:h1 "Comics"]
          [base/loading]]
         (sut/comic-list :loading)))

  (is (= [:div [:h1 "Comics"]
          [:ul
           [:li [base/large-button "Comic A"]]
           [:li [base/large-button "Comic B"]]
           [:li [base/large-button "Comic C"]]]]
         (strip-classes
          (sut/comic-list [{:id :a :name "Comic A"}
                           {:id :b :name "Comic B"}
                           {:id :c :name "Comic C"}])))))

(deftest test-set-comic-list
  (is (= {:comic-list []}
         (sut/set-comic-list {} [:_ []])))

  (is (= {:comic-list [:a :b :c]}
         (sut/set-comic-list {} [:_ [:a :b :c]]))))

(deftest test-get-comic-list
  (is (= []
         (sut/get-comic-list {:comic-list []})))

  (is (= [:a :b :c]
         (sut/get-comic-list {:comic-list [:a :b :c]}))))

(sut/setup-comic-list!)

(defcard-rg comic-list
  (do
    (re-frame/dispatch [:set-comic-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
    [sut/comic-list-container]))
