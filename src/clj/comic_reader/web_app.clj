(ns comic-reader.web-app
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [comic-reader.comic-repository :as repo]
            [compojure.core :as c]
            [compojure.route :as route]
            [com.stuartsierra.component :as component]
            [hiccup.page :as page]
            [garden.core :as garden]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defn- edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn; charset=utf-8"}
   :body (pr-str data)})

(defn- make-api-routes [repository]
  (c/routes
    (c/GET "/sites" []
      (edn-response (repo/list-sites repository)))

    (c/GET "/:site-id/comics" [site-id]
      (edn-response (repo/list-comics repository site-id)))

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

(defn- make-routes [repository]
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
      (-> (make-api-routes repository)
          wrap-params
          wrap-edn-params))

    (route/resources "/")))

(defrecord WebApp [routes repository]
  component/Lifecycle

  (start [component]
    (log/info "Generating web app...")
    (assoc component :routes (make-routes repository)))

  (stop [component]
    (log/info "Tearing down web app...")
    (assoc component :routes nil)))

(defn new-web-app []
  (map->WebApp {}))

(defn get-routes [web-app]
  (:routes web-app))
