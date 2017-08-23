(ns comic-reader.api
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET POST]]
            [ajax.edn]
            [ajax.ring]
            [clairvoyant.core :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]))

(def ^:private errors-db-key :api-errors)
(def ^:private errors-subscription-key :api-errors)
(def ^:private last-error-subscription-key :last-error)
(def ^:private error-count-subscription-key :error-count)
(def ^:private error-handler-key :api-error)

(def ^:private add-error
  (fnil conj []))

(declare get-sites
         get-comics
         get-prev-locations
         get-next-locations)

(def ^:private api-fn
  (delay
   {:get-sites          get-sites
    :get-comics         get-comics
    :get-prev-locations get-prev-locations
    :get-next-locations get-next-locations}))

(defn null-fn [& args])

(defn call-api-fn [fn-key args]
  (apply (get @api-fn fn-key null-fn) args))

(defn setup! []
  (trace-forms {:tracer (tracer :color "gold")}
    (re-frame/reg-fx
     :api
     (fn api-fx [calls]
       (doseq [[fn-key & args] calls]
         (call-api-fn fn-key args)))))

  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     error-handler-key
     (fn error-event [db [_ error]]
       (update db errors-db-key add-error error))))

  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     errors-subscription-key
     (fn errors-sub [app-db _]
       (errors-db-key app-db)))

    (re-frame/reg-sub
     last-error-subscription-key
     :<- [errors-subscription-key]
     (fn last-error-sub [errors _]
       (peek errors)))

    (re-frame/reg-sub
     error-count-subscription-key
     :<- [errors-subscription-key]
     (fn error-count-sub [errors _]
       (count errors)))))

(defn api-errors []
  (re-frame/subscribe [errors-subscription-key]))

(defn last-error []
  (re-frame/subscribe [last-error-subscription-key]))

(defn error-count []
  (re-frame/subscribe [error-count-subscription-key]))

(defn report-error [error-response]
  (re-frame/dispatch [error-handler-key error-response]))

(declare check-async-request)

(defn- get-check-url [response opts]
  (let [url (get-in response [:headers "location"])]
    (GET url
      {:handler (fn [response]
                  (case (:status response)
                    200 ((:on-success opts) (:body response))
                    202 (check-async-request response opts)))
       :error-handler (or (:on-error opts) report-error)
       :response-format (ajax.ring/ring-response-format
                         {:format (ajax.edn/edn-response-format)})})))

(defn- check-async-request [response opts]
  (.setTimeout js/window #(get-check-url response opts) 2000))

(defn get-sites [opts]
  (GET "/api/v1/sites"
    {:handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))

(defn get-comics [site opts]
  (GET (str "/api/v1/" (name site) "/comics")
    {:handler (fn [response]
                (case (:status response)
                  200 ((:on-success opts) (:body response))
                  202 (check-async-request response opts)))
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.ring/ring-response-format
                       {:format (ajax.edn/edn-response-format)})}))

(defn- get-locations [direction site comic location n opts]
  (POST (str "/api/v1/" (name site) "/" (name comic) "/" direction)
    {:params {:location location
              :n n}
     :format (ajax.edn/edn-request-format)
     :handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))

(defn get-prev-locations [site comic location n opts]
  (get-locations "previous" site comic location n opts))

(defn get-next-locations [site comic location n opts]
  (get-locations "next" site comic location n opts))

(defn get-image [site-id location opts]
  (POST (str "/api/v1/" (name site-id) "/image")
    {:params {:location location}
     :format (ajax.edn/edn-request-format)
     :handler (:on-success opts)
     :error-handler (or (:on-error opts) report-error)
     :response-format (ajax.edn/edn-response-format)}))
