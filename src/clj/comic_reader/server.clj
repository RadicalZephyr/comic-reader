(ns comic-reader.server
  (:gen-class)
  (:require [comic-reader.sites :as sites]
            [comic-reader.scrape :as scrape]
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core  :as c]
            [compojure.route :as route]
            [hiccup.page :as hp]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.tools.reader.edn :as edn]))

(defn unknown-val [tag val]
  {:unknown-tag tag
   :value val})

(defn safe-read-string [s]
  (edn/read-string {:default unknown-val} s))

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
  (let [site (keyword site)]
    (if-let [comic-list (scrape/fetch-list
                         (sites/comic-list-data site))]
      (edn-response comic-list)
      (edn-response (gen-error-data request) 404))))

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

(defn get-comic-urls [{{:keys [site comic chapter page]} :params
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
    (if-let [comic-urls (map-indexed add-location
                                     page-list)]
      (edn-response comic-urls)
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

    (c/GET "/urls/:site/:comic/:chapter{\\d+}/:page{\\d+}"
        request
      (get-comic-urls request))

    (c/POST "/img" {{:keys [site url]} :edn-params
                    :as request}
      (let [site (keyword site)]
        (if-let [img-tag (scrape/fetch-image-tag
                          (sites/image-data site
                                            url))]
          (edn-response img-tag)
          (edn-response (gen-error-data request) 404)))))

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
