(ns comic-reader.ui.component-css
  (:refer-clojure :exclude [merge])
  (:require [garden.core :as g]
            [re-frame.core :as re-frame]))

(defn get* [db]
  (:component-css db))

(defn merge* [db id garden-css]
  (assoc-in db [:component-css id] garden-css))

(defn setup! []
  (re-frame/reg-sub
   :component-css
   (fn [app-db _]
     (get* app-db)))

  (re-frame/reg-event-db
   :add-to-component-css
   (fn [db [_ id garden-css]]
     (merge* db id garden-css))))

(defn merge [id garden-css]
  (re-frame/dispatch [:add-to-component-css id garden-css]))

(defn component-garden-css []
  (vals @(re-frame/subscribe [:component-css])))

(defn component-css []
  [:style (g/css @(component-garden-css))])
