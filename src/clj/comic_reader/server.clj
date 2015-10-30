(ns comic-reader.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]])
  (:import org.eclipse.jetty.server.Server))

(defrecord WebServer [port ^Server server web-app]
  component/Lifecycle

  (start [component]
    (if server
      component
      (do
        (println "Starting web server...")
        (assoc component
               :server (run-jetty (:routes web-app)
                                  {:port port
                                   :join? false})))))

  (stop [component]
    (if (not server)
      component
      (do
        (when (or (not (.isStopped server))
                  (not (.isStopping server)))
          (println "Shutting down web server...")
          (.stop server))
        (assoc component
               :web-app nil
               :server nil)))))

(defn new-server [port]
  (map->WebServer {:port port}))
