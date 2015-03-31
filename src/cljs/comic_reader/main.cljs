(ns comic-reader.main
  (:require [figwheel.client :as fw]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(fw/start {
           ;; configure a websocket url if you are using your own server
           :websocket-url "ws://localhost:3449/figwheel-ws"

           ;; optional callback
           :on-jsload (fn [] (print "reloaded"))

           ;; The heads up display is enabled by default
           ;; to disable it:
           ;; :heads-up-display false

           ;; when the compiler emits warnings figwheel
           ;; blocks the loading of files.
           ;; To disable this behavior:
           ;; :load-warninged-code true

           ;; if figwheel is watching more than one build
           ;; it can be helpful to specify a build id for
           ;; the client to focus on
           :build-id "dev"})

(defonce app-state (atom {:heading "Comic Sites"}))

(defn manga-site [site-data]
  [:li (:name site-data)])

(defn site-list [data]
  (let [data @data]
    [:div
     [:h1 (:heading data)]
     [:ul (map manga-site (:sites data))]]))

(defn mountit []
  (reagent/render-component [site-list app-state]
                            (.-body js/document)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(GET "/api/v1/sites" {:handler (fn [sites]
                                 (swap! app-state assoc :sites sites))
                      :error-handler error-handler
                      :response-format :edn})

(mountit)
