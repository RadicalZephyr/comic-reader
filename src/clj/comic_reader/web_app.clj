(ns comic-reader.web-app
  (:require [comic-reader.site-scraper :as scraper]
            [compojure.core :as c]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [hiccup.page :as page]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defn- edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn- make-api-routes [site-scraper]
  (c/routes
    (c/GET "/sites" []
      (edn-response (scraper/list-sites site-scraper)))

    (c/GET "/:site-name/comics" [site-name]
      (edn-response (scraper/list-comics site-scraper site-name)))

    (c/GET "/:site-name/:comic-id/chapters" [site-name comic-id]
      (edn-response (scraper/list-chapters site-scraper site-name comic-id)))

    (c/GET "/pages/:site/:comic/:chapter{\\d+}/:page{\\d+}"
        request
      )

    (c/POST "/img" {{:keys [site]
                     {:keys [chapter page url]} :page-info}
                    :edn-params
                    :as request}
      )))

(defn- make-routes [site-scraper]
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

(defrecord WebApp [routes site-scraper]
  component/Lifecycle

  (start [component]
    (println ";; Generating web app...")
    (assoc component :routes (make-routes site-scraper)))

  (stop [component]
    (println ";; Tearing down web app...")
    (assoc component :routes nil)))

(defn new-web-app []
  (map->WebApp {}))

(defn get-routes [web-app]
  (:routes web-app))
