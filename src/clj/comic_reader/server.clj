(ns comic-reader.server
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [hiccup.page :as page]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.params :refer [wrap-params]])
  (:import org.eclipse.jetty.server.Server))

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn make-api-routes [site-scraper]
  (c/routes
    (c/GET "/sites" []
      (edn-response ()
                    ))

    (c/GET "/comics/:site" [site]
      )

    (c/GET "/pages/:site/:comic/:chapter{\\d+}/:page{\\d+}"
        request
      )

    (c/POST "/img" {{:keys [site]
                     {:keys [chapter page url]} :page-info}
                    :edn-params
                    :as request}
      )))

(defn make-routes [site-scraper]
  (c/routes
    (c/GET "/" []
      (page/html5
       [:head
        (page/include-css "css/normalize.css"
                          "css/foundation.min.css"
                          "css/app.css")
        (page/include-js "js/vendor/modernizr.js")]
       [:body
        [:div.row
         [:div#app.small-12.columns]]
        [:input#history_state {:type "hidden"}]
        (page/include-js "js/vendor/jquery.js"
                         "js/vendor/fastclick.js"
                         "js/foundation.min.js"
                         "js/compiled/main.js")]))

    (c/context "/api/v1" []
      (-> (make-api-routes site-scraper)
          wrap-params
          wrap-edn-params))

    (route/resources "/")))

(defrecord WebServer [port routes ^Server server site-scraper]
  component/Lifecycle

  (start [component]
    (if server
      component
      (let [routes (make-routes site-scraper)]
        (println "Starting web server...")
        (assoc component
               :routes routes
               :server (run-jetty routes {:port port
                                          :join? false})))))

  (stop [component]
    (if (not server)
      component
      (do
        (when (or (not (.isStopped server))
                  (not (.isStopping server)))
          (println "Shutting down web server...")
          (.stop server))
        (assoc component
               :routes nil
               :server nil)))))

(defn new-server [port]
  (map->WebServer {:port port}))
