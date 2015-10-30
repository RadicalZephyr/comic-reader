(ns comic-reader.events)

(defn throttle [type fire-event delay & [target]]
  (let [target (or target js/window)
        running (atom false)
        toggle! #(swap! running not)
        throttler (fn []
                    (when (not @running)
                      (toggle!)
                      (js/requestAnimationFrame
                       (fn []
                         (fire-event)
                         (.setTimeout js/window
                                      #(toggle!)
                                      delay)))))]
    (.addEventListener target type throttler)))
