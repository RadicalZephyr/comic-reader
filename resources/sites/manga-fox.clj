{:image-selector [:div#viewer :img#image]

 :chapter-number-pattern #"/\d+\.html"

 :page-list-selector [:div#top_center_bar :form#top_bar :select.m :option]

 :page-normalize-format "%s/%s.html"
 :page-normalize-pattern #"^\d+$"
}
