(ns comic-reader.ui-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui :as ui]
            [comic-reader.main-test]))

(defcard-rg loading-card
  "## Loading
   This is the loading svg used everywhere on the site."
  (ui/loading))

(deftest test-site-element
  (is (= [:li [:a "A"]]
         (ui/site-element {:name "A"})))

  (is (= {:key "a"}
         (meta (ui/site-element {:id "a"})))))

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
         (ui/site-list [{:name "A" :id "a"}
                        {:name "B" :id "b"}
                        {:name "C" :id "c"}]))))

(defcard-rg site-list-card
  (do
    (re-frame/dispatch [:set-site-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
     (ui/site-list-container)))
