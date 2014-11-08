(ns misoi.lab2.view
  (:use [seesaw.core]))

"Main view declarations"
(declare f)

"Menu elements"

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Edit menu items."
(def bin-menu-item (menu-item :id :bin-menu-item
                              :text "Binarization"))

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
                                   (menu :text "Edit"
                                         :items [bin-menu-item])])
         :minimum-size [640 :by 480]
         :content form
         :on-close :exit))

"Form elements declaration"
(declare icon-path-label)
(declare icon-label)
