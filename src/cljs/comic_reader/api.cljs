(ns comic-reader.api
  (:require [re-frame.core :refer [dispatch]]
            [ajax.core :refer [GET POST]]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-sites []
  (GET "/api/v1/sites" {:handler #(dispatch [:site-list %])
                        :error-handler error-handler
                        :response-format :edn}))

(defn get-comics [site]
  (GET (str "/api/v1/comics/" (pr-str site))
      {:handler #(dispatch [:comic-list %])
       :error-handler error-handler
       :response-format :edn}))

(defn get-comic-urls [comic-name]
  (GET (str "/api/v1/comic/" (pr-str comic-name))
      {:handler #(dispatch [:comic-urls %])
       :error-handler error-handler
       :response-format :edn}))
