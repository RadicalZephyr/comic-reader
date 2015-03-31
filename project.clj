(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [enlive "1.1.5"]
                 [ring "1.3.1"]
                 [compojure "1.2.1"]

                 [org.clojure/clojurescript "0.0-2850"]
                 [figwheel "0.2.5-SNAPSHOT"]
                 [org.omcljs/om "0.8.8"]
                 [cljs-ajax "0.3.3"]]

  :plugins      [[lein-ring "0.8.13"]
                 [lein-cljsbuild "1.0.4"]
                 [lein-figwheel "0.2.5-SNAPSHOT"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :hooks [leiningen.cljsbuild]
  :ring  {:handler comic-reader.core/app
          :nrepl {:start? true :port 4500}
          :port 8090}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to  "resources/public/js/compiled/main.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :main comic-reader.main
                                   :asset-path "js/compiled/out"
                                   :source-map true
                                   :source-map-timestamp true
                                   :cache-analysis true
                                   :optimizations :none
                                   :pretty-print true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/comic_reader.js"
                                   :main comic-reader.main
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"] ;; watch and update CSS
             :nrepl-port 7888}

  :main ^:skip-aot comic-reader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
