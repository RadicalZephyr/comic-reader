(ns comic-reader.api
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer-macros [reaction]]
            ;[ajax.core :refer [GET POST]]
            ))

(defn GET [route opts])
(defn POST [route opts])

(def add-error
  (fnil conj []))

(defn setup! []
  (rf/register-sub
   :api-errors
   (fn [app-db _]
     (reaction (:api-errors @app-db))))

  (rf/register-handler
   :api-error
   (fn [db [_ error]]
     (update db :api-errors add-error error))))

(defn api-errors []
  (rf/subscribe [:api-errors]))

(defn report-error [error-response]
  (rf/dispatch [:api-error error-response]))

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
