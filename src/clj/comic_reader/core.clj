(ns comic-reader.core
  (:gen-class)
  (:require [comic-reader.scrape :as scrape]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core  :as c]
            [compojure.route :as route]))

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(c/defroutes routes
  (c/GET "/" [] (response/redirect "/index.html"))
  (c/GET "/blank" [] "")
  (c/context "/api/v1" []
    (c/GET "/sites" []
      (edn-response (vec (map #(select-keys % [:id :name :url])
                              scrape/sites))))
    (c/GET "/comics/:site" [site]
      (->> scrape/sites
           (some (fn [s]
                   (when (= (:id s)
                            site)
                     s)))
           (scrape/fetch-comic-list site)
           edn-response))
    (c/GET "/comic/:name" [name]
      (str "Hello comic" name)))
  (route/resources "/"))

(def app (wrap-params routes))
