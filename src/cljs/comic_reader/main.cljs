(ns comic-reader.main
  (:require
    [comic-reader.api :as api]
    [comic-reader.ui.base :as base]
    [comic-reader.ui.image :as image]
    [comic-reader.ui.reader :as reader]
    [comic-reader.ui.site-list :as site-list]
    [comic-reader.ui.comic-list :as comic-list]
    [reagent.core :as reagent]
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
  (api/setup!)
  (image/setup!)
  (reader/setup!)
  (site-list/setup!)
  (comic-list/setup!)

  (re-frame/reg-event-db
   :init-db
   (fn [db _]
     (assoc db :buffer-size 10)))

  (re-frame/reg-sub
   :page-key
   (fn [app-db v]
     (page-key app-db)))

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

  (re-frame/reg-sub
   :site-id
   (fn [app-db _]
     (:site-id app-db)))

  (re-frame/reg-event-db
   :view-comics
   (fn [db [_ site-id]]
     (api/get-comics site-id {:on-success comic-list/set})
     (-> db
         (set-page-key :comic-list)
         (assoc :site-id site-id
                :comic-list :loading))))

  (re-frame/reg-sub
   :comic-id
   (fn [app-db _]
     (:comic-id app-db)))

  (re-frame/reg-event-fx
   :read-comic
   (fn [cofx [_ comic-id]]
     (let [db (:db cofx)
           site-id (:site-id db)
           buffer-size (:buffer-size db)]
       {:db (-> db
                (set-page-key :reader)
                (assoc :comic-id comic-id
                       :loading-images true))
        :api [[:get-prev-locations site-id comic-id nil buffer-size
               {:on-success #(re-frame/dispatch [:add-locations %])}]
              [:get-next-locations site-id comic-id nil buffer-size
               {:on-success #(re-frame/dispatch [:add-locations %])}]]}))))

(defn main-panel [page-key]
  (case page-key
    :site-list [site-list/site-list-container]
    :comic-list [comic-list/comic-page-container]
    :reader [reader/view]
    nil [:span ""]
    [base/four-oh-four]))

(defn main-panel-container []
  (let [page-key (re-frame/subscribe [:page-key])]
    [#'main-panel @page-key]))

(defn render-root []
  (if-let [node (.getElementById js/document "app")]
    (reagent/render-component [#'main-panel-container] node)))

(defn ^:export main []
  (setup!)
  (re-frame/dispatch [:init-db])
  (re-frame/dispatch [:view-sites])
  (render-root))

(defn dev-reload []
  (re-frame.core/clear-subscription-cache!)
  (setup!)
  (.destroyAll js/Waypoint)
  (render-root))
