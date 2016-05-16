(ns comic-reader.api
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer-macros [reaction]]
            ;[ajax.core :refer [GET POST]]
            ))

(def ^:private errors-db-key :api-errors)
(def ^:private errors-subscription-key :api-errors)
(def ^:private error-handler-key :api-error)

(defn GET [route opts])
(defn POST [route opts])

(def add-error
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

(defn get-sites []
  (GET "/api/v1/sites"
    {:handler #(rf/dispatch [:site-list %])
     :error-handler report-error
     :response-format :edn}))

(defn get-comics [site]
  (GET (str "/api/v1/" site "/comics")
    {:handler #(rf/dispatch [:comic-list %])
     :error-handler report-error
     :response-format :edn}))
