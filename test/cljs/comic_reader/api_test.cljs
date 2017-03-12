(ns comic-reader.api-test
  (:require [comic-reader.api :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]
            [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [reagent.ratom :refer-macros [reaction]]))


(deftest test-add-error
  (is (= [{}] (sut/add-error nil {})))
  (is (= [{}] (sut/add-error [] {})))
  (is (= [{:a 1} {:b 2}] (sut/add-error [{:a 1}] {:b 2}))))

(sut/setup!)
(defcard-rg test-error-wiring
  (fn [data _]
    (let [error {:a "bad error"
                 :thing "happened"}
          warning {:not-bad "warning"
                   :happened true}]

      (reactively
       [:div
        [:button {:on-click #(sut/report-error error)} "Error"]
        [:button {:on-click #(sut/report-error warning) :style {:margin-left "10px"}} "Warning"]
        [:p (str "Error count " @(sut/error-count))]
        [:pre (prn-str @(sut/last-error))]]))))

(defcard-rg test-api-definitions
  (fn [data _]
    (let [set-data! #(swap! data assoc :data (take 10 %))]

      (reactively
       [:div
        [:button {:on-click #(sut/get-sites {:on-success set-data!})}
         "Sites"]
        [:button {:on-click #(sut/get-comics "manga-reader" {:on-success set-data!})
                  :style {:margin-left "10px"}}
         "Comics"]])))
  (reagent/atom {})
  {:inspect-data true})
