(ns comic-reader.history
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary])
  (:import goog.History))

(defn hook-browser-navigation! []
  (doto (History. false
                  "/blank"
                  (.getElementById js/document "history_state"))
    (events/listen
        EventType/NAVIGATE
        (fn [event]
          (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
