(ns comic-reader.api
  (:require [re-frame.core :refer [dispatch]]
            [ajax.core :refer [GET POST]]))

(def *last-error* (atom nil))

(defn error-handler [{:keys [status status-text]
                      :as error-response}]
  (reset! *last-error* error-response)
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-sites []
  (GET "/api/v1/sites" {:handler #(dispatch [:site-list %])
                        :error-handler error-handler
                        :response-format :edn}))

(defn get-comics [site]
  (GET (str "/api/v1/comics/" site)
      {:handler #(dispatch [:comic-list %])
       :error-handler error-handler
       :response-format :edn}))

(defn get-comic-pages [site {:keys [comic chapter page]}]
  (GET (str "/api/v1/pages/" site "/" comic
            "/" chapter "/" page)
      {:handler #(dispatch [:page-list %])
       :error-handler error-handler
       :response-format :edn}))

(defn get-img-tag [site page-info]
  (POST "/api/v1/img"
      {:format :edn
       :params {:site site
                :page-info page-info}
       :handler #(dispatch [:next-image %])
       :error-handler error-handler
       :response-format :edn}))
