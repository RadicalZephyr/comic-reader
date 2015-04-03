(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.history :as history]
            [reagent.core :as reagent :refer [atom]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :as rf]))

(defonce initial-state
  {:page :sites})

(rf/register-handler
 :initialize
 (fn
   [db _]
   (merge db initial-state)))

(defn comic-reader []
  (let [page (rf/subscribe [:page])]
    (fn []
      [:div
       (case @page
         :sites "Display the sites."
         :comics "Display the comics available."
         :reading "Display the comic itself!"
         "")])))

(defn ^:export run []
  (rf/dispatch [:initialize])
  (reagent/render [comic-reader]
                  (.-body js/document)))
