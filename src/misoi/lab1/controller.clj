(ns misoi.lab1.controller
  (:import (java.io File)
           (javax.imageio ImageIO)
           (java.awt.image BufferedImage)))

(use 'misoi.lab1.graphics)
(use 'seesaw.core)
(use 'seesaw.chooser)

(require '[seesaw.icon :as seesawIcon])

(declare initialImageBuffer)

(defn loadImagePath
  [root imagePath]
  (config! (select root [:#icon-path-label])
           :text imagePath))

(defn loadImage
  [root file]

  "Initial image buffer initialization"
  (def initialImageBuffer (ImageIO/read file))

  "Set image as icon for label."
  (config! (select root [:#icon-label])
           :icon (seesawIcon/icon initialImageBuffer)))

(defn imagesFileFilter
  [file]
  (.endsWith (clojure.string/lower-case (.getAbsolutePath file)) ".png"))

"Menu item controllers."
"Open menu item controller."
(defn initOpenMenuItem
  [root]
  (listen (select root [:#open-menu-item])
          :action (fn
                    [event]
                    (choose-file :filters [["Images" ["png" "jpeg"]]
                                            (file-filter "ImagesFF" imagesFileFilter)]
                                 :success-fn (fn
                                               [fc file]
                                               (loadImage root file)
                                               (loadImagePath root (.getAbsolutePath file)))))))

"Negative menu item controller."
(defn initNegativeMenuItem
  [root]
  (listen (select root [:#negative-menu-item])
          :action (fn
                    [event]
                    (config! (select root [:#icon-label])
                             :icon (makeNegative initialImageBuffer)))))

(defn init-controller
  [root]
  (initOpenMenuItem root)
  (initNegativeMenuItem root))