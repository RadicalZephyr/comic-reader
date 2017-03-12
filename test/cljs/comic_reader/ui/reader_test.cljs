(ns comic-reader.ui.reader-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [garden.core :as g]
            [garden.selectors :as gs]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.reader :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

(deftest test-partitioned-images
  (testing "handles no data gracefully"
    (is (= []
           (sut/partitioned-images [] nil)))

    (is (= []
           (sut/partitioned-images nil nil)))

    (is (= []
           (sut/partitioned-images nil :location/abc))))

  (testing "partitions based on the given image location"
    (is (= [[1] [{:image/location :a}] [2]]
           (sut/partitioned-images [1 {:image/location :a} 2] :a)))

    (is (= [[1] [{:image/location :a}] [2 3 4]]
           (sut/partitioned-images [1 {:image/location :a} 2 3 4] :a)))

    (is (= [[-1 0 1] [{:image/location :a}] [2]]
           (sut/partitioned-images [-1 0 1 {:image/location :a} 2] :a))))

  (testing "returns meaningful partitions when there are no preceding items"
    (is (= [[] [{:image/location :a}] [2]]
           (sut/partitioned-images [{:image/location :a} 2] :a))))

  (testing "returns meaningful partitions when there are no following items"
    (is (= [[1 2] [{:image/location :a}] []]
           (sut/partitioned-images [1 2 {:image/location :a}] :a)))))

(deftest test-current-images
  (testing "can handle having no images"
    (is (= []
           (sut/current-images [] 1))))

  (testing "can handle not having any previous items"
    (is (= [:current 2]
           (sut/current-images [[] [:current] [2 3]]  1))))

  (testing "can handle not having any following items"
    (is (= [2 :current]
           (sut/current-images [[1 2] [:current] []] 1))))

  (testing "can select 1 on either side of the current location"
    (is (= [1 {:image/location :a} 2]
           (sut/current-images [[1] [{:image/location :a}] [2]] 1)))

    (is (= [2 {:image/location :a} 3]
           (sut/current-images [[1 2] [{:image/location :a}] [3 4]]   1))))

  (testing "selects n on either side of the current location"
    (is (= [1 2 {:image/location :a} 3 4]
           (sut/current-images [[1 2] [{:image/location :a}] [3 4]] 2))))

  (testing "Only selects as many images as have been loaded"
    (is (= [1 {:image/location :a} 2]
           (sut/current-images [[1] [{:image/location :a}] [2]] 10)))
    (is (= [1 {:image/location :a} 2 3 4]
           (sut/current-images [[1] [{:image/location :a}] [2 3 4]] 10)))))

(defcard-rg comic-image-list
  (fn [data _]
    (let [set-current #(swap! data assoc :current-location %)
          images [{:image/location {:abc 1}
                   :image/tag [:img {:src "/public/img/tux.png"}]}
                  {:image/location {:abc 2}
                   :image/tag [:img {:src "/public/img/loading.svg"}]}]]
      (reactively
       [:div
        [:style {:dangerouslySetInnerHTML
                 {:__html
                  (g/css
                   [:#com-rigsomelight-devcards-main
                    [:div.com-rigsomelight-devcard
                     [(gs/& (gs/nth-child "0n+1"))
                      [:div.com-rigsomelight-devcards-typog.com-rigsomelight-rendered-edn
                       {:position "fixed"
                        :top "10px"
                        :left "10px"}]]]])}}]
        [sut/comic-image-list set-current images]])))
  (reagent/atom {})
  {:inspect-data true})

(defcard-rg comic-image-reader
  (fn [data _])
  (reagent/atom {})
  {:inspect-data true})
