(ns comic-reader.system
  (:gen-class)
  (:require (comic-reader [server :as server]
                          [web-app :as web-app]
                          [site-scraper :as site-scraper])
            [comic-reader.config.env :as env-config]
            (comic-reader.comic-repository [cache :as cache-repo]
                                           [datomic :as datomic-repo]
                                           [scraper :as scraper-repo])
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defn comic-reader-system []
  (component/system-map
   :config (env-config/new-env-config {:database-uri "datomic:mem://comics"
                                       :norms-dir "database/norms"
                                       :server-port 10555})
   :datomic-repo (component/using
                  (datomic-repo/new-datomic-repository)
                  [:config])
   :site-scraper (site-scraper/new-site-scraper)
   :scraper-repo (component/using
                  (scraper-repo/new-scraper-repo)
                  {:scraper :site-scraper})
   :repository   (component/using
                  (cache-repo/new-caching-repository)
                  {:source-repo  :scraper-repo
                   :storage-repo :datomic-repo})
   :web-app      (component/using
                  (web-app/new-web-app)
                  {:config :config
                   :repository :scraper-repo})
   :server       (component/using
                  (server/new-server)
                  [:config :web-app])))

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

(defn -main [& args]
  (go))
