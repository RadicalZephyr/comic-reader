(ns comic-reader.ui.comic-list
  (:refer-clojure :exclude [get set])
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]
            [comic-reader.ui.base :as base]))

(defn get* [db]
  (clojure.core/get db :comic-list))

(defn set* [db comics]
  (assoc db :comic-list comics))

(defn setup! []
  (re-frame/register-sub
   :comic-list
   (fn [app-db _]
     (reaction (get* @app-db))))

  (re-frame/register-handler
   :set-comic-list
   (fn [db [_ comics]]
     (set* db comics))))

(defn get []
  (re-frame/subscribe [:comic-list]))

(defn set [comics]
  (re-frame/dispatch [:set-comic-list comics]))

(defn comic-list [view-comic comics]
  (base/list-with-loading
   {:heading "Comics"
    :list-element [:ul.no-bullet]
    :item->li (fn [comic]
                [:a.button.radius
                 {:on-click #(view-comic (:id comic))}
                 (:name comic)])}
   comics))

(defn comic-list-container []
  (let [comics (re-frame/subscribe [:comic-list])]
    (fn []
      [comic-list
       (fn [comic-id]
         (re-frame/dispatch [:view-comic comic-id]))
       (deref comics)])))

(defn letter-filter [set-prefix letter search-prefix]
  [(if (= search-prefix letter) :dd.active :dd)
   {:role "menuitem"}
   [:a.button.success.radius {:on-click #(set-prefix letter)}
    letter]])

(defn make-letter-builder [set-prefix search-prefix]
  (fn [letter]
    ^{:key (str "letter-" letter)}
    [letter-filter set-prefix letter search-prefix]))

(defn alphabet-letter-filters [set-prefix search-prefix]
  [:dl.sub-nav {:role "menu" :title "Comics Filter List"}
   (->> (seq "#ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        (map (make-letter-builder set-prefix search-prefix)))])

(defn search-box [set-prefix search-prefix clear]
  (let [attrs {:type "search"
               :placeholder (or search-prefix "")
               :auto-complete "on"
               :on-change #(set-prefix
                            (.-value (.-target %)))}
        maybe-value (when clear
                      (set-prefix search-prefix)
                      {:value ""})]
    [:input (merge attrs maybe-value)]))

(defn comic-list-filter [set-prefix search-data]
  (let [{:keys [search-prefix clear]} search-data]
    [:div.panel.radius
     [:h6 "Filter Comics:"]
     [search-box set-prefix search-prefix clear]
     [alphabet-letter-filters
      (fn [letter] (set-prefix letter :clear true))
      search-prefix]
     [:a.tiny.secondary.button.radius
      {:on-click #(set-prefix "" :clear true)}
      "clear filters"]]))
