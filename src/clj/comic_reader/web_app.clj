(ns comic-reader.web-app
  (:require [clojure.core.async :as async]
            [clojure.string :as str]
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

(defn- accepted-response [location]
  {:status 202
   :headers {"Location" (str "/api/v1/check/" location)}})

(def not-found-response
  {:status 404})

(defn- make-comic-id [site-id comic-id]
  (keyword (format "%s/%s" site-id comic-id)))

(defn- make-api-routes [timeout repository]
  (let [request-cache (atom {})
        chan-response
        (fn chan-response
          ([timeout ch] (chan-response timeout ch (str (java.util.UUID/randomUUID))))
          ([timeout ch id]
           (async/alt!!
             ch ([value] (edn-response value))
             (async/timeout timeout) (do
                                       (swap! request-cache assoc id ch)
                                       (accepted-response id)))))]
   (c/routes
     (c/GET "/sites" []
       (chan-response timeout (repo/list-sites repository)))

     (c/GET "/:site-id/comics" [site-id]
       (chan-response timeout (repo/list-comics repository (keyword site-id))))

     (c/POST "/:site-id/image" [site-id location]
       (chan-response timeout (repo/image-tag repository (keyword site-id) location)))

     (c/POST "/:site-id/:comic-id/previous" [site-id comic-id location n]
       (chan-response timeout (repo/previous-locations repository (make-comic-id site-id comic-id) location n)))

     (c/POST "/:site-id/:comic-id/next" [site-id comic-id location n]
       (chan-response timeout (repo/next-locations repository (make-comic-id site-id comic-id) location n)))

     (c/GET "/check/:id" [id]
       (if-let [ch (get @request-cache id)]
         (chan-response timeout ch id)
         not-found-response)))))

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
  (let [api-routes (cond-> (make-api-routes 400 repository)
                     :always wrap-with-logger
                     :always wrap-params
                     :always wrap-edn-params
                     (not (cfg/testing? config)) wrap-edn-body)]
    (c/routes
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
        api-routes)

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
