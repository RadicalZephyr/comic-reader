{:chapter-list-selector [:div#chapters :ul.chlist :li :div #{:h4 :h3} :a]
 :chapter-number-match-pattern #"/c0*(\d+)/"
 :chapter-number-pattern #"/\d+\.html"

 :comic->url-format "%s%s/"
 :comic-list-selector [:div.manga_list :ul :li :a]

 :image-selector [:div#viewer :img#image]

 :link-name-normalize clojure.core/first
 :link-url-normalize  clojure.core/identity

 :manga-list-format "%s/manga/"
 :manga-pattern-match-portion "(.*?)/"
 :manga-url-format "%s/manga/"

 :page-list-selector [:div#top_center_bar :form#top_bar :select.m :option]
 :page-normalize-format "%s/%s.html"
 :page-normalize-pattern #"^\d+$"

 :root-url "http://mangafox.me"}
