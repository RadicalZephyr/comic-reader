(ns comic-reader.comic-repository.datomic
  (:require [comic-reader.comic-repository :as repo]
            [comic-reader.database.datomic :as db]
            [datomic.api :as d]))

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

(defrecord DatomicRepository [database]
  repo/ComicRepository
  (list-sites [this]
    (let [db (d/db (db/connection database))]
      (d/q '[:find [(pull ?e [:site/id :site/name]) ...]
             :where [?e :site/name]]
           db)))

  (list-comics [this site-id]
    (let [db (d/db (db/connection database))]
      (d/q '[:find [(pull ?e [:comic/id :comic/name]) ...]
             :in $ ?site-id
             :where [?seid :site/id ?site-id]
                    [?e :comic/site ?seid]]
           db site-id)))

  (previous-locations [this site comic-id location n])

  (next-locations [this site-id comic-id location n]
    (let [db (d/db (db/connection database))]
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
    (d/transact (db/connection database) (mapv site-record sites)))

  (store-comics [this site-id comics]
    (let [site-db-id (get-site-id (db/connection database) site-id)]
      (d/transact (db/connection database) (mapv (partial comic-record site-db-id) comics))))

  (store-locations [this site-id comic-id locations]
    (let [comic-db-id (get-comic-id (db/connection database) site-id comic-id)]
      (d/transact (db/connection database) (mapcat (partial location-record comic-db-id) locations)))))

(defn new-datomic-repository []
  (map->DatomicRepository {}))
