(ns comic-reader.ui.reader-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [garden.core :as g]
            [garden.selectors :as gs]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.reader :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

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
  (testing "can handle having a nil buffer-size"
    (is (= []
           (sut/current-locations [] nil))))

  (testing "can handle having no images"
    (is (= []
           (sut/current-locations [] 1))))

  (testing "can handle not having any previous items"
    (is (= [:current 2]
           (sut/current-locations [[] [:current] [2 3]]  1))))

  (testing "can handle not having any following items"
    (is (= [2 :current]
           (sut/current-locations [[1 2] [:current] []] 1))))

  (testing "can select 1 on either side of the current location"
    (is (= [1 :a 2]
           (sut/current-locations [[1] [:a] [2]] 1)))

    (is (= [2 :a 3]
           (sut/current-locations [[1 2] [:a] [3 4]]   1))))

  (testing "selects n on either side of the current location"
    (is (= [1 2 :a 3 4]
           (sut/current-locations [[1 2] [:a] [3 4]] 2))))

  (testing "Only selects as many images as have been loaded"
    (is (= [1 :a 2]
           (sut/current-locations [[1] [:a] [2]] 10)))
    (is (= [1 :a 2 3 4]
           (sut/current-locations [[1] [:a] [2 3 4]] 10)))))

(defcard-rg comic-image-list
  (fn [data _]
    (let [set-current #(swap! data assoc :current-location %)
          locations [{:location/chapter {:chapter/title "The Gamer 1"
                                         :chapter/url "http://www.mangareader.net/the-gamer/1"
                                         :chapter/number 1}
                      :location/page {:page/number 1
                                      :page/url "http://www.mangareader.net/the-gamer/1"}}
                     {:location/chapter {:chapter/title "The Gamer 1"
                                         :chapter/url "http://www.mangareader.net/the-gamer/1"
                                         :chapter/number 1}
                      :location/page {:page/number 2
                                      :page/url "http://www.mangareader.net/the-gamer/1/2"}}]]
      (reactively
       [:div
        [:style {:dangerouslySetInnerHTML
                 {:__html
                  (g/css
                   [:#com-rigsomelight-devcards-main
                    [:div.com-rigsomelight-devcard
                     [(gs/& (gs/nth-child "0n+3"))
                      [:div.com-rigsomelight-devcards-typog.com-rigsomelight-rendered-edn
                       {:position "fixed"
                        :top "10px"
                        :left "10px"}]]]])}}]
        [sut/comic-location-list set-current locations]])))
  (reagent/atom {})
  {:inspect-data true})

(defcard-rg comic-image-reader
  (fn [data _]
    (let [set-data! #(swap! data assoc :current-location %)
          locations [{:location/chapter {:chapter/title "The Gamer 1"
                                         :chapter/url "http://www.mangareader.net/the-gamer/1"
                                         :chapter/number 1}
                      :location/page {:page/number 1
                                      :page/url "http://www.mangareader.net/the-gamer/1"}}]]
      [sut/reader set-data! locations]))
  (reagent/atom {})
  {:inspect-data true})
