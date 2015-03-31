(ns comic-reader.main
  (:require [figwheel.client :as fw]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
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

(om/root
 (fn [data owner]
   (om/component
    (dom/div nil
             (dom/h1 nil (:heading data))
             (apply dom/ul nil
                    (map (fn [{:keys [name url]}]
                           (dom/li nil
                                   (dom/a #js {:href url} name)))
                         (:sites data))))))
 app-state
 {:target (. js/document (getElementById "main-area"))})

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(GET "/api/v1/sites" {:handler (fn [sites]
                                 (swap! app-state assoc :sites sites))
                      :error-handler error-handler
                      :response-format :edn})
