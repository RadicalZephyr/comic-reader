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

(deftest test-api-calls-for-location
  (testing "when first and last markers are present in locations list no api calls are made"
    (let [db {:locations #{{:location/boundary :boundary/first}
                           {:location/boundary :boundary/last}}}]
      (is (= []
             (sut/api-calls-for-location db [])))))

  (let [site-id :site/id
        comic-id :comic/id
        current-location :current/location
        buffer-size 10
        db {:site-id site-id
            :comic-id comic-id
            :locations #{current-location}
            :buffer-size buffer-size}]

    (testing "when first is present no prev-locations call is issued"
      (let [db (update db :locations conj {:location/boundary :boundary/first})]
        (is (= [[:get-next-locations site-id comic-id current-location buffer-size]]
               (map butlast (sut/api-calls-for-location db current-location))))))

    (testing "when last is present no next-locations call is issued"
      (let [db (update db :locations conj {:location/boundary :boundary/last})]
        (is (= [[:get-prev-locations site-id comic-id current-location buffer-size]]
               (map butlast (sut/api-calls-for-location db current-location))))))))
