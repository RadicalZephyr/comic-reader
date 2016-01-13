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

(defn letter-filter [set-prefix letter search-prefix]
  [(if (= search-prefix letter) :dd.active :dd)
   {:role "menuitem"}
   [:a.button.success.radius {:on-click #(set-prefix)}
    letter]])

(defn make-letter-builder [make-set-prefix search-prefix]
  (fn [letter]
    ^{:key (str "letter-" letter)}
    [letter-filter (make-set-prefix letter) letter search-prefix]))

(defn alphabet-letter-filters [make-set-prefix search-prefix]
  [:dl.sub-nav {:role "menu" :title "Comics Filter List"}
   (->> (seq "#ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        (map (make-letter-builder make-set-prefix search-prefix)))])

(defn search-box [search-prefix]
  [:input {:type "search"
           :placeholder (or search-prefix "")
           :auto-complete "on"}])

(defn comic-list-filter [search-prefix]
  [:div.panel.radius
   [:h6 "Filter Comics: " [search-box search-prefix]]
   [alphabet-letter-filters (fn [] identity) search-prefix]
   [:a.tiny.secondary.button.radius
    {:href ""}
    "clear filters"]])
