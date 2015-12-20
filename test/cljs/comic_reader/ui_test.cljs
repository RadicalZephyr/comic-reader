(ns comic-reader.ui-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui :as ui]
            [comic-reader.test-helper :refer [strip-classes]]))

(defcard-rg loading-card
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   (ui/loading)])

(defcard-rg four-oh-four-card
  (ui/four-oh-four))

(deftest test-site-button
  (is (= [:li [:a "A"]]
         (strip-classes (ui/site-button {:name "A"}))))

  (is (= {:key "a"}
         (meta (ui/site-button {:id "a"})))))

(deftest test-site-list
  (is (= [:div [:h1 "Comic Sites"]]
         (ui/site-list nil)))

  (is (= [:div [:h1 "Comic Sites"]
          [ui/loading]]
         (ui/site-list :loading)))

  (is (= [:div [:h1 "Comic Sites"]
          [:ul
           [:li [:a "A"]]
           [:li [:a "B"]]
           [:li [:a "C"]]]]
         (strip-classes
          (ui/site-list [{:name "A" :id "a"}
                         {:name "B" :id "b"}
                         {:name "C" :id "c"}])))))

(deftest test-set-site-list
  (is (= (ui/set-site-list {} [:site-site-list []])
         {:site-list []})))

(defcard-rg site-list-card
  (do
    (re-frame/dispatch [:set-site-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
    (ui/site-list-container)))
