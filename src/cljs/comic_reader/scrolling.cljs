(ns comic-reader.scrolling
  (:require [re-frame.core :as rf]))

(defn throttle [type key & [target]]
  (let [target (or target js/window)
        running (atom false)
        toggle! #(swap! running not)
        throttler (fn []
                    (when (not @running)
                      (toggle!)
                      (js/requestAnimationFrame
                       (fn []
                         (rf/dispatch [key])
                         (toggle!)))))]
    (.addEventListener target type throttler)))

(defn setup-scrolling-events! []
  (throttle "scroll" :scroll))
