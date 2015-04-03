(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.session :as session]
            [comic-reader.history :as history]
            [comic-reader.pages.sites
             :refer [site-list]]
            [comic-reader.pages.comics
             :refer [comic-list]]
            [comic-reader.pages.viewer
             :refer [comic-viewer]]
            [reagent.core :as reagent :refer [atom]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :as rf]))

(defonce time-updater (js/setInterval
                       #(rf/dispatch [:timer (js/Date.)]) 1000))

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
