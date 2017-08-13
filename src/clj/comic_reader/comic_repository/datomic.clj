(ns comic-reader.comic-repository.datomic
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [comic-reader.comic-repository :as repo]
            [comic-reader.config :as config]
            [comic-reader.database :as db]
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

(defn- get-site-id
  "Resolve the db/id for the given site-id"
  [conn site-id]
  (let [db (d/db conn)]
    (d/q '[:find ?e .
           :in $ ?site-id
           :where [?e :site/id ?site-id]]
         db site-id)))

(defn- get-comic-id
  "Resolve the db/id for the given comic and site."
  [conn site-id comic-id]
  (let [db (d/db conn)]
    (d/q '[:find ?e .
           :in $ ?site-id ?comic-id
           :where [?site-ent :site/id ?site-id]
           [?e :comic/id ?comic-id]
           [?e :comic/site ?site-ent]]
         db site-id comic-id)))


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

(defrecord DatomicRepository [config conn]
  component/Lifecycle
  (start [component]
    (if-let [config (:config component)]
      (assoc component :conn
             (setup-and-connect-to-database config))
      component))

  (stop [component]
    (log/info "Disconnecting from database...")
    (dissoc component :conn))

  db/Database
  (destroy [database]
    (d/delete-database (config/database-uri config)))

  repo/ComicRepository
  (list-sites [this]
    (let [db (d/db conn)]
      (d/q '[:find [(pull ?e [:site/id :site/name]) ...]
             :where [?e :site/name]]
           db)))

  (list-comics [this site-id]
    (let [db (d/db conn)]
      (d/q '[:find [(pull ?e [:comic/id :comic/name]) ...]
             :in $ ?site-id
             :where [?seid :site/id ?site-id]
                    [?e :comic/site ?seid]]
           db site-id)))

  (previous-locations [this site comic-id location n])

  (next-locations [this site-id comic-id location n]
    (let [db (d/db conn)]
      (->> (d/q '[:find [(pull ?loc-ent [{:location/chapter [:chapter/title :chapter/number]}
                                         {:location/page [:page/number :page/url]}]) ...]
                  :in $ ?site-id ?comic-id
                  :where [?site-ent :site/id ?site-id]
                  [?comic-ent :comic/id ?comic-id]
                  [?comic-ent :comic/site ?site-ent]
                  [?loc-ent :location/comic ?comic-ent]]
                db site-id comic-id)
           (sort-by #(get-in % [:location/page :page/number]))
           (sort-by #(get-in % [:location/chapter :chapter/number])))))

  (image-tag [this site location])


  repo/WritableComicRepository
  (store-sites [this sites]
    (d/transact conn (mapv site-record sites)))

  (store-comics [this site-id comics]
    (let [site-db-id (get-site-id conn site-id)]
      (d/transact conn (mapv (partial comic-record site-db-id) comics))))

  (store-locations [this site-id comic-id locations]
    (let [comic-db-id (get-comic-id conn site-id comic-id)]
      (d/transact conn (mapcat (partial location-record comic-db-id) locations)))))

(defn new-datomic-repository []
  (map->DatomicRepository {}))
