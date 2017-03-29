(ns comic-reader.ui.routing
  (:require [bide.core :as routing]
            [clojure.string :as str]
            [comic-reader.ui.history :as history]
            [re-frame.core :as re-frame]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [comic-reader.ui.base :as base]))

(def router
  (routing/router
   [["/"                                                :comic-reader/site-list]
    ["/:site-id"                                        :comic-reader/comic-list]
    ["/:site-id/:comic-id"                              :comic-reader/reader-view-start]
    ["/:site-id/:comic-id/:chapter-number/:page-number" :comic-reader/reader-view]]))

(defn- token->route [token]
  (-> token
      (str/replace-first #"#" "")
      (str/replace-first #"!" "")))

(defn- route->token [route]
  (str "!" route))

(defonce ^:private dispatch? (atom true))

(defn match [token]
  (routing/match router (token->route token)))

(defn match-and-dispatch-route [token]
  (if @dispatch?
    (when-let [route-data (match token)]
      (re-frame/dispatch [:change-route route-data]))
    (reset! dispatch? true)))

(defn- ignore-next-token-change! []
  (reset! dispatch? false))

(defn setup! []
  (re-frame/reg-cofx
   :current-route
   (fn route-cofx [cofx _]
     (assoc cofx :current-route (match (history/get)))))

  (re-frame/reg-fx
   :navigate
   (fn navigate-fx [route-data]
     (ignore-next-token-change!)
     (history/set! (route->token (apply routing/resolve router route-data)))))

  (re-frame/reg-fx
   :navigate!
   (fn navigate!-fx [route-data]
     (ignore-next-token-change!)
     (history/replace! (route->token (apply routing/resolve router route-data)))))

  (history/add-listener! match-and-dispatch-route))
