(ns comic-reader.web-app-test
  (:require [clojure.test :as t]
            [comic-reader.site-scraper :as site-scraper]
            [comic-reader.web-app :refer :all]
            [comic-reader.mock-site-scraper :refer [mock-scraper]]
            [com.stuartsierra.component :as component]
            [ring.mock.request :as mock]))

(defn server-test-system [scraper]
  (component/system-map
   :site-scraper scraper
   :web-app (component/using
             (new-web-app)
             [:site-scraper])))

(defn test-system [scraper]
  (-> (server-test-system scraper)
      (component/start)))

(defn app-routes [system]
  (get-routes (:web-app system)))

(t/deftest test-home-page
  (t/is (= (:status
            ((app-routes (test-system {})) (mock/request :get "/")))
           200)))

(def edn-content-type {"Content-Type" "application/edn; charset=utf-8"})

(t/deftest test-api
  (t/testing "/api/v1"

    (t/testing "/sites"
      (let [handle (app-routes
                    (test-system (mock-scraper
                                  :sites '("site-one"
                                           "site-two"
                                           "site-three"))))]

        (t/is (= (handle (mock/request :get "/api/v1/sites"))
                 {:status 200
                  :headers edn-content-type
                  :body (str
                         "({:id \"site-one\", :name \"Site One\"}"
                         " {:id \"site-two\", :name \"Site Two\"}"
                         " {:id \"site-three\", :name \"Site Three\"})")}))))

    (t/testing "/:site-name/comics"
      (let [handle (app-routes
                    (test-system (mock-scraper
                                  :comics {"manga-here"
                                           [{:id "the_gamer"
                                             :name "The Gamer"
                                             :url "real_url"}
                                            {:id "other_comic"
                                             :name "Other Comic"
                                             :url "another_url"}]})))]

        (t/is (= (handle (mock/request :get "/api/v1/manga-here/comics"))
                 {:status 200
                  :headers edn-content-type
                  :body (str "[{:id \"the_gamer\", :name \"The Gamer\", :url \"real_url\"}"
                             " {:id \"other_comic\", :name \"Other Comic\", :url \"another_url\"}]")}))))))
