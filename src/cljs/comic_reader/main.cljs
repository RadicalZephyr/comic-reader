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
            [comic-reader.utils :refer [titlize]]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]
            [secretary.core :as secretary
             :refer-macros [defroute]]
            org.clojars.earthlingzephyr.waypoints))

;; Actual re-frame code

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here."
   [:a {:href "/#"}]])

(defn id-btn-for-callback [click-address]
  (fn [{:keys [id name]
        :as data}]
    ^{:key id}
    [:li
     [:a.large.button
      {:href (str "/#" (click-address data))}
      name]]))

(defn site-list []
  (let [site-list (rf/subscribe [:site-list])]
    (fn []
      (when-let [site-list @site-list]
        [:div
         [:h1 "Manga Sites"]
         [:ul (map (id-btn-for-callback
                    #(r/comics-path
                      {:site (name (:id %))}))
                   site-list)]]))))

(defn comic-list []
  (let [site (rf/subscribe [:site])
        comic-list (rf/subscribe [:comic-list])]
    (fn []
      (let [site       @site
            comic-list @comic-list]
        [:div
         [:h1 "Comics from " (titlize site
                                      :to-spaces #"-")]
         (when (and site comic-list)
           [:ul (map (id-btn-for-callback
                      (fn [item]
                        (r/read-path {:site site
                                      :comic (:id item)
                                      :chapter 1
                                      :page 1})))
                     comic-list)])]))))

(defn img-component [site
                     {:keys [comic]}
                     {:keys [chapter page tag]}]
  (let [waypoints (clojure.core/atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [node (reagent/dom-node this)
              go-to-comic #(r/go-to
                            (r/read-path
                             {:site site
                              :comic comic
                              :chapter chapter
                              :page page}))
              wp-down
              (js/Waypoint.
               #js {:element node
                    :handler #(when (= %
                                       "down")
                                (go-to-comic))})
              wp-up
              (js/Waypoint.
               #js {:element node
                    :offset "bottom-in-view"
                    :handler #(when (= %
                                       "up")
                                (go-to-comic))})]
          (reset! waypoints [wp-up wp-down])))
      :componentWillUnmount
      (fn []
        (map #(.destroy %) @waypoints))
      :reagent-render
      (fn [site location {:keys [tag]}]
        [:div.row
         [:div.medium-12.columns tag]])})))

(defn reader []
  (let [site       (rf/subscribe [:site])
        location   (rf/subscribe [:location])
        comic-imgs (rf/subscribe [:comic-imgs])]
    (fn []
      (let [site       @site
            location   @location
            comic-imgs @comic-imgs]
        (when (and site
                   location)
          (let [component
                [:div
                 [:h2 (titlize (:comic location)
                               :to-spaces #"(_|-)")]
                 [:br]]]
            (when comic-imgs
              (into component
                    (map (partial vector
                                  img-component site location)
                         comic-imgs)))))))))

(defn comic-reader []
  (let [page (rf/subscribe [:page])]
    (fn []
      (case @page
        :sites [site-list]
        :comics [comic-list]
        :read [reader]
        nil [:span ""]
        [four-oh-four]))))

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
