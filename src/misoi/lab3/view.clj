(ns misoi.lab3.view
  (:use [seesaw.core]))

"Main view declarations"
(declare f)

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Form elements"

"Form elements declaration"
(declare icon-path-label)
(declare icon-label)

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
  (frame :title "Lab3"
         :menubar (menubar :items [(menu :text "File"
                                         :items [open-menu-item])])
         :minimum-size [640 :by 480]
         :content form
         :on-close :exit))
