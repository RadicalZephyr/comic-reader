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

(defn gen-error-data [request & {:as others}]
  (-> request
      (select-keys [:uri
                    :request-method
                    :params])
      (merge others)))

(defn get-comics-list [{{:keys [site]} :params
                        :as request}]
  (if-let [comic-list (scrape/fetch-list
                       (sites/comic-list-data (keyword site)))]
    (edn-response comic-list)
    (edn-response (gen-error-data request) 404)))

(defn get-comic-chapter [site comic chapter]
  (first
   (drop (dec chapter)
         (sort-by :url
                  (scrape/fetch-list
                   (sites/chapter-list-data site
                                            comic))))))

(defn get-following-pages [site comic chapter-map page]
  (drop (dec page)
        (scrape/fetch-list
         (sites/page-list-data site
                               (:url chapter-map)))))

(defn get-comic-imgs [{{:keys [site comic chapter page]} :params
                       :as request}]
  (println (:uri request))
  (let [chapter-map (get-comic-chapter site comic
                                       (int chapter))
        page-list (get-following-pages site comic
                                       chapter-map
                                       (int page))]
    (if-let [comic-urls nil]
      (edn-response)
      (edn-response (gen-error-data request) 404))))

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
      (edn-response
       (vec (map #(select-keys % [:id :name :url])
                 sites/list))))

    (c/GET "/comics/:site" request
      (get-comics-list request))

    (c/GET "/imgs/:site/:comic/:chapter{\\d+}/:page{\\d+}"
        request
      (get-comic-imgs request)))

  (route/resources "/"))

(def app (wrap-params routes))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty app {:port port :join? false})))

(defn -main [& [port]]
  (run-web-server port))
