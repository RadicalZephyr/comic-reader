(ns comic-reader.server
  (:gen-class)
  (:require [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites :refer [sites]]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]
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
  )

(defn get-comic-chapter [site comic chapter]
  )

(defn get-following-pages [site comic chapter-map page]
  )

(defn get-comic-pages [{{:keys [site comic chapter page]} :params
                        :as request}]
  (let [site (keyword site)
        chapter (safe-read-string chapter)
        page    (safe-read-string page)
        add-location (fn [idx comic-info]
                       (assoc comic-info
                              :chapter chapter
                              :page (+ page idx)))
        chapter-map (get-comic-chapter site comic chapter)
        page-list (get-following-pages site comic
                                       chapter-map
                                       page)]
    (if-let [comic-pages (map-indexed add-location
                                      page-list)]
      (edn-response comic-pages)
      (edn-response (gen-error-data request) 404))))

(c/defroutes routes
  (c/GET "/" [] (hp/html5
                 [:head
                  (hp/include-css "css/normalize.css"
                                  "css/foundation.min.css"
                                  "css/app.css")
                  (hp/include-js "js/vendor/modernizr.js")]
                 [:body
                  [:div.row
                   [:div#app.small-12.columns]]
                  [:input#history_state {:type "hidden"}]
                  (hp/include-js "js/vendor/jquery.js"
                                 "js/vendor/fastclick.js"
                                 "js/foundation.min.js"
                                 "js/compiled/main.js")]))
  (c/GET "/blank" [] "")
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

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty app {:port port :join? false})))

(defn -main [& [port]]
  (run-web-server port))
