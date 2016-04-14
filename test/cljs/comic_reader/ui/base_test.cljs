(ns comic-reader.ui.base-test
  (:require [cljs.test :refer-macros [is testing]]
            [clojure.string :as str]
            [comic-reader.ui.base :as sut]
            [devcards.core :refer-macros [deftest defcard-rg]]))

(defcard-rg loading
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   [sut/loading]])

(defcard-rg four-oh-four
  [sut/four-oh-four])

(deftest test-map-into-list
  (is (= [:ul [:li 1] [:li 2]]
         (sut/map-into-list [:ul] identity identity [1 2])))

  (is (= [:ol.everything
          [:li [:a {:href "a"} "A"]]
          [:li [:a {:href "b"} "B"]]]
         (sut/map-into-list [:ol.everything]
                            :content
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

(deftest test-list-with-loading
  (is (= [:div [:h1 "Empty"]]
         (sut/list-with-loading {:heading "Empty"} nil)))

  (is (= [:div [:h1 "Loading"]
          [sut/loading]]
         (sut/list-with-loading {:heading "Loading"} :loading)))

  (is (= [:div [:h1 "Rendered List"]
          [:ul
           [:li "Thing One"]
           [:li "Thing Two"]]]
         (sut/list-with-loading {:heading "Rendered List"
                                 :list-element [:ul]
                                 :item->li identity}
                                ["Thing One" "Thing Two"])))

  (is (= [:div [:h1 "Rendered List"]
          [:ul.stuff
           [:li [:a.thing "Thing One"]]
           [:li [:a.thing "Thing Two"]]]]
         (sut/list-with-loading {:heading "Rendered List"
                                 :list-element [:ul.stuff]
                                 :item->li (fn [el] [:a.thing el])}
                                ["Thing One" "Thing Two"]))))

(deftest test-unique-class
  (is (keyword? (sut/unique-class :div "overlay")))

  (is (str/starts-with?
       (name (sut/unique-class :div "overlay"))
       "div.overlay"))

  (is (str/starts-with?
       (name (sut/unique-class "nothing"))
       ".nothing"))

  (is (not= (sut/unique-class "nothing")
            (sut/unique-class "nothing"))))
