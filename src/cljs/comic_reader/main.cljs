(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.handlers :refer [init-handlers!]]
            [comic-reader.subscriptions
             :refer [init-subscriptions!]]
            [comic-reader.history :as history]
            [reagent.core :as reagent :refer [atom]]
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
  (init-subscriptions!)
  (rf/dispatch [:initialize])
  (try
    (history/hook-browser-navigation!)
    (catch js/Error e
      nil))
  (reagent/render [comic-reader]
                  (.-body js/document)))
