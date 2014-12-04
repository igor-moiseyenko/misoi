(ns misoi.lab2.view
  (:use [seesaw.core]))

"Main view declarations"
(declare f)

"Menu elements"

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Thresholding menu items."
(def bin-thresholding-50-menu-item (menu-item :id :bin-thresholding-50-menu-item
                                              :text "Thresholding50"))
(def bin-thresholding-100-menu-item (menu-item :id :bin-thresholding-100-menu-item
                                               :text "Thresholding100"))
(def bin-thresholding-127-menu-item (menu-item :id :bin-thresholding-127-menu-item
                                               :text "Thresholding127"))
(def bin-thresholding-145-menu-item (menu-item :id :bin-thresholding-145-menu-item
                                               :text "Thresholding145"))
(def bin-thresholding-170-menu-item (menu-item :id :bin-thresholding-170-menu-item
                                               :text "Thresholding170"))

"Noise menu items."
(def median-filter-menu-item (menu-item :id :median-filter-menu-item
                                        :text "Median 3x3"))
(def median-filter-5x5-menu-item (menu-item :id :median-filter-5x5-menu-item
                                            :text "Median 5x5"))

"Segmentation menu items."
(def recursive-segmentation-menu-item (menu-item :id :recursive-segmentation-menu-item
                                                 :text "Recursive"))

"Clustering menu items."
(def k-medoids-clustering-menu-item (menu-item :id :k-medoids-clustering-menu-item
                                               :text "k-medoids-2"))
(def k-medoids-clustering-3-menu-item (menu-item :id :k-medoids-clustering-3-menu-item
                                                 :text "k-medoids-3"))
(def k-medoids-clustering-4-menu-item (menu-item :id :k-medoids-clustering-4-menu-item
                                                 :text "k-medoids-4"))
(def k-medoids-clustering-5-menu-item (menu-item :id :k-medoids-clustering-5-menu-item
                                                 :text "k-medoids-5"))
(def k-medoids-clustering-6-menu-item (menu-item :id :k-medoids-clustering-6-menu-item
                                                 :text "k-medoids-6"))
(def k-medoids-clustering-7-menu-item (menu-item :id :k-medoids-clustering-7-menu-item
                                                 :text "k-medoids-7"))

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
                                   (menu :text "Binarization"
                                         :items [bin-thresholding-50-menu-item
                                                 bin-thresholding-100-menu-item
                                                 bin-thresholding-127-menu-item
                                                 bin-thresholding-145-menu-item
                                                 bin-thresholding-170-menu-item])
                                   (menu :text "Noise"
                                         :items [median-filter-menu-item median-filter-5x5-menu-item])
                                   (menu :text "Segmentation"
                                         :items [recursive-segmentation-menu-item])
                                   (menu :text "Clustering"
                                         :items [k-medoids-clustering-menu-item
                                                 k-medoids-clustering-3-menu-item
                                                 k-medoids-clustering-4-menu-item
                                                 k-medoids-clustering-5-menu-item
                                                 k-medoids-clustering-6-menu-item
                                                 k-medoids-clustering-7-menu-item])])
         :minimum-size [640 :by 480]
         :content form
         :on-close :exit))

"Form elements declaration"
(declare icon-path-label)
(declare icon-label)
