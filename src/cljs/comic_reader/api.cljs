(ns comic-reader.api
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET POST]]
            [ajax.edn]))

(def ^:private errors-db-key :api-errors)
(def ^:private errors-subscription-key :api-errors)
(def ^:private last-error-subscription-key :last-error)
(def ^:private error-count-subscription-key :error-count)
(def ^:private error-handler-key :api-error)

(def ^:private add-error
  (fnil conj []))

(defn setup! []
  (re-frame/reg-sub
   errors-subscription-key
   (fn [app-db _]
     (errors-db-key app-db)))

  (re-frame/reg-sub
   last-error-subscription-key
   :<- [errors-subscription-key]
   (fn [errors _]
     (peek errors)))

  (re-frame/reg-sub
   error-count-subscription-key
   :<- [errors-subscription-key]
   (fn [errors _]
     (count errors)))

  (re-frame/reg-event-db
   error-handler-key
   (fn [db [_ error]]
     (update db errors-db-key add-error error))))

(defn api-errors []
  (re-frame/subscribe [errors-subscription-key]))

(defn last-error []
  (re-frame/subscribe [last-error-subscription-key]))

(defn error-count []
  (re-frame/subscribe [error-count-subscription-key]))

(defn report-error [error-response]
  (re-frame/dispatch [error-handler-key error-response]))

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

(defn- *-n-images [direction site comic location n opts]
  (POST (str "/api/v1/" site "/" comic "/" direction "/" n)
    {:params {:location location}
     :format (ajax.edn/edn-request-format)
     :handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))

(defn get-images-before [site comic location n opts]
  (*-n-images "previous" site comic location n opts))

(defn get-images-after [site comic location n opts]
  (*-n-images "next" site comic location n opts))
