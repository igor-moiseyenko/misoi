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
(declare pixelLabels)
(declare labelAreaMap)
(declare areas)

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
(defn- initBinThresholding50Item
  [root]
  (listen (select root [:#bin-thresholding-50-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer 50)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initBinThresholding100Item
  [root]
  (listen (select root [:#bin-thresholding-100-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer 100)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initBinThresholding127Item
  [root]
  (listen (select root [:#bin-thresholding-127-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer 127)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initBinThresholding145Item
  [root]
  (listen (select root [:#bin-thresholding-145-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer 145)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initBinThresholding170Item
  [root]
  (listen (select root [:#bin-thresholding-170-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeBinaryThresholding initialImageBuffer 170)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Noise menu item controller."
(defn- initMedianFilterMenuItem
  [root]
  (listen (select root [:#median-filter-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeMedianFilter initialImageBuffer 3)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initMedianFilter5x5MenuItem
  [root]
  (listen (select root [:#median-filter-5x5-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeMedianFilter initialImageBuffer 5)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Recursive segmentation menu item controller."
(defn- initRecursiveSegmentationMenuItem
  [root]
  (listen (select root [:#recursive-segmentation-menu-item])
          :action (fn [event]
                    (let [segmentationResult (lab2Graphics/makeSequentialSegmentation initialImageBuffer)]
                      (def pixelLabels (get segmentationResult :pixel-labels))
                      (def labelAreaMap (get segmentationResult :label-area-map))
                      (def areas (get segmentationResult :areas))
                      (config! (select root [:#icon-label])
                               :icon initialImageBuffer)))))

"k-medoids clustering menu item controller."
(defn- initKMedoidsClusteringMenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 2)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initKMedoidsClustering3MenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-3-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 3)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initKMedoidsClustering4MenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-4-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 4)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initKMedoidsClustering5MenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-5-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 5)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initKMedoidsClustering6MenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-6-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 6)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

(defn- initKMedoidsClustering7MenuItem
  [root]
  (listen (select root [:#k-medoids-clustering-7-menu-item])
          :action (fn [event]
                    (lab2Graphics/makeClustering initialImageBuffer pixelLabels labelAreaMap 7)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Init controller"
(defn init
  [root]
  (initOpenMenuItem root)

  (initBinThresholding50Item root)
  (initBinThresholding100Item root)
  (initBinThresholding127Item root)
  (initBinThresholding145Item root)
  (initBinThresholding170Item root)

  (initMedianFilterMenuItem root)
  (initMedianFilter5x5MenuItem root)

  (initRecursiveSegmentationMenuItem root)
  (initKMedoidsClusteringMenuItem root)
  (initKMedoidsClustering3MenuItem root)
  (initKMedoidsClustering4MenuItem root)
  (initKMedoidsClustering5MenuItem root)
  (initKMedoidsClustering6MenuItem root)
  (initKMedoidsClustering7MenuItem root))
