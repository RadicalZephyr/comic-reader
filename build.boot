(defn get-cleartext [prompt]
  (print prompt)
  (read-line))

(defn get-password [prompt]
  (print prompt)
  (apply str (.readPassword (System/console))))

(require '[clojure.string :as str])

(defn get-env-or-prompt [prefix prompt-fmt word get-fn]
  (let [env-name (str prefix word)]
    (or (System/getenv env-name)
        (get-fn (format prompt-fmt env-name (str/capitalize word))))))

(let [[username password] (mapv #(get-env-or-prompt "DATOMIC_"
                                                    "%s was not defined.\n%s"
                                                    %1 %2)
                                ["USERNAME"    "PASSWORD"]
                                [get-cleartext get-password])]

  (set-env! :source-paths #{"src/clj" "src/cljs"}
            :resource-paths #{"resources"}
            :dependencies '[[org.clojure/clojure "1.8.0"]

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
                            [cljs-ajax "0.5.8"]

                            ;; Dev Dependencies
                            [org.clojure/tools.namespace "0.3.0-alpha3" :scope "test"]
                            [radicalzephyr/clansi "1.2.0" :scope "test"]
                            [aysylu/loom "1.0.0" :scope "test"]

                            [ring/ring-mock "0.3.0" :scope "test"]
                            [figwheel-sidecar "0.5.9" :scope "test"]
                            [devcards "0.2.2" :scope "test"]
                            [com.cemerick/piggieback "0.2.1" :scope "test"]
                            [weasel "0.7.0"     :scope "test"]
                            [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                            [binaryage/devtools "0.9.2" :scope "test"]

                            ;; Boot Dependencies
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-cljs "1.7.228-2" :scope "test"]
                            [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                            [crisptrutski/boot-cljs-test   "0.3.0"     :scope "test"]
                            [pandeiro/boot-http "0.7.6" :scope "test"]
                            [adzerk/boot-reload "0.4.13" :scope "test"]
                            [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]]

            :repositories #(conj % ["my.datomic.com" {:url "https://my.datomic.com/repo" :username username :password password}])
            ))

(require
 '[adzerk.boot-test      :as boot-test]
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]]
 '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]])

(swap! boot.repl/*default-middleware*
       conj
       'refactor-nrepl.middleware/wrap-refactor
       'cider.nrepl/cider-middleware)

(deftask build []
  (comp (notify :visual true)
        (cljs)))

(deftask run []
  (comp (watch)
        (cljs-repl)
        (cljs-devtools)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced
                       :compiler-options {:closure-defines {:goog.DEBUG false}}})
  identity)

(deftask development []
  (set-env! :source-paths #(conj % "dev-src/clj")
            :resource-paths #(conj %  "dev-resources"))
  (task-options! cljs {:optimizations :none}
                 repl {:init-ns 'comic-reader.dev}
                 reload {:on-jsload 'comic-reader.main/main})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

(deftask testing []
  (set-env! :source-paths #(conj %  "test/clj" "test/cljs"))
  identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (watch)
        (test-cljs :js-env :phantom)))
