(ns comic-reader.system
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            (comic-reader [config :as config]
                          [database :as database]
                          [server :as server]
                          [web-app :as web-app]
                          [site-scraper :as sites])
            [comic-reader.comic-repository.scraper :as scraper-repo]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defn comic-reader-system [config-options]
  (let [{:keys [port]} config-options]
    (component/system-map
     :config (config/new-config)
     :site-scraper (sites/new-site-scraper)
     :comic-repository (component/using
                        (scraper-repo/new-scraper-repo)
                        {:scraper :site-scraper})
     :web-app      (component/using
                    (web-app/new-web-app)
                    {:repository :comic-repository})
     :server       (component/using
                    (server/new-server port)
                    [:web-app])
     #_:database #_(component/using
                    (database/new-database)
                    [:config]))))

(def system nil)

(defn init [config-options]
  (alter-var-root #'system
    (constantly (comic-reader-system config-options))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (init {:port port})
    (start)))

(defn reset []
  (stop)
  (refresh :after 'comic-reader.system/go))

(defn -main [& args]
  (apply go args))
