(ns misoi.lab4.view
  (:use [seesaw.core]))

"Main view declarations"
(declare f)

"File menu items."
(def open-menu-item (menu-item :id :open-menu-item
                               :text "Open"))

"Initial letters to run machine learning with current image."
(def i-menu-item (menu-item :id :i-menu-item
                            :text "И"))
(def v-menu-item (menu-item :id :v-menu-item
                            :text "В"))
(def m-menu-item (menu-item :id :m-menu-item
                            :text "М"))

"Noise menu itmes"
(def noise-5-percent-menu-item (menu-item :id :noise-5-percent-menu-item
                                          :text "5%"))
(def noise-10-percent-menu-item (menu-item :id :noise-10-percent-menu-item
                                           :text "10%"))
(def noise-20-percent-menu-item (menu-item :id :noise-20-percent-menu-item
                                           :text "20%"))
(def noise-50-percent-menu-item (menu-item :id :noise-50-percent-menu-item
                                           :text "50%"))
(def noise-100-percent-menu-item (menu-item :id :noise-100-percent-menu-item
                                            :text "100%"))

"Run Kohonen network menu item."
(def run-kohonen-network-menu-item (menu-item :id :run-kohonen-network-menu-item
                                              :text "Run Kohonen Network"))

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
                                         :items [open-menu-item ])
                                   (menu :text "Learning"
                                         :items [i-menu-item m-menu-item v-menu-item])
                                   (menu :text "Noise"
                                         :items [noise-5-percent-menu-item
                                                 noise-10-percent-menu-item
                                                 noise-20-percent-menu-item
                                                 noise-50-percent-menu-item
                                                 noise-100-percent-menu-item])
                                   (menu :text "Run"
                                         :items [run-kohonen-network-menu-item])])
         :minimum-size [640 :by 480]
         :content form
         :on-close :exit))
