(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.handlers :refer [init-handlers!]]
            [comic-reader.history :as history]
            [reagent.core :as reagent :refer [atom]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :as rf]
            [secretary.core :as secretary
                            :refer-macros [defroute]]))

;; Have secretary pull apart URL's and then dispatch with re-frame
(defroute sites-path "/" []
  (rf/dispatch [:sites]))

(defroute comics-path "/comics/:site" [site]
  (rf/dispatch [:comics site]))

(defroute read-path "/read/:comic/:volume/:page" {:as location}
  (rf/dispatch [:read location]))


;; Actual re-frame code


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

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here."])

(defn comic-reader []
  (let [page (rf/subscribe [:page])]
    (fn []
      [:div
       (case @page
         :sites "Display the sites."
         :comics "Display the comics available."
         :read "Display the comic itself!"
         [four-oh-four])])))

(defn ^:export run []
  (init-handlers!)
  (rf/dispatch [:initialize])
  (try
    (history/hook-browser-navigation!)
    (catch js/Error e
      nil))
  (reagent/render [comic-reader]
                  (.-body js/document)))
