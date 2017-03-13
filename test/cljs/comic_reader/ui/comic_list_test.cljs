(ns comic-reader.ui.comic-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.comic-list :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

(deftest test-get
  (is (= []
         (sut/get* {:comic-list []})))

  (is (= [:a :b :c]
         (sut/get* {:comic-list [:a :b :c]}))))

(deftest test-set
  (is (= {:comic-list []}
         (sut/set* {} [])))

  (is (= {:comic-list [:a :b :c]}
         (sut/set* {} [:a :b :c]))))

(deftest test-prefix-filter-comics
  (is (= [{:comic/name "A"}]
         (sut/prefix-filter-comics "A" [{:comic/name "A"}
                                        {:comic/name "B"}
                                        {:comic/name "C"}])))
  (is (= [{:comic/name "1"} {:comic/name "\""}]
         (sut/prefix-filter-comics "#" [{:comic/name "1"}
                                        {:comic/name "B"}
                                        {:comic/name "\""}
                                        {:comic/name "C"}]))))

(defcard-rg test-get-set-wiring
  (fn [_ _]
    (let [comics-list [:a :b :c :d]]
      (sut/setup!)
      (sut/set comics-list)
      (reactively
       [:div
        [:p (prn-str @(sut/get))
         "should be equal to "
         (prn-str comics-list)]]))))

(defcard-rg comic-list
  (fn [data _]
    (let [view-comic (fn [comic-id]
                       (base/do-later
                        #(swap! data assoc
                                :comic comic-id)))]
      [sut/comic-list
       view-comic
       [{:comic/id :a :comic/name "Comic A"}
        {:comic/id :b :comic/name "Comic B"}]]))
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
                            (base/do-later
                             #(swap! data assoc
                                     :search-prefix letter)))]
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

(defcard-rg comic-page
  (fn [data _]
    (let [comics [{:comic/id :a :comic/name "A"}
                  {:comic/id :b :comic/name "B"}
                  {:comic/id :c :comic/name "C"}]
          view-comic (fn [comic-id]
                       (base/do-later
                        #(swap! data assoc
                                :comic-to-view comic-id)))
          update-search-prefix (fn [prefix & {:keys [clear]
                                              :or {:clear false}}]
                                 (base/do-later
                                  #(swap! data assoc
                                          :search-data {:search-prefix prefix
                                                        :clear clear})))]
      (reactively
       [sut/comic-page view-comic comics update-search-prefix (:search-data @data)])))
  (reagent/atom {:search-data {:search-prefix "Initial search"}})
  {:inspect-data true})
