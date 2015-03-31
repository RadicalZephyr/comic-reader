(ns comic-reader.main
  (:require [figwheel.client :as fw]
            [comic-reader.session :as session]
            [comic-reader.pages.sites :as sites]
            [comic-reader.pages.comics :as comics]
            [comic-reader.pages.reader :as reader]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary
             :include-macros true :refer [defroute]]
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

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(GET "/api/v1/sites" {:handler (fn [sites]
                                 (swap! app-state assoc :sites sites))
                      :error-handler error-handler
                      :response-format :edn})
