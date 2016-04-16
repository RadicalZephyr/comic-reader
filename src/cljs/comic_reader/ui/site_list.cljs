(ns comic-reader.ui.site-list
  (:refer-clojure :exclude [get set]
                  :rename {get cget})
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]
            [comic-reader.ui.base :as base]))

(defn get [db]
  (cget db :site-list))

(defn set [db [_ sites]]
  (assoc db :site-list sites))

(defn setup! []
  (re-frame/register-sub
   :site-list
   (fn [app-db v]
     (reaction (get @app-db))))

  (re-frame/register-handler
   :set-site-list
   set))

(defn site-list [view-site sites]
  (base/list-with-loading
   {:heading "Comic Sites"
    :list-element [:ul.inline-list]
    :item->li (fn [site]
                [:a.large.button.radius
                 {:on-click #(view-site (:id site))}
                 (:name site)])}
   sites))

(defn site-list-container []
  (let [sites (re-frame/subscribe [:site-list])]
    (fn [] [site-list
            (fn [site-id]
              (re-frame/dispatch [:view-site site-id]))
            (deref sites)])))
