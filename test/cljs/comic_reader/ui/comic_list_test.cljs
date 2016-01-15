(ns comic-reader.ui.comic-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.comic-list :as sut])
  (:require-macros [comic-reader.macro-util :refer [reactively]]))

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
  (fn [data _]
    (let [view-comic (fn [comic-id]
                       (base/do-later
                        #(swap! data assoc
                                :comic comic-id)))]
      [sut/comic-list
       view-comic
       [{:id :a :name "Comic A"}
        {:id :b :name "Comic B"}]]))
  (reagent/atom {:comic nil})
  {:inspect-data true})

(defcard-rg letter-filter
  "Individual letter-filters"
  [:ul.sub-nav.no-bullet
   [sut/letter-filter identity "A" "B"]
   [sut/letter-filter identity "B" "B"]
   [sut/letter-filter identity "C" ""]])

(defcard-rg alphabet-letter-filters
  "The letter \"J\" should start highlighted below"
  (fn [data _]
    (let [make-set-prefix (fn [letter]
                            (fn []
                              (base/do-later
                               #(swap! data assoc
                                       :search-prefix letter))))]
      (reactively
       [:div [:h5 (str "Prefix: " (:search-prefix @data))]
        [sut/alphabet-letter-filters
         make-set-prefix (:search-prefix @data)]])))
  (reagent/atom {:search-prefix "J"})
  {:inspect-data true})

(defcard-rg search-box
  [:div
   [sut/search-box identity "" false]
   [sut/search-box identity "Current search" false]])

(defcard-rg comic-list-filter
  (fn [data _]
    (let [update-search-prefix (fn [prefix & {:keys [clear]
                                              :or {:clear false}}]
                                 (base/do-later
                                  #(swap! data assoc
                                          :search-prefix prefix
                                          :clear clear)))]
      (reactively
       [sut/comic-list-filter update-search-prefix @data])))
  (reagent/atom {:search-prefix "Initial search"})
  {:inspect-data true})
