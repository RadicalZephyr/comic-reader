(ns comic-reader.system
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [comic-reader.server :as server]
            [comic-reader.sites :as sites]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defn comic-reader-system [config-options]
  (let [{:keys [app port]} config-options]
    (component/system-map
     :site-scraper (sites/new-site-scraper)
     :server       (component/using
                    (server/new-server app port)
                    [:site-scraper]))))

(def system nil)

(defn init [config-options]
  (alter-var-root #'system
    (constantly (comic-reader-system config-options))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go [config-options]
  (init config-options)
  (start))

(defn reset []
  (stop)
  (refresh :after 'comic-reader.system/-main))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (go {:app server/app
         :port port})))
