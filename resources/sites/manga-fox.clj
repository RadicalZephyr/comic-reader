{:image-selector [:div#viewer :img#image]

 :chapter-number-pattern #"/\d+\.html"
 :page-list-selector [:div#top_center_bar :form#top_bar :select.m :option]
 :page-normalize-format "%s/%s.html"
 :page-normalize-pattern #"^\d+$"

 :chapter-list-selector [:div#chapters :ul.chlist :li :div #{:h4 :h3} :a]
 :chapter-number-match-pattern #"/c0*(\d+)/"
 :link-name-normalize clojure.core/first
 :link-url-normalize  clojure.core/identity

 :root-url "http://mangafox.me"
 :manga-url-format "%s/manga/"
 :manga-pattern-match-portion "(.*?)/"
 :comic-list-selector [:div.manga_list :ul :li :a]

 :manga-list-format "%s/manga/"}
