(ns misoi.lab2.view
  (:use [seesaw.core]))

"Main view declarations"
(declare f)

"Menu elements"

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Thresholding menu items."
(def bin-thresholding-menu-item (menu-item :id :bin-thresholding-menu-item
                                           :text "Binary"))

"Segmentation menu items."
(def recursive-segmentation-menu-item (menu-item :id :recursive-segmentation-menu-item
                                                 :text "Recursive"))

"Clustering menu items."
(def k-medoids-clustering-menu-item (menu-item :id :k-medoids-clustering-menu-item
                                               :text "k-medoids"))

"Form elements"

"Icon path label"
(def icon-path-label
  (label :id :icon-path-label
         :text "File is not selected."))

"Icon label"
(def icon-label
  (label :id :icon-label))

"Main form"
(def form
  (border-panel :border 5
                :hgap 5
                :vgap 5
                :north icon-path-label
                :center (scrollable icon-label)))

"Main frame"
(def f
  (frame :title "Lab2"
         :menubar (menubar :items [(menu :text "File"
                                         :items [open-menu-item])
                                   (menu :text "Thresholding"
                                         :items [bin-thresholding-menu-item])
                                   (menu :text "Segmentation"
                                         :items [recursive-segmentation-menu-item])
                                   (menu :text "Clustering"
                                         :items [k-medoids-clustering-menu-item])])
         :minimum-size [640 :by 480]
         :content form
         :on-close :exit))

"Form elements declaration"
(declare icon-path-label)
(declare icon-label)
