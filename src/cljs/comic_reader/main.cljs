(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.routes :as r
             :refer [setup-secretary!]]
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
     [:a.large.button.radius
      {:href (click-address data)}
      name]]))

(defn site-list []
  (let [site-list (rf/subscribe [:site-list])]
    (fn []
      (when-let [site-list @site-list]
        [:div
         [:h1 "Manga Sites"]
         [:ul.inline-list (map (id-btn-for-callback
                                #(r/comics-path
                                  {:site (name (:id %))}))
                               site-list)]]))))

(defn filter-element [site cl-filter letter]
  [(if (= cl-filter letter)
     :dd.active
     :dd)
   {:role "menuitem"}
   [:a.button.success.radius
    {:href (r/comics-path
            {:site site
             :query-params
             (when-not (= cl-filter letter)
               {:filter letter})})}
    letter]])

(defn search-box [site cl-filter]
  (reagent/create-class
   {:display-name "search-box"
    :component-did-mount
    (fn [this]
      (let [node (reagent/dom-node this)
            value (.-value node)]
        (.addEventListener node "change"
                           #(r/go-to
                             (r/comics-path
                              {:site site
                               :query-params
                               (when value
                                 {:filter value})})))))
    :reagent-render
    (fn [site cl-filter]
      [:input {:type "search"
               :placeholder (or cl-filter "")
               :auto-complete "on"}])}))

(defn filter-nav-bar [site cl-filter]
  [:div.panel.radius
   [:h6 "Filter Comics:"
    [search-box site cl-filter]]
   [:dl.sub-nav {:role "menu" :title "Comics Filter List"}
    (->> "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"
         seq
         (map #(conj ^{:key (str "filter-" %)}
                     [filter-element site cl-filter]
                     %)))]
   [:a.tiny.secondary.button.radius
    {:href (r/comics-path
            {:site site})}
    "clear filters"]])

(defn comic-buttons [site comic-list]
  (map (id-btn-for-callback
        (fn [item]
          (r/read-path {:site site
                        :comic (:id item)
                        :chapter 1
                        :page 1})))
       comic-list))

(defn comic-list []
  (let [site (rf/subscribe [:site])
        comic-list (rf/subscribe [:comic-list])
        comic-list-filter (rf/subscribe [:comic-list-filter])]
    (fn []
      (let [site       @site
            comic-list @comic-list
            cl-filter  @comic-list-filter]
        [:div.row
         [:div.small-12.large-5.large-push-7.columns
          [filter-nav-bar site cl-filter]]
         (when site
           [:div.small-12.large-7.large-pull-5.columns
            [:h1 "Comics from " (titlize site
                                         :to-spaces #"-")]
            (when comic-list
              [:ul.no-bullet
              (->> comic-list
                   (comic-buttons site))])])]))))

(defn img-component [site
                     {:keys [comic]}
                     {:keys [chapter page tag]}]
  (let [waypoints (clojure.core/atom nil)]
    (reagent/create-class
     {:display-name "image-component"
      :component-did-mount
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
      :component-will-unmount
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
                    (map #(conj [img-component site location]
                                %)
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
  (setup-secretary!)
  (try
    (hook-browser-navigation!)
    (catch js/Error e
      nil))
  (reagent/render [comic-reader]
                  (.getElementById js/document "app")))

(run)
