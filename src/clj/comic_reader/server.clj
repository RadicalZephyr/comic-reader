(ns comic-reader.server
  (:require [comic-reader.web-app :as web-app]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]])
  (:import org.eclipse.jetty.server.Server))

(defrecord WebServer [port ^Server server web-app]
  component/Lifecycle

  (start [component]
    (if server
      component
      (do
        (printf "Comic-Reader: Starting web server on port: %d ...\n"
                port)
        (assoc component
               :server (run-jetty (web-app/get-routes web-app)
                                  {:port port
                                   :join? false})))))

  (stop [component]
    (if-not server
      component
      (do
        (when-not (or (.isStopped server)
                      (.isStopping server))
          (println "Comic-Reader: Shutting down web server...")
          (.stop server))
        (if (.isStopped server)
          (assoc component
                 :web-app nil
                 :server nil)
          component)))))

(defn new-server [port]
  (map->WebServer {:port port}))
