(ns comic-reader.ui.comic-list-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
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

(sut/setup-comic-list!)

(defcard-rg comic-list
  (do
    (re-frame/dispatch [:set-comic-list
                        [{:id :a :name "Comic A"}
                         {:id :b :name "Comic B"}]])
    [sut/comic-list-container]))

(defcard-rg letter-filter
  [:ul.sub-nav.no-bullet
   [sut/letter-filter "A" "B"]
   [sut/letter-filter "B" "B"]
   [sut/letter-filter "C" ""]])
