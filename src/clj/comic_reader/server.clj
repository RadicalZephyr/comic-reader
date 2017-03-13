(ns comic-reader.server
  (:require [clojure.tools.logging :as log]
            [comic-reader.config :as config]
            [comic-reader.web-app :as web-app]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as server]))

(defrecord WebServer [config web-app stop-server]
  component/Lifecycle

  (start [component]
    (if stop-server
      component
      (let [port (Integer. (config/server-port config))]
        (log/info (format "Starting web server on port: %d ...\n" port))
        (assoc component
               :stop-server (server/run-server (web-app/get-routes web-app)
                                               {:port port})))))

  (stop [component]
    (if-not stop-server
      component
      (do
        (log/info "Shutting down web server...")
        (stop-server)
        (assoc component
               :stop-server nil)))))

(defn new-server []
  (map->WebServer {}))
