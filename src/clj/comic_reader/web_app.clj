(ns comic-reader.web-app
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [comic-reader.comic-repository :as repo]
            [comic-reader.config :as cfg]
            [compojure.core :as c]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [hiccup.page :as page]
            [garden.core :as garden]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.params :refer [wrap-params]]))

(defn- wrap-edn-body [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (seqable? (:body response))
               (nil? (seq (:body response)))
               (:edn-body response))
        (-> response
            (assoc :body (pr-str (:edn-body response)))
            (dissoc :edn-body))
        response))))

(defn- edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn; charset=utf-8"}
   :edn-body data})

(defn- make-api-routes [repository]
  (c/routes
    (c/GET "/sites" []
      (edn-response (repo/list-sites repository)))

    (c/GET "/:site-id/comics" [site-id]
      (edn-response (repo/list-comics repository site-id)))

    (c/POST "/:site-id/image" [site-id location]
      (edn-response (repo/image-tag repository site-id location)))

    (c/POST "/:site-id/:comic-id/previous" [site-id comic-id location n]
      (edn-response (repo/previous-locations repository site-id comic-id location n)))

    (c/POST "/:site-id/:comic-id/next" [site-id comic-id location n]
      (edn-response (repo/next-locations repository site-id comic-id location n)))))

(defn- render-page [& {:keys [head css js content]}]
  (page/html5
   `[:head
     [:link {:rel "apple-touch-icon" :sizes "57x57" :href "/apple-icon-57x57.png"}]
     [:link {:rel "apple-touch-icon" :sizes "60x60" :href "/apple-icon-60x60.png"}]
     [:link {:rel "apple-touch-icon" :sizes "72x72" :href "/apple-icon-72x72.png"}]
     [:link {:rel "apple-touch-icon" :sizes "76x76" :href "/apple-icon-76x76.png"}]
     [:link {:rel "apple-touch-icon" :sizes "114x114" :href "/apple-icon-114x114.png"}]
     [:link {:rel "apple-touch-icon" :sizes "120x120" :href "/apple-icon-120x120.png"}]
     [:link {:rel "apple-touch-icon" :sizes "144x144" :href "/apple-icon-144x144.png"}]
     [:link {:rel "apple-touch-icon" :sizes "152x152" :href "/apple-icon-152x152.png"}]
     [:link {:rel "apple-touch-icon" :sizes "180x180" :href "/apple-icon-180x180.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "/android-icon-192x192.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "/favicon-32x32.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "/favicon-96x96.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "/favicon-16x16.png"}]
     [:link {:rel "manifest" :href "/manifest.json"}]
     [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
     [:meta {:name "msapplication-TileImage" :content "/ms-icon-144x144.png"}]
     [:meta {:name "theme-color" :content "#ffffff"}]
     ~@head
     ~(page/include-css "/public/css/normalize.css"
                        "/public/css/foundation.min.css"
                        "/public/css/app.css")
     ~@css
     ~(page/include-js "/public/js/vendor/modernizr.js")]
   `[:body ~@content
     ~(page/include-js "/public/js/vendor/jquery.js"
                       "/public/js/vendor/fastclick.js"
                       "/public/js/foundation.min.js")
     ~@js]))

(defn- make-routes [repository config]
  (-> (c/routes
        (c/GET "/" []
          (render-page
           :content
           [[:div.row
             [:div#app.small-12.columns]]
            [:input#history_state {:type "hidden"}]]
           :js [(page/include-js "/public/js/main.js")
                [:script "comic_reader.main.main();"]]))

        (c/GET "/devcards" []
          (render-page
           :content
           [[:div.row
             [:div#cards.small-12.columns]]]
           :css [[:style (garden/css
                          [:.com-rigsomelight-devcards_rendered-card
                           [:a.button {:color "#FFF !important"}]])]]
           :js [(page/include-js "/public/js/devcards.js")]))

        (c/context "/api/v1" []
          (cond-> (make-api-routes repository)
            :always wrap-with-logger
            :always wrap-params
            :always wrap-edn-params
            (not (cfg/testing? config)) wrap-edn-body))

        (route/resources "/public"))))

(defrecord WebApp [config routes repository]
  component/Lifecycle

  (start [component]
    (log/info "Generating web app...")
    (assoc component :routes (make-routes repository config)))

  (stop [component]
    (log/info "Tearing down web app...")
    (assoc component :routes nil)))

(defn new-web-app []
  (map->WebApp {}))

(defn get-routes [web-app]
  (:routes web-app))
