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

(defcard-rg test-error-wiring
  (fn [data _]
    (let [error {:a "bad error"
                 :thing "happened"}
          warning {:not-bad "warning"
                   :happened true}]
      (sut/setup!)
      (reactively
       [:div
        [:button {:on-click #(sut/report-error error)} "Error"]
        [:button {:on-click #(sut/report-error warning) :style {:margin-left "10px"}} "Warning"]
        [:p (str "Error count " (count @(sut/api-errors)))]
        [:pre (prn-str @sut/*last-error*)]]))))
