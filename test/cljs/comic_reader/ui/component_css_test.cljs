(ns comic-reader.ui.component-css-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [garden.core :as g]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.component-css :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]
            [clojure.string :as str]))

(deftest test-get
  (is (= []
         (sut/get* {:component-css []})))

  (is (= [:a :b :c]
         (sut/get* {:component-css [:a :b :c]}))))

(deftest test-set
  (is (= {:component-css {:id1 {:a 1}}}
         (sut/merge* {} :id1 {:a 1})))

  (is (= {:component-css {:a 1 :b 2}}
         (sut/merge* {:component-css {:b 2}} :a 1)))

  (is (= {:component-css {:first "worked"}}
         (sut/merge* {} :first "worked"))))

(defcard-rg display-css
  (let [pre-attrs {:style {:border "1px solid #aaa" :padding "10px"}}]
    (sut/setup!)
    (reactively
     [:div
      [:button {:on-click #(sut/merge (gensym) [:a {:color "blue"}])} "Add a blue style"]
      [:p "Raw data structure: "]
      [:pre pre-attrs (prn-str (sut/component-garden-css))]

      [:p "Rendered css:"]
      [:pre pre-attrs
       (let [css-hiccup (sut/component-css)]
         (str "<style>\n"
              (second css-hiccup)
              "\n</style>"))]])))
