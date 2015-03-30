(defproject comic-reader "0.1.0-SNAPSHOT"
  :description "An app for reading comics/manga on or offline"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [ring "1.3.1"]
                 [compojure "1.2.1"]
                 [fogus/ring-edn "0.2.0"]
                 [cljs-ajax "0.3.3"]]

  :plugins      [[lein-ring "0.8.13"]
                 [lein-cljsbuild "1.0.3"]]

  :hooks [leiningen.cljsbuild]
  :ring  {:handler comic-reader.core/app}
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-dir "resources/public/js"
                                   :output-to "resources/public/js/main.js"
                                   :source-map "resources/public/js/main.js.map"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}

  :main ^:skip-aot comic-reader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
