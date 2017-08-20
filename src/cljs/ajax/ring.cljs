(ns ajax.ring
  (:require [ajax.core :refer [map->ResponseFormat]]
            [ajax.protocols :refer [-status -body]]
            [cljs.reader :as edn]))

(defn make-ring-read [body-read]
  (fn ring-read [xhrio]
    {:status (-status xhrio)
     :headers (js->clj (.getResponseHeaders xhrio))
     :body (body-read xhrio)}))

(defn ring-response-format
  "Returns a Ring-compatible response map.

   Optionally can be passed a :format option. This should be another
  response-format map. If format is provided it will be used to
  specify the content-type, and the read method will be used to
  populate the :body key in the response map."
  ([] (ring-response-format {:format {:read -body
                                      :description "raw body"
                                      :content-type ["*/*"]}}))
  ([{:keys [format]}]
   (map->ResponseFormat {:read (make-ring-read (:read format))
                         :description (str "Ring-" (:description format))
                         :content-type (:content-type format)})))
