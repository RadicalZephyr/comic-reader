(ns comic-reader.comic-repository.datomic
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [com.stuartsierra.component :as component]
            [comic-reader.comic-repository :as repo]
            [comic-reader.config :as config]
            [comic-reader.database :as db]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]))

(defn- site-record [site & {:keys [temp-id]}]
  (let [site (set/rename-keys site {:id :site/id
                                    :name :site/name})
        temp-id (if temp-id
                  (d/tempid :db.part/user temp-id)
                  (d/tempid :db.part/user))]
    (assoc site :db/id temp-id)))

(defn- comic-record [comic]
  (let [comic (set/rename-keys comic {:id :comic/id
                                      :name :comic/name})
        site-id (keyword (namespace (:comic/id comic)))]
    (assoc comic :db/id (d/tempid :db.part/user)
           :comic/site [:site/id site-id])))

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
  (-list-sites [this]
    (async/thread
      (let [db (d/db conn)]
        (d/q '[:find [(pull ?e [:site/id :site/name]) ...]
               :where [?e :site/name]]
             db))))

  (-list-comics [this site-id]
    (async/thread
      (let [db (d/db conn)]
        (d/q '[:find [(pull ?e [:comic/id :comic/name]) ...]
               :in $ ?site-id
               :where [?seid :site/id ?site-id]
               [?e :comic/site ?seid]]
             db site-id))))

  (-previous-locations [this comic-id location n]
    (async/thread []))

  (-next-locations [this comic-id location n]
    (async/thread
      (let [db (d/db conn)]
        (->> (d/q '[:find [(pull ?loc-ent [{:location/chapter [:chapter/title :chapter/number]}
                                           {:location/page [:page/number :page/url]}]) ...]
                    :in $ ?comic-id
                    :where
                    [?comic-ent :comic/id ?comic-id]
                    [?loc-ent :location/comic ?comic-ent]]
                  db comic-id)
             (sort-by #(get-in % [:location/page :page/number]))
             (sort-by #(get-in % [:location/chapter :chapter/number]))
             (drop-while (if location #(not= % location) (constantly false)))
             (take n)))))

  (-image-tag [this site location]
    (async/thread []))


  repo/WritableComicRepository
  (-store-sites [this sites]
    (d/transact conn (mapv site-record sites)))

  (-store-comics [this comics]
    (d/transact conn (mapv comic-record comics)))

  (-store-locations [this comic-id locations]
    (let [comic-db-ref [:comic/id comic-id]]
      (d/transact conn (mapcat (partial location-record comic-db-ref) locations)))))

(defn new-datomic-repository []
  (map->DatomicRepository {}))
