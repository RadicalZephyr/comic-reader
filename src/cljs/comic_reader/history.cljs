(ns comic-reader.history
  (:require [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(defonce goog-history (atom nil))

(defn setup-history! []
  (when (nil? @goog-history)
    (let [hist (History. false
                         "/blank"
                         (.getElementById js/document
                                          "history_state"))]
      (reset! goog-history hist))))
