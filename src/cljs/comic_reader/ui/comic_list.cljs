(ns comic-reader.ui.comic-list
  (:refer-clojure :exclude [get set])
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [comic-reader.ui.base :as base]))

(defn get* [db]
  (clojure.core/get db :comic-list))

(defn set* [db comics]
  (assoc db :comic-list comics))

(defn setup! []
  (re-frame/reg-sub
   :comic-list
   (fn [app-db _]
     (get* app-db)))

  (re-frame/reg-event-db
   :set-comic-list
   (fn [db [_ comics]]
     (set* db comics)))

  (re-frame/reg-sub
   :search-data
   (fn [app-db _]
     (:search-data app-db)))

  (re-frame/reg-event-db
   :set-search-data
   (fn [db [_ search-data]]
     (assoc db :search-data search-data))))

(defn get []
  (re-frame/subscribe [:comic-list]))

(defn set [comics]
  (re-frame/dispatch [:set-comic-list comics]))

(defn get-search-data []
  (re-frame/subscribe [:search-data]))

(defn set-search-data [prefix & {:keys [clear]
                                 :or {:clear false}}]
  (re-frame/dispatch [:set-search-data {:search-prefix prefix
                                        :clear clear}]))

(defn comic-list [view-comic comics]
  (base/list-with-loading
   {:heading "Comics"
    :list-element [:ul.no-bullet]
    :item->li (fn [comic]
                [:a.button.radius
                 {:on-click #(view-comic (:id comic))}
                 (:name comic)])}
   comics))

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

(defn- re-string [letter]
  (if (= letter "#")
    "^[^a-z]"
    (str "^" letter)))

(defn prefix-filter-comics [prefix comics]
  (if-not (str/blank? prefix)
    (let [filter-re (re-pattern (str "(?i)^"
                                     (re-string prefix)))]
      (filter (fn [{:keys [name]}]
                (re-find filter-re name))
              comics))
    comics))

(defn comic-page [view-comic comics set-prefix search-data]
  (let [prefix (:search-prefix search-data)]
   [:div
    [comic-list-filter set-prefix search-data]
    [comic-list view-comic (prefix-filter-comics prefix comics)]]))

(defn comic-page-container []
  (let [view-comic (fn [comic-id]
                     (re-frame/dispatch [:view-comic comic-id]))
        comics      (get)
        search-data (get-search-data)]
    (fn []
      [comic-page
       view-comic (deref comics)
       set-search-data (deref search-data)])))
