(ns comic-reader.pages.sites
  (:require [comic-reader.session :as session]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :refer [dispatch!]]))

(defn manga-site [site-data]
  ^{:key (:id site-data)}
  [:li
   [:input {:type "button"
            :value (:name site-data)
            :on-click #(dispatch!
                        (str "/site/" (:id site-data)))}]])

(defn site-list [sites]
  [:div
   [:h1 "Comic Sources"]
   [:ul (map manga-site sites)]])
