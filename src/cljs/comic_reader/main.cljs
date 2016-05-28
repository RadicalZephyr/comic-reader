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
  (re-frame/register-sub
   :page-key
   (fn [app-db v]
     (reaction (page-key @app-db))))

  (re-frame/register-handler
   :set-page-key
   (fn [db [_ page-key]]
     (set-page-key db page-key)))

  (re-frame/register-handler
   :view-sites
   (fn [db _]
     (-> db
         (set-page-key :site-list)
         (maybe-load-sites))))

  (re-frame/register-handler
   :view-comics
   (fn [db [_ site-id]]
     (api/get-comics site-id {:on-success comic-list/set})
     (-> db
         (set-page-key :comic-list)
         (assoc :site-id site-id
                :comic-list :loading))))

  (re-frame/register-handler
   :view-comic
   (fn [db [_ comic-id]]
     (-> db
         (set-page-key :comic-viewer)))))

(defn main-panel
  [page-key]
  (case page-key
    :site-list [site-list/site-list-container]
    :comic-list [comic-list/comic-page-container]
    :comic-viewer [:div [:h1 "Read a Comic!"]]
    nil [:span ""]
    [base/four-oh-four]))

(defn main-panel-container []
  (let [page-key (re-frame/subscribe [:page-key])]
    (fn []
      [main-panel (deref page-key)])))

(defn ^:export main
  []
  (enable-console-print!)
  (setup!)
  (site-list/setup!)
  (comic-list/setup!)
  (re-frame/dispatch [:view-sites])
  (reagent/render-component [main-panel-container]
                            (.getElementById js/document "app")))

(main)
