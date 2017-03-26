(ns comic-reader.ui.reader-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.reader :as sut]))

(deftest test-partitioned-locations
  (testing "handles no data gracefully"
    (is (= []
           (sut/partitioned-locations [] nil)))

    (is (= []
           (sut/partitioned-locations nil nil)))

    (is (= []
           (sut/partitioned-locations nil :location/abc))))

  (testing "partitions based on the given image location"
    (is (= [[1] [:a] [2]]
           (sut/partitioned-locations [1 :a 2] :a)))

    (is (= [[1] [:a] [2 3 4]]
           (sut/partitioned-locations [1 :a 2 3 4] :a)))

    (is (= [[-1 0 1] [:a] [2]]
           (sut/partitioned-locations [-1 0 1 :a 2] :a))))

  (testing "returns meaningful partitions when there are no preceding items"
    (is (= [[] [:a] [2]]
           (sut/partitioned-locations [:a 2] :a))))

  (testing "returns meaningful partitions when there are no following items"
    (is (= [[1 2] [:a] []]
           (sut/partitioned-locations [1 2 :a] :a)))))

(deftest test-current-locations
  (testing "can handle having no images"
    (is (= []
           (sut/current-locations []))))

  (testing "can handle not having any previous items"
    (is (= [:current 2 3]
           (sut/current-locations [[] [:current] [2 3]]))))

  (testing "can handle not having any following items"
    (is (= [1 2 :current]
           (sut/current-locations [[1 2] [:current] []]))))

  (testing "selects 2 before and 2 after the current location"
    (is (= [3 4 :a 5 6]
           (sut/current-locations [[1 2 3 4] [:a] [5 6 7 8]])))))
