(ns comic-reader.database
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [comic-reader.config :as config]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]))

(defn- site-record [site-data & {:keys [temp-id]}]
  (let [temp-id (if temp-id
                  (d/tempid :db.part/user temp-id)
                  (d/tempid :db.part/user))
        {:keys [id name]} site-data]
    {:db/id temp-id
     :site/id id
     :site/name name}))

(defn- comic-record [site-id comic]
  {:db/id (d/tempid :db.part/user)
   :comic/id (:id comic)
   :comic/site site-id
   :comic/name (:name comic)})

(defn- location-record [comic-id location]
  (let [chapter-temp-id (d/tempid :db.part/user)
        page-temp-id (d/tempid :db.part/user)]
    [{:db/id chapter-temp-id
      :chapter/id (d/squuid)
      :chapter/number (get-in location [:chapter :ch-num])
      :chapter/title (get-in location [:chapter :name])}

     {:db/id page-temp-id
      :page/id (d/squuid)
      :page/number (get-in location [:page :number])
      :page/url (get-in location [:page :url])}

     {:db/id (d/tempid :db.part/user)
      :location/id (d/squuid)
      :location/comic comic-id
      :location/chapter chapter-temp-id
      :location/page page-temp-id}]))

(defn- create-database [config]
  (d/create-database (config/database-uri config)))

(defn- connect [config]
  (d/connect (config/database-uri config)))

(defn- setup-and-connect-to-database [config]
  (when (config/database-uri config)
    (log/info "Connecting to database...")
    (let [do-seeds? (create-database config)
          conn (connect config)]
      ;; Conform database to schemas here
      (when-let [norms-dir (config/norms-dir config)]
        (log/info "Conforming database to norms...")
        (conformity/ensure-conforms conn (norms/norms-map norms-dir)))

      ;; Add seeds if database was newly created
      ;; (when do-seeds?
      ;;   @(d/transact conn (seed/data)))
      conn)))

(defprotocol Database
  (get-sites [database] "Returns a seq of the stored site records.")
  (get-site-id [database site-id] "Get the db/id for the given site-id.")
  (store-sites [database sites] "Store a seq of site records.")

  (get-comics [database site-id] "Returns a seq of the stored comic records.")
  (get-comic-id [database site-id comic-id] "Get the db/id for the given comic-id at site-id.")
  (store-comics [database site-id comics] "Store a seq of comic records.")

  (get-locations [database site-id comic-id] "Returns a seq of the stored locations for the given comic and site.")
  (store-locations [database site-id comic-id locations] "Store a seq of location records."))

(defrecord DatomicDatabase [config conn]

  component/Lifecycle
  (start [component]
    (if-let [config (:config component)]
      (assoc component :conn
             (setup-and-connect-to-database config))
      component))

  (stop [component]
    (log/info "Disconnecting from database...")
    (dissoc component :conn))

  Database
  (get-sites [database]
    (let [db (d/db conn)]
      (d/q '[:find [(pull ?e [:site/id :site/name]) ...]
             :where [?e :site/name]]
           db)))

  (get-site-id [database site-id]
    (let [db (d/db conn)]
      (d/q '[:find ?e .
             :in $ ?site-id
             :where [?e :site/id ?site-id]]
           db site-id)))

  (store-sites [database sites]
    (d/transact conn (mapv site-record sites)))

  (get-comics [database site-id]
    (let [db (d/db conn)]
      (d/q '[:find [(pull ?e [:comic/id :comic/name]) ...]
             :in $ ?site-id
             :where [?seid :site/id ?site-id]
                    [?e :comic/site ?seid]]
           db site-id)))

  (get-comic-id [database site-id comic-id]
    (let [db (d/db conn)]
      (d/q '[:find ?e .
             :in $ ?site-id ?comic-id
             :where [?site-ent :site/id ?site-id]
                    [?e :comic/id ?comic-id]
                    [?e :comic/site ?site-ent]]
           db site-id comic-id)))

  (store-comics [database site-id comics]
    (let [site-db-id (get-site-id database site-id)]
      (d/transact conn (mapv (partial comic-record site-db-id) comics))))

  (get-locations [database site-id comic-id]
    (let [db (d/db conn)]
      (->> (d/q '[:find [(pull ?loc-ent [{:location/chapter [:chapter/title :chapter/number]}
                                         {:location/page [:page/number :page/url]}]) ...]
                  :in $ ?site-id ?comic-id
                  :where [?site-ent :site/id ?site-id]
                  [?comic-ent :comic/id ?comic-id]
                  [?comic-ent :comic/site ?site-ent]
                  [?chapter-ent :chapter/id]
                  [?loc-ent :location/chapter ?chapter-ent]]
                db site-id comic-id)
           (sort-by #(get-in % [:location/page :page/number]))
           (sort-by #(get-in % [:location/chapter :chapter/number])))))

  (store-locations [database site-id comic-id locations]
    (let [comic-db-id (get-comic-id database site-id comic-id)]
      (d/transact conn (mapcat (partial location-record comic-db-id) locations)))))

(defn database? [e]
  (instance? DatomicDatabase e))

(defn new-database []
  (map->DatomicDatabase {}))
