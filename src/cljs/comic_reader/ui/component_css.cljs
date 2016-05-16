(ns comic-reader.ui.component-css
  (:refer-clojure :exclude [merge])
  (:require [garden.core :as g]
            [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn get* [db]
  (:component-css db))

(defn merge* [db id garden-css]
  (assoc-in db [:component-css id] garden-css))

(defn setup! []
  (re-frame/register-sub
   :component-css
   (fn [app-db _]
     (reaction (get* @app-db))))

  (re-frame/register-handler
   :add-to-component-css
   (fn [db [_ id garden-css]]
     (merge* db id garden-css))))

(defn merge [id garden-css]
  (re-frame/dispatch [:add-to-component-css id garden-css]))

(defn component-garden-css []
  (reaction (vals @(re-frame/subscribe [:component-css]))))

(defn component-css []
  (reaction [:style (g/css @(component-garden-css))]))
