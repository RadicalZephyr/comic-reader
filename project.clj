(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2843"]
                 [figwheel "0.2.5"]
                 [enlive "1.1.5"]
                 [ring "1.3.1"]
                 [compojure "1.2.1"]
                 [cljs-ajax "0.3.3"]]

  :plugins      [[lein-ring "0.8.13"]
                 [lein-cljsbuild "1.0.4"]
                 [lein-figwheel "0.2.5"]]

  :hooks [leiningen.cljsbuild]
  :ring  {:handler comic-reader.core/app}
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to  "resources/public/js/compiled/main.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map true
                                   :optimizations :none
                                   :pretty-print true}}]}
  :figwheel {:nrepl-port 7888}

  :main ^:skip-aot comic-reader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
