(ns comic-reader.ui-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui :as ui]
            [comic-reader.test-helper :refer [strip-classes]]))

(defcard-rg loading
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   (ui/loading)])

(defcard-rg four-oh-four
  (ui/four-oh-four))

(defcard-rg large-button
  (ui/large-button "Button"))

(deftest test-site-list
  (is (= [:div [:h1 "Comic Sites"]]
         (ui/site-list nil)))

  (is (= [:div [:h1 "Comic Sites"]
          [ui/loading]]
         (ui/site-list :loading)))

  (is (= [:div [:h1 "Comic Sites"]
          [:ul
           [:li [ui/large-button "A"]]
           [:li [ui/large-button "B"]]
           [:li [ui/large-button "C"]]]]
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
    (ui/site-list-container)))
