(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.1"
  :uberjar-name "comic-reader.jar"
  :source-paths ["src/clj"]

  :repositories {"my-datomic" {:url "https://my.datomic.com/repo"
                               :creds :gpg}}

  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; Core app dependencies
                 [com.stuartsierra/component "0.3.0"]
                 [com.datomic/datomic-pro "0.9.5327"]
                 [environ "1.0.1"]

                 ;; Web server
                 [ring "1.4.0"]
                 [fogus/ring-edn "0.3.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]

                 ;; Comic scraping
                 [clj-http "2.0.0"]
                 [tempfile "0.2.0"]
                 [enlive "1.1.6"]

                 ;; Clojurescript frontend
                 [org.clojure/clojurescript "1.7.145"]
                 [reagent "0.5.0"]
                 [re-frame "0.5.0-alpha1"
                  :exclusions [[org.clojure/clojurescript
                                :extension "jar"]]]
                 [secretary "1.2.3"]
                 [cljsjs/waypoints "3.1.1-0"]
                 [cljs-ajax "0.5.1"]]

  :plugins      [[lein-ring "0.9.7"]
                 [lein-cljsbuild "1.1.0"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :hooks [leiningen.cljsbuild]
  :ring  {:handler comic-reader.server/app
          :nrepl {:start? true :port 4500}
          :port 8090}

  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.0"]
                                  [figwheel "0.4.1"]
                                  [aysylu/loom "0.5.4"]]
                   :plugins [[lein-figwheel "0.4.1"
                              :exclusions [[cider/cider-nrepl
                                            :extensions "jar"]]]]
                   :figwheel {:http-server-root "public"
                              :css-dirs ["resources/public/css"]
                              :nrepl-port 7888}
                   :cljsbuild
                   {:builds {:client {:source-paths ["devsrc"]
                                      :compiler
                                      {:main comic-reader.dev
                                       :optimizations :none
                                       :source-map true
                                       :source-map-timestamp true}}}}}

             :uberjar {:omit-source true
                       :aot :all
                       :cljsbuild
                       {:builds {:client {:compiler
                                          {:main comic-reader.main
                                           :optimizations :advanced
                                           :elide-asserts true
                                           :pretty-print false}}}}}}

  :cljsbuild {:builds
              {:client
               {:source-paths ["src/cljs"]
                :compiler
                {:output-to  "resources/public/js/compiled/main.js"
                 :output-dir "resources/public/js/compiled/out"
                 :asset-path "js/compiled/out"}}}})
