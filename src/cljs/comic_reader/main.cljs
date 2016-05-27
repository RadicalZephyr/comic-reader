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
   :initialize-app-state
   (fn [db [_ state]]
     state))

  (re-frame/register-handler
   :view-site
   (fn [db [_ site-id]]
     (-> db
         (assoc :page-key :comic-list)))))

(defn main-panel
  [page-key]
  (case page-key
    :site-list [site-list/site-list-container]
    :comic-list [comic-list/comic-list-container]
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
  (re-frame/dispatch [:initialize-app-state {:page-key :site-list}])
  (api/get-sites {:on-success #(re-frame/dispatch [:set-site-list %])})
  (reagent/render-component [main-panel-container]
                            (.getElementById js/document "app")))

(main)
