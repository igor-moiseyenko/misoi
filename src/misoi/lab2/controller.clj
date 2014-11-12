(ns misoi.lab2.controller
  (:require [seesaw.icon :as seesawIcon]
            [misoi.lab2.graphics :as lab2Graphics])
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

"Binary thresholding menu item controller."
(defn- initBinThresholdingItem
  [root]
  (listen (select root [:#bin-thresholding-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Recursive segmentation menu item."
(defn- initRecursiveSegmentationMenuItem
  [root]
  (listen (select root [:#recursive-segmentation-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeSequentialSegmentation initialImageBuffer))))

"Init controller"
(defn init
  [root]
  (initOpenMenuItem root)
  (initBinThresholdingItem root)
  (initRecursiveSegmentationMenuItem root))
