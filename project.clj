(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"

  :url "https://github.com/radicalzephyr/comic-reader"
  :scm {:name "git"
        :url "https://github.com/radicalzephyr/comic-reader"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.0"

  :uberjar-name "comic-reader.jar"

  :source-paths ["src/clj"]
  :test-paths   ["test/clj"]

  :main ^:skip-aot comic-reader.system

  :aliases {"ci" ["do" "clean,"
                  "with-profiles" "dev,test" "test"]

            "uberjar" ["do" "clean,"
                       "run" "-m" "comic-reader.tasks.compile-sites,"
                       "uberjar"]}

  :env {:norms-dir "database/norms"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]

                 ;; Core app dependencies
                 [com.stuartsierra/component "0.3.2"]
                 [prismatic/schema "1.1.3"]
                 [environ "1.1.0"]
                 [org.clojure/tools.logging "0.3.1" :exclusions
                  [[org.slf4j/slf4j-log4j12 :extension "jar"]]]
                 [ch.qos.logback/logback-classic "1.1.11"]
                 [org.clojure/core.async "0.3.441"]

                 ;; Web server
                 [ring "1.5.1"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.2"]
                 [fogus/ring-edn "0.3.0"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]

                 ;; Data storage
                 [com.datomic/datomic-pro "0.9.5404"
                  :exclusions [org.clojure/clojure
                               com.google.guava/guava
                               org.apache.httpcomponents/httpclient
                               org.slf4j/slf4j-nop]]
                 [io.rkn/conformity "0.4.0"]

                 ;; Comic scraping
                 [clj-http "2.3.0"]
                 [tempfile "0.2.0"]
                 [enlive "1.1.6"]

                 ;; Clojurescript frontend
                 [org.clojure/clojurescript "1.9.495"]
                 [re-frame "0.9.2" :exclusions
                  [[org.clojure/clojurescript
                    :extension "jar"]]]
                 [cljsjs/waypoints "4.0.0-0"]
                 [secretary "1.2.3"]
                 [cljs-ajax "0.5.8"]]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username [:gpg :env/datomic_username]
                                   :password [:gpg :env/datomic_password]}}

  :plugins      [[lein-cljsbuild "1.1.4"]
                 [lein-environ "1.1.0"]]

  :clean-targets ^{:protect false} [:target-path
                                    "resources/public/js/compiled"]

  :cljsbuild {:builds
              {:client
               {:source-paths ["src/cljs"]
                :compiler
                {:output-to  "resources/public/js/compiled/main.js"
                 :output-dir "resources/public/js/compiled/out"
                 :asset-path "js/compiled/out"}}}}

  :profiles {:dev {:env {:database-uri "datomic:dev://localhost:4334/comics"}
                   :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
                   :source-paths ["dev-src/clj"]
                   :test-paths   ["test/cljs"]
                   :resource-paths ["dev-resources"]
                   :dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]
                                  [radicalzephyr/clansi "1.2.0"]
                                  [aysylu/loom "1.0.0"]

                                  [ring/ring-mock "0.3.0"]
                                  [figwheel-sidecar "0.5.9"]
                                  [devcards "0.2.2"]
                                  [com.cemerick/piggieback "0.2.1"]]

                   :repl-options {:init-ns comic-reader.dev
                                  :welcome (comic-reader.dev/welcome)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :figwheel {:http-server-root "public"
                              :css-dirs ["resources/public/css"]
                              :nrepl-port 7888}

                   :cljsbuild
                   {:builds
                    {:client {:source-paths ["src/cljs" "test/cljs"]
                              :figwheel true
                              :compiler
                              {:main "comic-reader.main"
                               :optimizations :none
                               :source-map true
                               :source-map-timestamp true}}

                     :test {:source-paths ["src/cljs" "test/cljs"]
                            :figwheel true
                            :compiler
                            {:main "comic-reader.runner"
                             :optimizations :none
                             :source-map true
                             :source-map-timestamp true
                             :output-to "resources/public/js/compiled/main.js"
                             :output-dir "/resources/public/js/compiled/out"
                             :asset-path "js/compiled/out"}}}}}

             :test {:env {:database-uri "datomic:mem://comics-test"}}

             :uberjar {:omit-source true
                       :aot :all
                       :hooks [leiningen.cljsbuild]

                       :cljsbuild
                       {:builds {:client {:compiler
                                          {:main "comic-reader.main"
                                           :optimizations :advanced
                                           :elide-asserts true
                                           :pretty-print false}}}}}})
