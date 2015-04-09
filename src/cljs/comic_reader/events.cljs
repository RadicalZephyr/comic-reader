(ns comic-reader.events)

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
                         (.setTimeout js/window
                                      #(toggle!)
                                      1000)))))]
    (.addEventListener target type throttler)))
