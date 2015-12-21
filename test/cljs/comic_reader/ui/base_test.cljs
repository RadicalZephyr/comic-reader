(ns comic-reader.ui.base-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [comic-reader.ui.base :as sut]))

(defcard-rg loading
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   [sut/loading]])

(defcard-rg four-oh-four
  [sut/four-oh-four])

(defcard-rg large-button
  [sut/large-button "Button"])

(deftest test-map-into-list
  (is (= [:ul [:li 1] [:li 2]]
         (sut/map-into-list [:ul] identity [1 2])))

  (is (= [:ol.everything
          [:li [:a {:href "a"} "A"]]
          [:li [:a {:href "b"} "B"]]]
         (sut/map-into-list [:ol.everything]
                            (fn [el]
                              [:a {:href (:url el)}
                               (:content el)])
                            [{:url "a" :content "A"}
                             {:url "b" :content "B"}]))))

(deftest test-with-optional-tail
  (is (= [:div]
         (sut/with-optional-tail [:div] nil)))

  (is (= [:div [1 2 3]]
         (sut/with-optional-tail [:div] [1 2 3]))))
