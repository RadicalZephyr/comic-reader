(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2
                                                    with-optional-tail]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-element [site]
  ^{:key (:id site)}
  [:li [:a (:name site)]])

(defn get-sites-list [db]
  (get db :site-list))

(re-frame/register-sub
 :site-list
 (fn [app-db v]
   (reaction (get-sites-list @app-db))))

(defn set-site-list [db [_ sites]]
  (assoc db :site-list sites))

(re-frame/register-handler
 :set-site-list
 set-site-list)

(defcomponent-2 site-list
  [[sites :site-list]]
  (with-optional-tail
    [:div [:h1 "Comic Sites"]]
    (cond
      (= :loading sites) [loading]
      (seq sites)        (into [:ul]
                               (map site-element sites))
      :else nil)))
