(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2
                                                    with-optional-tail]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here. Try checking out the "
   [:a {:href "/#"} "site list."]])

(defn large-button [content]
  [:a.large.button.radius content])

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

(defn map-into-list [base-el f coll]
  (into base-el
        (map (fn [data]
               ^{:key (:id data)} [:li (f data)]) coll)))

(defcomponent-2 site-list
  [[sites :site-list]]
  (with-optional-tail
    [:div [:h1 "Comic Sites"]]
    (cond
      (= :loading sites) [loading]
      (seq sites)        (map-into-list
                          [:ul.inline-list]
                          (fn [site]
                            [large-button (:name site)])
                          sites)
      :else nil)))
