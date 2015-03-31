(ns comic-reader.api
  (:require [ajax.core :refer [GET POST]]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-sites [callback]
  (GET "/api/v1/sites" {:handler callback
                        :error-handler error-handler
                        :response-format :edn}))
