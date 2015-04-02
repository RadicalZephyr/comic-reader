(ns comic-reader.history
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary])
  (:import goog.History))

(defonce goog-history (atom nil))

(defn hook-browser-navigation! []
  (when (nil? @goog-history)
    (let [hist (History. false
                         "/blank"
                         (.getElementById js/document
                                          "history_state"))]
      (reset! goog-history hist)
      (doto hist
        (events/listen
            EventType/NAVIGATE
            (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))))
