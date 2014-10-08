(ns misoi.lab1.view)

(use 'seesaw.core)

(declare f)

"Menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))
(def negative-menu-item (menu-item :id :negative-menu-item
                                   :text "Negative"))

"Form elements."
(def icon-path-label
  (label :id :icon-path-label
         :text "File is not selected."))
(def icon-label
  (label :id :icon-label))

"Main form."
(def form
  (border-panel
    :border 5
    :hgap 5
    :vgap 5
    :north icon-path-label
    :center (scrollable
              icon-label)))


"Main frame."
(def f (frame :title "lalala"
              :menubar (menubar :items [(menu :text "File"
                                              :items [open-menu-item])
                                        (menu :text "Edit"
                                              :items [negative-menu-item])])
              :minimum-size [640 :by 480]
              :content form
              :on-close :exit))

