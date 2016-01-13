(ns comic-reader.ui.comic-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.comic-list :as sut]))

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

(defcard-rg comic-list
  [sut/comic-list [{:id :a :name "Comic A"}
                   {:id :b :name "Comic B"}]])

(defcard-rg letter-filter
  "Individual letter-filters"
  [:ul.sub-nav.no-bullet
   [sut/letter-filter identity "A" "B"]
   [sut/letter-filter identity "B" "B"]
   [sut/letter-filter identity "C" ""]])

(defn alphabet-card-container [initial-letter]
  (let [search-prefix (reagent/atom initial-letter)
        make-set-prefix (fn [letter]
                          (fn []
                            (reset! search-prefix letter)))]
    (fn [initial-letter]
      [:div [:h5 (str "Prefix: " @search-prefix)]
       [sut/alphabet-letter-filters
        make-set-prefix @search-prefix]])))

(defcard-rg alphabet-letter-filters
  "The letter \"J\" should start highlighted below"
  [alphabet-card-container "J"])

(defcard-rg search-box
  [:div
   [sut/search-box ""]
   [sut/search-box "Current search"]])

(defcard-rg comic-list-filter
  [sut/comic-list-filter ""])
