(ns comic-reader.system
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            (comic-reader [database :as database]
                          [server :as server]
                          [web-app :as web-app]
                          [site-scraper :as site-scraper])
            [comic-reader.config.env :as env-config]
            [comic-reader.comic-repository.scraper :as scraper-repo]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defn comic-reader-system []
  (component/system-map
   :config (env-config/new-env-config {:server-port 10555})
   :site-scraper (site-scraper/new-site-scraper)
   :comic-repository (component/using
                      (scraper-repo/new-scraper-repo)
                      {:scraper :site-scraper})
   :web-app      (component/using
                  (web-app/new-web-app)
                  {:config :config
                   :repository :comic-repository})
   :server       (component/using
                  (server/new-server)
                  [:config :web-app])
   #_:database #_(component/using
                  (database/new-database)
                  [:config])))

(def system nil)

(defn init []
  (alter-var-root #'system
    (constantly (comic-reader-system))))

(defn start []
  (when (deref #'system)
    (alter-var-root #'system component/start)))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'comic-reader.system/go))

(defn -main [& args]
  (go))
