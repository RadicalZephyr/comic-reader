(ns comic-reader.ui.overlay-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [reagent.core :as reagent]
            [comic-reader.ui.overlay :as sut]
            [comic-reader.ui.base :as base])
  (:require-macros [comic-reader.macro-util :refer [reactively]]))

(def dummy-content
  [:div
   [:h2 "Content Title"]
   [:p "Body paragraph"]
   [:p "Body paragraph"]
   [:p "Body paragraph"]])

(defcard-rg overlay-on
  (fn [data _]
    (let [toggle-overlay (fn []
                           (base/do-later
                            #(swap! data update :overlay not)))]
      (reactively
       [sut/overlay
        {:header [:h2 "Top"]
         :footer [:h2 "Bottom"]
         :display? (:overlay @data)
         :toggle-overlay toggle-overlay
         :content dummy-content}])))
  (reagent/atom {:overlay true})
  {:inspect-data true})

(defcard-rg overlay-off
  (fn [data _]
    (let [toggle-overlay (fn []
                           (base/do-later
                            #(swap! data update :overlay not)))]
      (reactively
       [sut/overlay
        {:header [:h2 "Top"]
         :footer [:h2 "Bottom"]
         :display? (:overlay @data)
         :toggle-overlay toggle-overlay
         :content dummy-content}])))
  (reagent/atom {:overlay false})
  {:inspect-data true})
