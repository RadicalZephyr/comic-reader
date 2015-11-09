{
 :image-selector [:div#imgholder :a :img#img]
 :page-normalize-pattern #"(?<=/\d{1,5})/\d+$"
 :page-normalize-format "%s%s"

 :chapter-number-pattern #"(?<=/\d{1,5})/\d+$"

 :page-list-selector [:div#selectpage :select#pageMenu :option]

 ;; :root-url "http://www.mangareader.net"
 ;; :manga-url-format "%s/"
 ;; :manga-url-suffix-pattern #"/(.*?)$"

 ;; :chapter-number-match-pattern #"/(\d+)$"


 }
