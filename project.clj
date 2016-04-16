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
                  "with-profiles" "test" "test"]

            "uberjar" ["do" "clean,"
                       "run" "-m" "comic-reader.tasks.compile-sites,"
                       "uberjar"]}

  :env {:norms-dir "database/norms"}

  :dependencies [[org.clojure/clojure "1.8.0"]

                 ;; Core app dependencies
                 [com.stuartsierra/component "0.3.1"]
                 [prismatic/schema "1.1.0"]
                 [environ "1.0.2"]

                 ;; Web server
                 [ring "1.4.0"]
                 [fogus/ring-edn "0.3.0"]
                 [compojure "1.5.0"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]

                 ;; Data storage
                 [com.datomic/datomic-pro "0.9.5350" :exclusions [org.apache.httpcomponents/httpclient]]
                 [io.rkn/conformity "0.4.0"]

                 ;; Comic scraping
                 [clj-http "2.1.0"]
                 [tempfile "0.2.0"]
                 [enlive "1.1.6"]

                 ;; Clojurescript frontend
                 [org.clojure/clojurescript "1.7.228"]
                 [re-frame "0.7.0" :exclusions
                  [[org.clojure/clojurescript
                    :extension "jar"]]]
                 [cljsjs/waypoints "4.0.0-0"]

                 ;; [secretary "1.2.3"]
                 ;; [cljs-ajax "0.5.1"]
                 ]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username [:gpg :env/datomic_username]
                                   :password [:gpg :env/datomic_password]}}

  :plugins      [[lein-cljsbuild "1.1.3"]
                 [lein-environ "1.0.2"]]

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
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [radicalzephyr/clansi "1.2.0"]
                                  [aysylu/loom "0.6.0"]

                                  [ring/ring-mock "0.3.0"]
                                  [figwheel-sidecar "0.5.2"]
                                  [devcards "0.2.1-6"]
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
                             :output-dir ~(str (.getCanonicalPath (java.io.File. "."))
                                               "/resources/public/js/compiled/out")
                             :asset-path "js/compiled/out"}}}}}

             :test {:env {:database-uri "datomic:mem://comics"}}

             :uberjar {:omit-source true
                       :aot :all
                       :hooks [leiningen.cljsbuild]

                       :cljsbuild
                       {:builds {:client {:compiler
                                          {:main "comic-reader.main"
                                           :optimizations :advanced
                                           :elide-asserts true
                                           :pretty-print false}}}}}})
