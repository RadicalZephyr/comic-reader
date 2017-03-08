(ns comic-reader.main
  (:require
    [comic-reader.api :as api]
    [comic-reader.ui.base :as base]
    [comic-reader.ui.site-list :as site-list]
    [comic-reader.ui.comic-list :as comic-list]
    [reagent.core :as reagent]
    [reagent.ratom :refer-macros [reaction]]
    [re-frame.core :as re-frame]))

(defn page-key [db]
  (:page-key db))

(defn set-page-key [db page-key]
  (assoc db :page-key page-key))

(defn maybe-load-sites [db]
  (if (:site-list db)
    db
    (do
      (api/get-sites {:on-success site-list/set})
      (assoc db :site-list :loading))))

(defn setup! []
  (re-frame/reg-sub
   :page-key
   (fn [app-db v]
     (reaction (page-key @app-db))))

  (re-frame/reg-event-db
   :set-page-key
   (fn [db [_ page-key]]
     (set-page-key db page-key)))

  (re-frame/reg-event-db
   :view-sites
   (fn [db _]
     (-> db
         (set-page-key :site-list)
         (maybe-load-sites))))

  (re-frame/reg-event-db
   :view-comics
   (fn [db [_ site-id]]
     (api/get-comics site-id {:on-success comic-list/set})
     (-> db
         (set-page-key :comic-list)
         (assoc :site-id site-id
                :comic-list :loading))))

  (re-frame/reg-event-db
   :read-comic
   (fn [db [_ comic-id]]
     (-> db
         (set-page-key :reader)))))

(defn main-panel [page-key]
  (case page-key
    :site-list [site-list/site-list-container]
    :comic-list [comic-list/comic-page-container]
    :reader [:div [:h1 "Read a Comic!"]]
    nil [:span ""]
    [base/four-oh-four]))

(defn main-panel-container []
  (let [page-key (re-frame/subscribe [:page-key])]
    [#'main-panel @page-key]))

(defn ^:export main []
  (enable-console-print!)
  (setup!)
  (site-list/setup!)
  (comic-list/setup!)
  (re-frame/dispatch [:view-sites])
  (reagent/render-component [main-panel-container]
                            (.getElementById js/document "app")))
