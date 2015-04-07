(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [comic-reader.utils :refer [safe-read-string]]
            [clojure.string :as s]))

(defn gen-link->map [process-name process-url]
  (fn [{name :content
        {url :href} :attrs}]
    {:name (process-name name)
     :url (process-url url)}))

(defn gen-add-key-from-url [key extract-pattern & [process]]
  (let [process (or process identity)]
    (fn [{:keys [url] :as comic-map}]
      (let [[_ data] (re-find extract-pattern url)]
        (assoc comic-map
               key (process data))))))

(def list
  [(let [canonical-url "http://mangafox.me"
         manga-url (format "%s/manga/" canonical-url)
         manga-pattern (re-pattern (str manga-url "(.*?)/"))
         link->map (gen-link->map first identity)]
     {:id :manga-fox
      :name "Manga Fox"

      :comic->url (fn [comic-id]
                    (str manga-url comic-id "/"))
      :comic-list-data {:url manga-url
                        :selector [:div.manga_list :ul :li :a]
                        :normalize (comp (gen-add-key-from-url
                                          :id
                                          manga-pattern)
                                         link->map)}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div#chapters :ul.chlist
                    :li :div #{:h3 :h4} :a]
         :normalize (comp
                     (gen-add-key-from-url :ch-num
                                           #"/c0*(\d+)/"
                                           safe-read-string)
                     link->map)})
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
         manga-pattern (re-pattern (str canonical-url
                                        "/(.*)(\\.html)?$"))
         link->map (gen-link->map (comp s/trim first)
                                  (partial str canonical-url))]
     {:id :manga-reader
      :name "Manga Reader"

      :comic->url (fn [comic-id]
                    (str canonical-url "/" comic-id))
      :comic-list-data {:url (str canonical-url "/alphabetical")
                        :selector [:div.series_alpha :ul :li :a]
                        :normalize (comp
                                    (gen-add-key-from-url
                                     :id
                                     manga-pattern)
                                    link->map)}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div#chapterlist :tr :td :a]
         :normalize (comp
                     (gen-add-key-from-url :ch-num
                                           #"/0+(\d+)(/\d+)?"
                                           safe-read-string)
                     link->map)})
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
                     :selector [:div#imgholder :a :img#img]})})

   (let [canonical-url "http://www.mangahere.co"
         manga-url     (format "%s/manga" canonical-url)
         mangalist-url (format "%s/mangalist/" canonical-url)
         manga-pattern (re-pattern (str manga-url "/(.*?)/"))
         link->map (gen-link->map (comp #(s/replace % #"\"" "")
                                        second)
                                  identity)]
     {:id :manga-here
      :name "Manga Here"
      :comic->url (fn [comic-id]
                    (str canonical-url "/" comic-id))
      :comic-list-data {:url mangalist-url
                        :selector [:section.main
                                   :li :a.manga_info]
                        :normalize (comp (gen-add-key-from-url
                                          :id
                                          manga-pattern)
                                         link->map)}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div.detail_list :ul
                    :li :span.left :a]
         :normalize (comp
                     (gen-add-key-from-url :ch-num
                                           #"/c0*(\d+)(\.\d+)?/"
                                           safe-read-string)
                     link->map)})
      :page-list-data-for-comic-chapter
      (fn [chapter-url]
        {:url chapter-url
         :selector [:section.readpage_top :div.go_page
                    :span.right :select :option]
         :normalize (fn [{[name]       :content
                          {url :value} :attrs}]
                      {:name name
                       :url  url})})
      :image-data (fn [page-url]
                    {:url page-url
                     :selector [:section#viewer :a :img#image]})})])

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
