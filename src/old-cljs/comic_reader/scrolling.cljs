(ns comic-reader.scrolling
  (:require [comic-reader.events :as e]
            [re-frame.core :as rf]))

(defn setup-scrolling-events! []
  (e/throttle "scroll" #(rf/dispatch [:scroll]) 1000))
