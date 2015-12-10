(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"

  :url "https://github.com/radicalzephyr/comic-reader"
  :scm {:name "git"
        :url "https://github.com/radicalzephyr/comic-reader"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :uberjar-name "comic-reader.jar"

  :source-paths ["src/clj"]
  :test-paths   ["test/clj"]

  :main comic-reader.system

  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; Core app dependencies
                 [com.stuartsierra/component "0.3.0"]
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
                 [org.clojure/clojurescript "1.7.170"]
                 [re-frame "0.5.0" :exclusions
                  [[org.clojure/clojurescript
                    :extension "jar"]]]

                 ;; [secretary "1.2.3"]
                 ;; [cljsjs/waypoints "3.1.1-0"]
                 ;; [cljs-ajax "0.5.1"]
                 ]

  :plugins      [[lein-cljsbuild "1.1.0"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :cljsbuild {:builds
              {:client
               {:source-paths ["src/cljs"]
                :compiler
                {:output-to  "resources/public/js/compiled/main.js"
                 :output-dir "resources/public/js/compiled/out"
                 :asset-path "js/compiled/out"}}}}

  :profiles {:dev {:jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
                   :source-paths ["dev-src/clj" "src/cljs"]
                   :test-paths   ["test/cljs"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [radicalzephyr/clansi "1.2.0"]
                                  [aysylu/loom "0.5.4"]

                                  [ring/ring-mock "0.3.0"]
                                  [figwheel-sidecar "0.5.0-1"]
                                  [devcards "0.2.1"]
                                  [com.cemerick/piggieback "0.2.1"]]

                   :repl-options {:init-ns comic-reader.system
                                  :welcome (println (str "To start developing a new site definition, "
                                                         "run:\n (require 'comic-reader.site-dev)"
                                                         "\n (in-ns   'comic-reader.site-dev)"
                                                         "\n\nThen use `(run-site-tests)' until all tests pass."
                                                         "\nSee doc/adding-a-new-site.md for more details."))
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :figwheel {:http-server-root "public"
                              :css-dirs ["resources/public/css"]
                              :nrepl-port 7888}

                   :cljsbuild
                   {:builds
                    {:client {:figwheel true
                              :compiler
                              {:main "comic-reader.main"
                               :source-paths ["src/cljs" "test/cljs"]
                               :optimizations :none
                               :source-map true
                               :source-map-timestamp true}}

                     :devcards {:figwheel {:devcards true}
                                :compiler
                                {:main "comic-reader.devcards"
                                 :source-paths ["src/cljs" "test/cljs"]
                                 :optimizations :none
                                 :source-map true
                                 :source-map-timestamp true
                                 :output-to "resources/public/js/compiled/devcards.js"
                                 :output-dir "resources/public/js/compiled/out-devcards"
                                 :asset-path "js/compiled/out-devcards"}}

                     :test {:source-paths ["src/cljs" "test/cljs"]
                            :figwheel true
                            :compiler
                            {:main "comic-reader.runner"
                             :optimizations :none
                             :source-map true
                             :source-map-timestamp true
                             :output-to "resources/public/js/compiled/main.js"
                             :output-dir "/Users/geoff/prog/clj/comic-reader/resources/public/js/compiled/out"
                             :asset-path "js/compiled/out"}}}}}

             :uberjar {:omit-source true
                       :aot :all
                       :hooks [leiningen.cljsbuild]

                       :cljsbuild
                       {:builds {:client {:compiler
                                          {:main comic-reader.main
                                           :optimizations :advanced
                                           :elide-asserts true
                                           :pretty-print false}}}}}})
