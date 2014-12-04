(ns misoi.lab3.controller
  (:require [seesaw.icon :as seesawIcon]
            [misoi.lab3.graphics :as lab3Graphics])
  (:use [seesaw.core]
        [seesaw.chooser])
  (:import (javax.imageio ImageIO)))

"Main controller interface"
(declare init)

"Shared objects"
(declare initialImageBuffer)

(defn- loadImagePath
  [root imagePath]
  (config! (select root [:#icon-path-label])
           :text imagePath))

(defn- loadImage
  [root file]

  (def initialImageBuffer (ImageIO/read file))
  (config! (select root [:#icon-label])
           :icon (seesawIcon/icon initialImageBuffer)))

(defn- imagesFileFilter
  [file]
  (.endsWith (clojure.string/lower-case (.getAbsolutePath file)) ".png"))

"Open menu item controller."
(defn- initOpenMenuItem
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

(defn init
  [root]
  (initOpenMenuItem root))
