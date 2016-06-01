(ns comic-reader.web-app
  (:require [comic-reader.site-scraper :as scraper]
            [compojure.core :as c]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [hiccup.page :as page]
            [garden.core :as garden]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.string :as str]))

(defn- edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn; charset=utf-8"}
   :body (pr-str data)})

(defn- capitalize-all [words]
  (map str/capitalize words))

(defn- spacify [words]
  (str/join " " words))

(defn- titleize [id]
  (-> id
      (str/split #"-")
      capitalize-all
      spacify))

(defn- format-site [site-id]
  {:id site-id
   :name (titleize site-id)})

(defn- make-api-routes [site-scraper]
  (c/routes
    (c/GET "/sites" []
      (edn-response (map format-site (scraper/list-sites site-scraper))))

    (c/GET "/:site-name/comics" [site-name]
      (edn-response (scraper/list-comics site-scraper site-name)))

    ))

(defn- render-page [& {:keys [head css js content]}]
  (page/html5
   `[:head
     ~@head
     ~(page/include-css "css/normalize.css"
                        "css/foundation.min.css"
                        "css/app.css")
     ~@css
     ~(page/include-js "js/vendor/modernizr.js")]
   `[:body ~@content
     ~(page/include-js "js/vendor/jquery.js"
                       "js/vendor/fastclick.js"
                       "js/foundation.min.js")
     ~@js]))

(defn- make-routes [site-scraper]
  (c/routes
    (c/GET "/" []
      (render-page
       :content
       [[:div.row
         [:div#app.small-12.columns]]
        [:input#history_state {:type "hidden"}]]
       :js [(page/include-js "js/compiled/main.js")]))

    (c/GET "/devcards" []
      (render-page
       :content
       [[:div.row
         [:div#cards.small-12.columns]]]
       :css [[:style (garden/css
                      [:.com-rigsomelight-devcards_rendered-card
                       [:a.button {:color "#FFF !important"}]])]]
       :js [(page/include-js "js/compiled/devcards.js")]))

    (c/context "/api/v1" []
      (-> (make-api-routes site-scraper)
          wrap-params
          wrap-edn-params))

    (route/resources "/")))

(defrecord WebApp [routes site-scraper]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Generating web app...")
    (assoc component :routes (make-routes site-scraper)))

  (stop [component]
    (println "Comic-Reader: Tearing down web app...")
    (assoc component :routes nil)))

(defn new-web-app []
  (map->WebApp {}))

(defn get-routes [web-app]
  (:routes web-app))
