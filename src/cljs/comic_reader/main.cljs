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

(defroute "*" {:as _}
  (rf/dispatch [:unknown]))

;; Actual re-frame code

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here."
   [:a {:href "/#"}]])

(defn manga-site [site]
  (let [{:keys [id name]} site]
   ^{:key id}
     [:li
      [:input {:type "button"
               :value name
               :on-click #(.log js/console
                                (str "You clicked the "
                                     id
                                     " button!"))}]]))

(defn site-list []
  (let [site-list (rf/subscribe [:site-list])]
    (fn []
      (when-let [site-list @site-list]
        [:ul (map manga-site site-list)]))))

(defn comic-reader []
  (let [page (rf/subscribe [:page])]
    (fn []
      [:div
       (case @page
         :sites [site-list]
         :comics "Display the comics available."
         :read "Display the comic itself!"
         nil ""
         [four-oh-four])])))

(defn ^:export run []
  (init-handlers!)
  (init-subscriptions!)
  (try
    (history/hook-browser-navigation!)
    (catch js/Error e
      nil))
  (reagent/render [comic-reader]
                  (.-body js/document)))
