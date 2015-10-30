(ns comic-reader.server
  (:gen-class)
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

(c/defroutes routes
  (c/GET "/" [] (page/html5
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
    (c/GET "/sites" []
      (edn-response
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
      ))

  (route/resources "/"))

(def app (-> routes
             wrap-params
             wrap-edn-params))

(defrecord WebServer [app port ^Server server site-scraper]
  component/Lifecycle

  (start [component]
    (if server
      component
      (do
        (println "Starting web server...")
        (assoc component
               :server (run-jetty app {:port port
                                       :join? false})))))

  (stop [component]
    (if (not server)
      component
      (do
        (when (or (not (.isStopped server))
                  (not (.isStopping server)))
          (println "Shutting down web server...")
          (.stop server))
        (assoc component :server nil)))))

(defn new-server [app port]
  (map->WebServer {:app app
                   :port port}))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty app {:port port :join? false})))

(defn -main [& [port]]
  (run-web-server port))
