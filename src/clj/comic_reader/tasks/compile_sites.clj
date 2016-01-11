(ns comic-reader.tasks.compile-sites
  (:require [clojure.java.io :as io]
            [comic-reader.sites.read :as site-read]))

(defn -main [& args]
  (println "Compiling sites list...")
  (spit (io/file "resources" site-read/sites-list-file-name)
        (prn-str (vec (site-read/find-all-sites)))))
