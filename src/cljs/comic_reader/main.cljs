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
  [:div])

(defn ^:export run []
  (rf/dispatch [:initialize])
  (reagent/render [comic-reader]
                  (.-body js/document)))
