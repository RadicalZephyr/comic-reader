(ns comic-reader.server
  (:require [comic-reader.web-app :as web-app]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as server]))

(defrecord WebServer [port stop-server web-app]
  component/Lifecycle

  (start [component]
    (if stop-server
      component
      (do
        (printf "Comic-Reader: Starting web server on port: %d ...\n" port)
        (assoc component
               :stop-server (server/run-server (web-app/get-routes web-app)
                                               {:port port})))))

  (stop [component]
    (if-not stop-server
      component
      (do
        (println "Comic-Reader: Shutting down web server...")
        (stop-server)
        (assoc component
               :stop-server nil)))))

(defn new-server [port]
  (map->WebServer {:port port}))
