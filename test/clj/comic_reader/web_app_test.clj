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
    (:sites this))

  (list-comics [this site-name]
    (get-in this [:comics site-name]))

  (list-chapters [this site-name comic-id]
    (get-in this [:chapters site-name comic-id]))

  (list-pages [this site-name comic-chapter]
    (get-in this [:pages site-name comic-chapter]))

  (get-page-image [this site-name comic-page]
    (get-in this [:images site-name comic-page])))

(defn test-system [scraper]
  (-> (server-test-system scraper)
      (component/start)))

(defn app-routes [system]
  (get-routes (:web-app system)))

(deftest test-home-page
  (is (= (:status
          ((app-routes (test-system {})) (mock/request :get "/")))
         200)))

(def edn-content-type {"Content-Type" "application/edn; charset=utf-8"})

(deftest test-api
  (testing "/api/v1"

    (testing "/sites"
      (let [handle (app-routes
                    (test-system {:sites '("site-one"
                                           "site-two"
                                           "site-three")}))]

        (is (= (handle (mock/request :get "/api/v1/sites"))
               {:status 200
                :headers edn-content-type
                :body "(\"site-one\" \"site-two\" \"site-three\")"}))))

    (testing "/:site-name/comics"
      (let [handle (app-routes
                    (test-system {:comics {"manga-here"
                                           [{:id "the_gamer"
                                             :name "The Gamer"
                                             :url "real_url"}
                                            {:id "other_comic"
                                             :name "Other Comic"
                                             :url "another_url"}]}}))]

        (is (= (handle (mock/request :get "/api/v1/manga-here/comics"))
               {:status 200
                :headers edn-content-type
                :body (str "[{:id \"the_gamer\", :name \"The Gamer\", :url \"real_url\"}"
                           " {:id \"other_comic\", :name \"Other Comic\", :url \"another_url\"}]")}))))

    (testing "/:site-name/:comic-id/chapters"
      (let [handle (app-routes
                    (test-system {:chapters {"manga-here"
                                             {"the_gamer" [{:name "The Gamer 1",
                                                            :url "http://www.mangahere.co/manga/the_gamer/c001/",
                                                            :ch-num 1}
                                                           {:name "The Gamer 2",
                                                            :url "http://www.mangahere.co/manga/the_gamer/c002/",
                                                            :ch-num 2}]}}}))]

        (is (= (handle (mock/request :get "/api/v1/manga-here/the_gamer/chapters"))
               {:status 200
                :headers edn-content-type
                :body (str
                       "[{:name \"The Gamer 1\","
                       " :url \"http://www.mangahere.co/manga/the_gamer/c001/\","
                       " :ch-num 1} "
                       "{:name \"The Gamer 2\","
                       " :url \"http://www.mangahere.co/manga/the_gamer/c002/\","
                       " :ch-num 2}]")}))))

    (testing "/:site-name/:comic-id/pages"
      (let [handle (app-routes
                    (test-system {:pages {"manga-here"
                                          {{:name "The Gamer 2", :url "http://www.mangahere.co/manga/the_gamer/c002/", :ch-num 2}
                                           [{:name "1", :url  "http://www.mangahere.co/manga/the_gamer/c002/"}
                                            {:name "2", :url  "http://www.mangahere.co/manga/the_gamer/c002/2.html"}]}}}))]

        (is (= (handle (-> (mock/request :post "/api/v1/manga-here/pages"
                                         (str
                                          "{:comic-chapter"
                                          " {:name \"The Gamer 2\","
                                          " :url \"http://www.mangahere.co/manga/the_gamer/c002/\","
                                          " :ch-num 2}}"))
                           (mock/content-type "application/edn")))
               {:status 200
                :headers edn-content-type
                :body (str
                       "[{:name \"1\", :url \"http://www.mangahere.co/manga/the_gamer/c002/\"}"
                       " {:name \"2\", :url \"http://www.mangahere.co/manga/the_gamer/c002/2.html\"}]")}))))

    (testing "/:site-name/image"
      (let [handle (app-routes
                    (test-system {:images {"manga-here"
                                           {{:name "1", :url "http://www.mangahere.co/manga/the_gamer/c095/"}
                                            [:img
                                             {:src (str "http://a.mhcdn.net/store/manga/13739/095.0/compressed"
                                                        "/rthe-gamer-5978533.jpg?v=1440085982")
                                              :alt "The Gamer 95 Page 1"}]}}}))]

        (is (= (handle (-> (mock/request :post "/api/v1/manga-here/image"
                                         (str "{:comic-page {:name \"1\", :url "
                                              "\"http://www.mangahere.co/manga/the_gamer/c095/\"}}"))
                           (mock/content-type "application/edn")))
               {:status 200
                :headers edn-content-type
                :body (str "[:img "
                           "{:src \"http://a.mhcdn.net/store/manga/13739/095.0/compressed"
                           "/rthe-gamer-5978533.jpg?v=1440085982\", "
                           ":alt \"The Gamer 95 Page 1\"}]")}))))))
