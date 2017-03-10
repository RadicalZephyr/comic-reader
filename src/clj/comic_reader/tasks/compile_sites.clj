(ns comic-reader.tasks.compile-sites
  (:require [comic-reader.sites.read :as site-read]))

(defn -main [& args]
  [site-read/sites-list-file-name
   (prn-str (vec (site-read/find-all-sites)))])
