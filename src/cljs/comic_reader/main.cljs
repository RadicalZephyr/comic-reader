(ns comic-reader.main
  (:require [figwheel.client :as fw]
            [comic-reader.api :as api]
            [comic-reader.session :as session]
            [comic-reader.pages.sites
             :refer [site-list]]
            [comic-reader.pages.comics
             :refer [comic-list]]
            [comic-reader.pages.viewer
             :refer [comic-viewer]]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary
             :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(enable-console-print!)

;; TODO: Put figwheel setups into some kind of dev namespace
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

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
        EventType/NAVIGATE
        (fn [event]
          (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defroute "/" []
  (session/put! :current-page site-list))

(defroute "/site/:site" [site]
  (session/put! :current-page comic-list))

(defroute "/comic/:comic/:volume/:page" [comic volume page]
  (session/put! :current-page comic-viewer))

(defn page [data]
  [(session/get :current-page) data])

(defn init! []
  (secretary/set-config! :prefix "#")
  (session/put! :current-page site-list)
  (api/get-sites
   (fn [data]
     (reagent/render-component [page data]
                               (.-body js/document)))))

(init!)
