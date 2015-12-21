(ns comic-reader.ui.site-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.site-list :as ui]
            [comic-reader.test-helper :refer [strip-classes]]))

(deftest test-site-list
  (is (= [:div [:h1 "Comic Sites"]]
         (ui/site-list nil)))

  (is (= [:div [:h1 "Comic Sites"]
          [base/loading]]
         (ui/site-list :loading)))

  (is (= [:div [:h1 "Comic Sites"]
          [:ul
           [:li [base/large-button "A"]]
           [:li [base/large-button "B"]]
           [:li [base/large-button "C"]]]]
         (strip-classes
          (ui/site-list [{:name "A" :id "a"}
                         {:name "B" :id "b"}
                         {:name "C" :id "c"}])))))

(deftest test-set-site-list
  (is (= (ui/set-site-list {} [:site-site-list []])
         {:site-list []})))

(defcard-rg site-list
  (do
    (re-frame/dispatch [:set-site-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
    [ui/site-list-container]))
