(ns comic-reader.ui.comic-list
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]
            [comic-reader.ui.base :as base])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2]]))

(defn set-comic-list [db [_ comics]]
  (assoc db :comic-list comics))

(defn get-comic-list [db]
  (get db :comic-list))

(defn setup-comic-list! []
  (re-frame/register-sub
   :comic-list
   (fn [app-db v]
     (reaction (get-comic-list @app-db))))

  (re-frame/register-handler
   :set-comic-list
   set-comic-list))

(defcomponent-2 comic-list
  [[comics :comic-list]]
  (base/list-with-loading
   {:heading "Comics"
    :list-element [:ul.no-bullet]
    :item->li (fn [comic]
                [base/button (:name comic)])}
   comics))

(defn letter-filter [letter search-prefix]
  [(if (= search-prefix letter) :li.active :li)
   {:role "menuitem"}
   [:a.button.success.radius {:href ""} letter]])
