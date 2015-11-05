(ns comic-reader.web-app-test
  (:require [clojure.test :refer :all]
            [comic-reader.site-scraper :as site-scraper]
            [comic-reader.web-app :refer :all]
            [com.stuartsierra.component :as component]
            [ring.mock.request :as mock]))

(defn server-test-system []
  (component/system-map
   :site-scraper {}
   :web-app (component/using
             (new-web-app)
             [:site-scraper])))

(defn test-system []
  (-> (server-test-system)
      (component/start)))

(defn app-routes [system]
  (get-routes (:web-app system)))

(deftest test-home-page
  (is (= (:status
          ((app-routes (test-system)) (mock/request :get "/")))
         200)))
