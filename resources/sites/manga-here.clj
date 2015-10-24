{:image-selector [:section#viewer.read_img :a :img]
 :manga-list-format "%s/mangalist/"
 :comic-list-selector [div.list_manga ul li a.manga_info]

 :page-normalize-pattern #"(?:\d+\.html)?$"
 :page-normalize-format "%s/%s"

 :chapter-number-pattern #"/\d*$"

 :page-list-selector [:section.readpage_top :div :span :select.wid60 :option]

 :root-url "http://www.mangahere.co/"
 :manga-url-format "%s/manga/"

 :manga-url-suffix-pattern #"(.*?)/c\d*/$"

 :comic-link-name-normalize second
 :comic-link-url-normalize identity

 :chapter-link-name-normalize (comp clojure.string/trim first)
 :chapter-link-url-normalize identity

 :chapter-list-selector [:div.manga_detail :div.detail_list :ul :li :span.left :a]
 :chapter-number-match-pattern #"c0*(\d*)/"

 :comic->url-format "%s%s/"}
