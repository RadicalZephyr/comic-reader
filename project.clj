(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.1"
  :uberjar-name "comic-reader.jar"
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.4.0"]
                 [fogus/ring-edn "0.3.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [enlive "1.1.6"]
                 [environ "1.0.0"]

                 [org.clojure/clojurescript "1.7.48"]
                 [reagent "0.5.0"]
                 [re-frame "0.4.1"
                  :exclusions [[org.clojure/clojurescript
                                :extension "jar"]]]
                 [secretary "1.2.3"]
                 [cljsjs/waypoints "3.1.1-0"]
                 [cljs-ajax "0.3.10"]]

  :plugins      [[lein-ring "0.9.6"]
                 [lein-cljsbuild "1.1.0"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :hooks [leiningen.cljsbuild]
  :ring  {:handler comic-reader.server/app
          :nrepl {:start? true :port 4500}
          :port 8090}

  :profiles {:dev {:dependencies [[figwheel "0.3.8"]]
                   :plugins [[lein-figwheel "0.3.8"
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
