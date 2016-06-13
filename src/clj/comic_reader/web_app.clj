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
            [ring.middleware.params :refer [wrap-params]]))

(defn- wrap-edn-body [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (nil? (:body response))
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

(defn- make-routes [repository testing?]
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
      (cond-> (make-api-routes repository)
        :always wrap-params
        :always wrap-edn-params
        (not testing?) wrap-edn-body))

    (route/resources "/")))

(defrecord WebApp [config routes repository]
  component/Lifecycle

  (start [component]
    (log/info "Generating web app...")
    (assoc component :routes (make-routes repository (cfg/testing? config))))

  (stop [component]
    (log/info "Tearing down web app...")
    (assoc component :routes nil)))

(defn new-web-app []
  (map->WebApp {}))

(defn get-routes [web-app]
  (:routes web-app))
