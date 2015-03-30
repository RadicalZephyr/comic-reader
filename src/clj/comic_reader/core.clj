(ns comic-reader.core
  (:gen-class)
  (:require [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core  :as c]
            [compojure.route :as route]))

(c/defroutes routes
  (c/GET "/" [] "Hello World!")
  (c/context "/api/v1" []
    (c/GET "/comics" []
      "All the comics.")
    (c/GET "/comic/:name" [name]
      (str "Hello comic" name)))
  (route/resources "/"))

(def app (wrap-params routes))
