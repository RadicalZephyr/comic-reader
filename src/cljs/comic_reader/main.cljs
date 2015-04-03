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

(rf/register-sub
 :page
 (fn [db _]
   (reaction (get @db :page))))

(rf/register-sub
 :site-list
 (fn [db _]
   (reaction (get @db :site-list))))

(rf/register-sub
 :site
 (fn [db _]
   (reaction (get @db :site))))

(rf/register-sub
 :comic-list
 (fn [db _]
   (reaction (get @db :comic-list))))

(rf/register-sub
 :comic
 (fn [db _]
   (reaction (get @db :comic))))

(rf/register-sub
 :location
 (fn [db _]
   (reaction (get @db :location))))

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
