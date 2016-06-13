{
 :image-selector [:div#imgholder :a :img#img]
 :page-normalize-pattern #"(?<=/\d{1,5})/\d+$"
 :page-normalize-format "%s%s"

 :chapter-number-pattern #"(?<=/\d{1,5})/\d+$"

 :page-list-selector [:div#selectpage :select#pageMenu :option]

 :chapter-list-selector [:table#listing :tr :td :a]

 :chapter-number-match-pattern #"/(\d+)$"

 :chapter-link-name-normalize first
 :chapter-link-url-normalize (fn [segment]
                               (str (comic-reader.sites/root-url options)
                                    segment))

 :comic-list-selector [:div#container :div#wrapper_body :ul.series_alpha :li :a]
 :root-url "http://www.mangareader.net"
 :manga-url-format "%s"
 :manga-url-suffix-pattern #"/(.+?)$"

 :comic-link-name-normalize (comp clojure.string/trim first)
 :comic-link-url-normalize (fn [segment]
                             (str (comic-reader.sites/root-url options)
                                  segment))

 :manga-list-format "%s/alphabetical"
 :comic->url-format "%s/%s"
 }
