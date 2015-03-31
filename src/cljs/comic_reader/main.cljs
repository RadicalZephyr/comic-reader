(ns comic-reader.main
  (:require [figwheel.client :as fw]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

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

(defonce app-state (atom {:text "Om..."}))

(om/root
 (fn [data owner]
   (reify om/IRender
     (render [_]
       (dom/h1 nil (:text data)))))
 app-state
 {:target (. js/document (getElementById "main-area"))})
