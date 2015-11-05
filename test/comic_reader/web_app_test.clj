(ns comic-reader.web-app-test
  (:require [clojure.test :refer :all]
            [comic-reader.site-scraper :as site-scraper]
            [comic-reader.web-app :refer :all]
            [com.stuartsierra.component :as component]
            [ring.mock.request :as mock]))

(defn server-test-system [scraper]
  (component/system-map
   :site-scraper scraper
   :web-app (component/using
             (new-web-app)
             [:site-scraper])))

(extend-type clojure.lang.IPersistentMap
  site-scraper/ISiteScraper

  (list-sites [this]
    (:sites this)))

(defn test-system [scraper]
  (-> (server-test-system scraper)
      (component/start)))

(defn app-routes [system]
  (get-routes (:web-app system)))

(deftest test-home-page
  (is (= (:status
          ((app-routes (test-system {})) (mock/request :get "/")))
         200)))

(deftest test-api
  (testing "/api/v1"
    (testing "/sites"
      (let [handle (app-routes (test-system {:sites '("site-one" "site-two" "site-three")}))]
        (is (= (handle (mock/request :get "/api/v1/sites"))
               {:status 200
                :headers {"Content-Type" "application/edn"}
                :body "(\"site-one\" \"site-two\" \"site-three\")"}))))))
