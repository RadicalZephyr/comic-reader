(ns comic-reader.main
  (:require
    [comic-reader.api :as api]
    [comic-reader.history :as history]
    [comic-reader.routing :as routing]
    [comic-reader.ui.base :as base]
    [comic-reader.ui.image :as image]
    [comic-reader.ui.reader :as reader]
    [comic-reader.ui.site-list :as site-list]
    [comic-reader.ui.comic-list :as comic-list]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [clairvoyant.core :refer-macros [trace-forms]]
    [re-frame-tracer.core :refer [tracer]]))

(defn page-key [db]
  (:page-key db))

(defn set-page-key [db page-key]
  (assoc db :page-key page-key))

(defn setup! []
  (api/setup!)
  (history/setup!)
  (routing/setup!)
  (image/setup!)
  (reader/setup!)
  (site-list/setup!)
  (comic-list/setup!)

  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     :init-db
     (fn init-db-event [db _]
       (assoc db :buffer-size 10)))

    (re-frame/reg-event-db
     :set-page-key
     (fn set-page-key-event [db [_ page-key]]
       (set-page-key db page-key)))

    (re-frame/reg-event-fx
     :view-sites
     (fn view-sites-event [cofx _]
       (let [db (:db cofx)]
         {:api (if-not (seq (:site-list db))
                 [[:get-sites {:on-success site-list/set}]]
                 [])
          :navigate [:comic-reader/site-list]
          :db (-> db
                  (set-page-key :site-list)
                  (update :site-list #(or % :loading)))})))

    (re-frame/reg-event-fx
     :view-comics
     (fn view-comics-event [cofx [_ site-id]]
       {:api [[:get-comics site-id {:on-success comic-list/set}]]
        :navigate [:comic-reader/comic-list {:site-id site-id}]
        :db (-> (:db cofx)
                (set-page-key :comic-list)
                (assoc :site-id site-id
                       :comic-list :loading))}))

    (re-frame/reg-event-fx
     :read-comic
     (fn read-comic-event [cofx [_ comic-id]]
       (let [db (:db cofx)
             site-id (:site-id db)
             buffer-size (:buffer-size db)]
         {:navigate [:comic-reader/reader-view-start {:site-id site-id :comic-id comic-id}]
          :db (-> db
                  (set-page-key :reader)
                  (assoc :comic-id comic-id
                         :loading-images true))
          :api [[:get-prev-locations site-id comic-id nil buffer-size
                 {:on-success #(re-frame/dispatch [:add-locations %])}]
                [:get-next-locations site-id comic-id nil buffer-size
                 {:on-success #(re-frame/dispatch [:add-locations %])}]]})))

    (re-frame/reg-event-fx
     :change-route
     (fn change-route-event [cofx [_ [route-name route-data]]]
       (case route-name
         :comic-reader/site-list {:dispatch [:view-sites]}
         :comic-reader/comic-list {:dispatch [:view-comics (:site-id route-data)]}
         :comic-reader/reader-view-start {:db (assoc (:db cofx) :site-id (:site-id route-data))
                                          :dispatch [:read-comic (:comic-id route-data)]}
         :comic-reader/reader-view {:db (assoc (:db cofx) :site-id (:site-id route-data))
                                    :dispatch [:read-comic (:comic-id route-data)]}))))

  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     :page-key
     (fn page-key-sub [app-db v]
       (page-key app-db)))

    (re-frame/reg-sub
     :site-id
     (fn site-id-sub [app-db _]
       (:site-id app-db)))

    (re-frame/reg-sub
     :comic-id
     (fn comic-id-sub [app-db _]
       (:comic-id app-db)))))

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
  (routing/match-and-dispatch-route (history/get))
  (render-root))

(defn dev-reload []
  (re-frame.core/clear-subscription-cache!)
  (setup!)
  (render-root))
