(ns comic-reader.history
  (:require [re-frame.core :as rf]
            [secretary.core :refer [dispatch!]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))

(defonce goog-history (atom nil))

(defn hook-browser-navigation! []
  (when (nil? @goog-history)
    (let [hist (Html5History. false
                         "/blank"
                         (.getElementById js/document
                                          "history_state"))]
      (reset! goog-history hist)
      (events/listen hist
          EventType/NAVIGATE
        (fn [event]
          (dispatch! (.-token event))))
      (doto hist
        (.setEnabled true)))))

(defn get-token []
  (when-let [h @goog-history]
    (.getToken h)))

(defn set-token [token]
  (when-let [h @goog-history]
    (.setToken h (str token))))
