(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as s]))

(defn gen-link->map [process-name process-url]
  (fn [{[name] :content
        {url :href} :attrs}]
    {:name (process-name name)
     :url (process-url url)}))

(defn gen-add-id-from-url [extract-pattern]
  (fn [{:keys [url] :as comic-map}]
    (let [[_ id] (re-find extract-pattern url)]
      (assoc comic-map :id id))))

(def list
  [(let [canonical-url "http://mangafox.me"
         manga-url (format "%s/manga/" canonical-url)
         manga-pattern (re-pattern (str manga-url "(.*?)/"))
         link->map (gen-link->map identity identity)]
     {:id :manga-fox
      :name "Manga Fox"

      :comic->url (fn [comic-id]
                    (str manga-url comic-id "/"))
      :comic-list-data {:url manga-url
                        :selector [:div.manga_list :ul :li :a]
                        :normalize (comp (gen-add-id-from-url
                                          manga-pattern)
                                         link->map)}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div#chapters :ul.chlist
                    :li :div #{:h3 :h4} :a]
         :normalize link->map})
      :page-list-data-for-comic-chapter
      (fn [chapter-url]
        {:url chapter-url
         :selector [:div#top_center_bar :form#top_bar
                    :select.m :option]
         :normalize (let [base-url (s/replace chapter-url
                                              #"/\d+\.html"
                                              "")]
                      (fn [{{name :value} :attrs}]
                        {:name name
                         :url (format "%s/%s.html"
                                      base-url name)}))})
      :image-data (fn [page-url]
                    {:url page-url
                     :selector [:div#viewer :img#image]})})

   (let [canonical-url "http://www.mangareader.net"
         manga-pattern (re-pattern (str canonical-url "/(.*)$"))
         link->map (gen-link->map s/trim
                                  (partial str canonical-url))]
     {:id :manga-reader
      :name "Manga Reader"

      :comic->url (fn [comic-id]
                    (str canonical-url "/" comic-id))
      :comic-list-data {:url (str canonical-url "/alphabetical")
                        :selector [:div.series_alpha :ul :li :a]
                        :normalize (comp
                                    (gen-add-id-from-url
                                     manga-pattern)
                                    link->map)}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div#chapterlist :tr :td :a]
         :normalize link->map})
      :page-list-data-for-comic-chapter
      (fn [chapter-url]
        {:url chapter-url
         :selector [:div#selectpage :select#pageMenu :option]
         :normalize (fn [{[name]       :content
                          {url :value} :attrs}]
                      {:name name
                       :url (str canonical-url url)})})
      :image-data (fn [page-url]
                    {:url page-url
                     :selector [:div#imgholder :a :img#img]})})])

(defn get-site [site]
  (some (fn [s]
          (when (= (:id s)
                   site)
            s))
        list))

(defn comic-list-data [site]
  (-> (get-site site)
      :comic-list-data))

(defn- get-data-fn [site fn-key]
  (-> (get-site site)
      fn-key))

(defn get-comic-url [site comic-id]
  ((get-data-fn site :comic->url) comic-id))

(defn chapter-list-data [site comic-id]
  ((get-data-fn site :chapter-list-data-for-comic)
   (get-comic-url site comic-id)))

(defn page-list-data [site chapter-url]
  ((get-data-fn site :page-list-data-for-comic-chapter)
   chapter-url))

(defn image-data [site page-url]
  ((get-data-fn site :image-data)
   page-url))
