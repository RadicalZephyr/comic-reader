(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.routes :as r]
            [comic-reader.handlers :refer [init-handlers!]]
            [comic-reader.subscriptions
             :refer [init-subscriptions!]]
            [comic-reader.history
             :refer [hook-browser-navigation!]]
            [comic-reader.scrolling
             :refer [setup-scrolling-events!]]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]
            [secretary.core :as secretary
             :refer-macros [defroute]]
            cljsjs.waypoints))

;; Actual re-frame code

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here."
   [:a {:href "/#"}]])

(defn id-btn-for-callback [on-click]
  (fn [{:keys [id name]
        :as data}]
    ^{:key id}
    [:li
     [:input {:type "button"
              :value name
              :on-click (partial on-click data)}]]))

(defn site-list []
  (let [site-list (rf/subscribe [:site-list])]
    (fn []
      (when-let [site-list @site-list]
        [:ul (map (id-btn-for-callback
                   #(r/go-to (r/comics-path {:site (name (:id %))})))
                  site-list)]))))

(defn comic-list []
  (let [site (rf/subscribe [:site])
        comic-list (rf/subscribe [:comic-list])]
    (fn []
      (let [site       @site
            comic-list @comic-list]
        (when (and site comic-list)
          [:ul (map (id-btn-for-callback
                     (fn [item]
                       (r/go-to (r/read-path {:site site
                                              :comic (:id item)
                                              :chapter 1
                                              :page 1}))))
                    comic-list)])))))

(defn reader []
  (let [site       (rf/subscribe [:site])
        location   (rf/subscribe [:location])
        url-list   (rf/subscribe [:url-list])
        comic-imgs (rf/subscribe [:comic-imgs])]
    (fn []
      (let [site       @site
            location   @location
            url-list   @url-list
            comic-imgs @comic-imgs]
        (when (and site
                   location
                   url-list)
          [:div
           [:h2 (str "Display chapter " (:chapter location)
                     " page " (:page location)
                     " of comic " (:comic location)
                     " from site " site)]
           [:br]
           (when comic-imgs
             (into [:div] comic-imgs))])))))

(defn comic-reader []
  (let [page (rf/subscribe [:page])]
    (fn []
      [:div
       (case @page
         :sites [site-list]
         :comics [comic-list]
         :read [reader]
         nil ""
         [four-oh-four])])))

(defn ^:export run []
  (init-handlers!)
  (init-subscriptions!)
  (setup-scrolling-events!)
  (try
    (hook-browser-navigation!)
    (catch js/Error e
      nil))
  (reagent/render [comic-reader]
                  (.getElementById js/document "app")))

(run)
