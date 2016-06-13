{
 :site/image-selector [:div#imgholder :a :img#img]
 :site/page-normalize-pattern #"(?<=/\d{1,5})/\d+$"
 :site/page-normalize-format "%s%s"

 :site/chapter-number-pattern #"(?<=/\d{1,5})/\d+$"

 :site/page-list-selector [:div#selectpage :select#pageMenu :option]

 :site/chapter-list-selector [:table#listing :tr :td :a]

 :site/chapter-number-match-pattern #"/(\d+)$"

 :site/chapter-link-name-normalize :trim-first
 :site/chapter-link-url-normalize :concat-with-root-url

 :site/comic-list-selector [:div#container :div#wrapper_body :ul.series_alpha :li :a]
 :site/root-url "http://www.mangareader.net"
 :site/manga-url-format "%s"
 :site/manga-url-suffix-pattern #"/(.+?)$"

 :site/comic-link-name-normalize :trim-first
 :site/comic-link-url-normalize :concat-with-root-url

 :site/manga-list-format "%s/alphabetical"
 :site/comic->url-format "%s/%s"
 }
