(ns comic-reader.ui.component-css
  (:refer-clojure :exclude [merge])
  (:require [garden.core :as g]
            [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn get* [db]
  (:component-css db))

(defn merge* [db garden-css]
  (update db :component-css conj garden-css))

(defn setup! []
  (re-frame/register-sub
   :component-css
   (fn [app-db _]
     (reaction (get* @app-db))))

  (re-frame/register-handler
   :add-to-component-css
   (fn [db [_ garden-css]]
     (merge* db garden-css))))

(defn merge [garden-css]
  (re-frame/dispatch [:add-to-component-css garden-css]))

(defn component-garden-css []
  (re-frame/subscribe [:component-css]))

(defn component-css []
  (reaction [:style (g/css @(component-garden-css))]))
