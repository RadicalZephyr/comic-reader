(ns comic-reader.web-app-test
  (:require [clojure.test :refer :all]
            [comic-reader.web-app :refer :all]
            [com.stuartsierra.component :as component]
            [ring.mock.request :as mock]))

(defn server-test-system []
  (component/system-map
   :site-scraper {}
   :web-app (component/using
             (new-web-app)
             [:site-scraper])))
