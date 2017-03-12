(ns comic-reader.ui.reader-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [garden.core :as g]
            [garden.selectors :as gs]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.reader :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

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
        [sut/comic-image-list set-current images :abc]])))
  (reagent/atom {})
  {:inspect-data true})

(defcard-rg comic-image-reader
  (fn [data _])
  (reagent/atom {})
  {:inspect-data true})
