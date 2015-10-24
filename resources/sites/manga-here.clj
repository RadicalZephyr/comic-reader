{:image-selector [:section#viewer.read_img :a :img]
 :manga-list-format "%s/mangalist/"
 :comic-list-selector [div.list_manga ul li a.manga_info]

 :page-normalize-pattern #".*"
 :page-normalize-format "%sc%3d/"

 :chapter-number-pattern #"c0*\d*/"

 :page-list-selector [:select.wid60 :option]

 :root-url "http://www.mangahere.co/"
 :manga-url-format "%s/manga/"

 :manga-url-suffix-pattern #"(.*?)/c\d*/$"

 :link-name-normalize first
 :link-url-normalize identity

 :chapter-list-selector [:div.manga_detail :div.detail_list :ul :li :span.left :a]
 :chapter-number-match-pattern #"c0*(\d*)/"

 :comic->url-format "%s%s/"}
