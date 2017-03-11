(ns comic-reader.api
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET POST]]
            [ajax.edn]))

(def ^:private errors-db-key :api-errors)
(def ^:private errors-subscription-key :api-errors)
(def ^:private error-handler-key :api-error)

(def ^:private add-error
  (fnil conj []))

(defn setup! []
  (re-frame/reg-sub
   errors-subscription-key
   (fn [app-db _]
     (errors-db-key app-db)))

  (re-frame/reg-event-db
   error-handler-key
   (fn [db [_ error]]
     (update db errors-db-key add-error error))))

(defn api-errors []
  (re-frame/subscribe [errors-subscription-key]))

(defn report-error [error-response]
  (re-frame/dispatch [error-handler-key error-response]))

(defn *last-error* []
  (peek @(api-errors)))


(defn get-sites [opts]
  (GET "/api/v1/sites"
    {:handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))

(defn get-comics [site opts]
  (GET (str "/api/v1/" site "/comics")
    {:handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))
