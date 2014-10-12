(ns misoi.lab1.view)

(use 'seesaw.core)

(declare f)

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Edit menu items."
(def negative-menu-item (menu-item :id :negative-menu-item
                                   :text "Negative"))
(def logarithm-menu-item (menu-item :id :logarithm-menu-item
                                    :text "Logarithm"))
(def grays-menu-item (menu-item :id :grays-menu-item
                                :text "Shades of gray"))
(def roberts-filter-menu-item (menu-item :id :roberts-filter-menu-item
                                         :text "Roberts filter"))

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
                                              :items [negative-menu-item
                                                      logarithm-menu-item
                                                      grays-menu-item
                                                      roberts-filter-menu-item])])
              :minimum-size [640 :by 480]
              :content form
              :on-close :exit))

