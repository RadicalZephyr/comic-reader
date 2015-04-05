(ns comic-reader.server
  (:gen-class)
  (:require [comic-reader.sites :as sites]
            [comic-reader.scrape :as scrape]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core  :as c]
            [compojure.route :as route]
            [hiccup.page :as hp]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(c/defroutes routes
  (c/GET "/" [] (hp/html5
                 [:head]
                 [:body
                  [:div#app]
                  [:input#history_state {:type "hidden"}]
                  (hp/include-js "js/compiled/main.js")]))
  (c/GET "/blank" [] "")
  (c/context "/api/v1" []
    (c/GET "/sites" []
      (edn-response (vec (map #(select-keys % [:id :name :url])
                              sites/list))))

    (c/GET "/comics/:site" [site :as r]
      (let [site (keyword site)]
        (if-let [comic-list (scrape/fetch-list
                             (sites/comic-list-data site))]
         (edn-response comic-list)
         (let [error-data (->
                           r
                           (select-keys [:uri
                                         :request-method
                                         :params])
                           (merge {:site site}))]
           (edn-response error-data 404)))))

    (c/GET "/imgs/:site/:comic/:chapter{\\d+}/:page{\\d+}"
        [site comic chapter page]
      (edn-response ["some-urls"])))

  (route/resources "/"))

(def app (wrap-params routes))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty app {:port port :join? false})))

(defn -main [& [port]]
  (run-web-server port))
