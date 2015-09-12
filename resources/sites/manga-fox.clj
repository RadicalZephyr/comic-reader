{:root-url "http://mangafox.me"
 :manga-list-format "%s/manga/"
 :manga-url-format "%s/manga/"
 :manga-pattern-match-portion "(.*?)/"

 :comic->url-format "%s%s/"

 :chapter-number-match-pattern #"/c0*(\d+)/"
 :chapter-number-pattern #"/\d+\.html"

 :chapter-list-selector [:div#chapters :ul.chlist :li :div #{:h4 :h3} :a]
 :comic-list-selector [:div.manga_list :ul :li :a]
 :image-selector [:div#viewer :img#image]
 :page-list-selector [:div#top_center_bar :form#top_bar :select.m :option]

 :link-name-normalize clojure.core/first
 :link-url-normalize  clojure.core/identity

 :page-normalize-format "%s/%s.html"
 :page-normalize-pattern #"^\d+$"}
