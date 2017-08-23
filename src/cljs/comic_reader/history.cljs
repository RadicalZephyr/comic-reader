(ns comic-reader.history
  (:refer-clojure :exclude [get])
  (:require [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import (goog History)))

(defonce h (History. false))

(defn add-listener! [listener]
  (events/listen h EventType/NAVIGATE #(listener (.-token %))))

(defn setup! []
  (.setEnabled h true))

(defn get []
  (.getToken h))

(defn set! [token & [title]]
  (.setToken h token (or title "")))

(defn replace! [token & [title]]
  (.replaceToken h token (or title "")))
