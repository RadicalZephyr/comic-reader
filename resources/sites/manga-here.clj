{:image-selector [:section#viewer.read_img :a :img]
 :manga-list-format "%s/mangalist/"
 :comic-list-selector [:li :a.manga_info]

 :page-normalize-pattern #"(?:\d+\.html)?$"
 :page-normalize-format "%s/%s"

 :chapter-number-pattern #"/\d*$"

 :page-list-selector [:section.readpage_top :div :span :select.wid60 :option]

 :root-url "http://www.mangahere.co"
 :manga-url-format "%s/manga/"

 :manga-url-suffix-pattern #"(.*?)/$"

 :comic-link-name-normalize :trim-second
 :comic-link-url-normalize :nothing

 :chapter-link-name-normalize :trim-first
 :chapter-link-url-normalize :nothing

 :chapter-list-selector [:div.manga_detail :div.detail_list :ul :li :span.left :a]
 :chapter-number-match-pattern #"c0*(\d*)/"

 :comic->url-format "%s%s/"}
