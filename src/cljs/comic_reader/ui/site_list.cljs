(ns comic-reader.ui.site-list
  (:refer-clojure :exclude [get set])
  (:require [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]
            [clairvoyant.core :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]))

(defn get* [db]
  (clojure.core/get db :site-list))

(defn set* [db sites]
  (assoc db :site-list sites))

(defn setup! []
  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     :set-site-list
     (fn set-site-list-event [db [_ sites]]
       (set* db sites))))

  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     :site-list
     (fn site-list-sub [app-db _]
       (get* app-db)))))

(defn get []
  (re-frame/subscribe [:site-list]))

(defn set [sites]
  (re-frame/dispatch [:set-site-list sites]))


(defn site-list [view-site sites]
  (base/list-with-loading
   {:heading "Comic Sites"
    :list-element [:ul.inline-list]
    :item->li (fn [site]
                [:a.large.button.radius
                 {:on-click #(view-site (:site/id site))}
                 (:site/name site)])}
   sites))

(defn site-list-container []
  (let [sites (re-frame/subscribe [:site-list])]
    (fn [] [site-list
            (fn [site-id]
              (re-frame/dispatch [:view-comics site-id]))
            (deref sites)])))
