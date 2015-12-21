(ns comic-reader.ui.site-list
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]
            [comic-reader.ui.base :as base])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2]]))

(defn get-sites-list [db]
  (get db :site-list))

(defn set-site-list [db [_ sites]]
  (assoc db :site-list sites))

(defn setup-site-list! []
 (re-frame/register-sub
  :site-list
  (fn [app-db v]
    (reaction (get-sites-list @app-db))))

 (re-frame/register-handler
  :set-site-list
  set-site-list))

(defcomponent-2 site-list
  [[sites :site-list]]
  (base/with-optional-tail
    [:div [:h1 "Comic Sites"]]
    (cond
      (= :loading sites) [base/loading]
      (seq sites)        (base/map-into-list
                          [:ul.inline-list]
                          (fn [site]
                            [base/large-button (:name site)])
                          sites)
      :else nil)))
