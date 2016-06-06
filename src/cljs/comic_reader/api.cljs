(ns comic-reader.api
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer-macros [reaction]]
            [ajax.core :refer [GET POST]]
            [ajax.edn]))

(def ^:private errors-db-key :api-errors)
(def ^:private errors-subscription-key :api-errors)
(def ^:private error-handler-key :api-error)

(def ^:private add-error
  (fnil conj []))

(defn setup! []
  (rf/register-sub
   errors-subscription-key
   (fn [app-db _]
     (reaction (errors-db-key @app-db))))

  (rf/register-handler
   error-handler-key
   (fn [db [_ error]]
     (update db errors-db-key add-error error))))

(defn api-errors []
  (rf/subscribe [errors-subscription-key]))

(defn report-error [error-response]
  (rf/dispatch [error-handler-key error-response]))

(def *last-error*
  (reaction (peek @(api-errors))))


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
