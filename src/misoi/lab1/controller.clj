(ns misoi.lab1.controller
  (:import (java.io File)
           (javax.imageio ImageIO)
           (java.awt.image BufferedImage)))

(use 'misoi.lab1.graphics)
(use 'seesaw.core)
(use 'seesaw.chooser)
(use '[incanter.core :as incanter-core])
(use '[incanter.charts :as incanter-charts :exclude [slider]])
(use '[incanter.stats :as incanter-stats])

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

(defn refreshIconLabel
  [root]
  (config! (select root [:#icon-label])
           :icon initialImageBuffer))

"File menu item controllers."

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

"Edit menu item controllers."

"Negative menu item controller."
(defn initNegativeMenuItem
  [root]
  (listen (select root [:#negative-menu-item])
          :action (fn
                    [event]
                    (makeNegative initialImageBuffer)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Logarithm menu item controller."
(defn initLogarithmMenuItem
  [root]
  (listen (select root [:#logarithm-menu-item])
          :action (fn
                    [event]
                    (makeLogarithm initialImageBuffer)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Grays menu item controller."
(defn initGraysMenuItem
  [root]
  (listen (select root [:#grays-menu-item])
          :action (fn
                    [event]
                    (makeShadesOfGrays initialImageBuffer)
                    (config! (select root [:#icon-label])
                             :icon initialImageBuffer))))

"Roberts menu item controller."
(defn initRobertsFilterMenuItem
  [root]
  (listen (select root [:#roberts-filter-menu-item])
          :action (fn
                    [event]
                    (makeRobertsFilter initialImageBuffer)
                    (refreshIconLabel root))))

"Increment brightness menu item controller."
(defn initIncBrightnessMenuItem
  [root]
  (listen (select root [:#inc-brightness-menu-item])
          :action (fn
                    [event]
                    (incImageBrightness initialImageBuffer)
                    (refreshIconLabel root))))

"Show histogram."
(defn showHistogram
  [histogramValue histogramName]
  (view (bar-chart (keys histogramValue)
                   (vals histogramValue)
                   :title histogramName
                   :x-label ""
                   :y-label ""
                   :legend true)))

"Init menu item to show RGB histogram."
(defn initHistogramMenuItem
  [root]
  (listen (select root [:#histogram-menu-item])
          :action (fn
                    [event]
                    (let [rgbHistogram (getRGBHistogram initialImageBuffer)
                          redHistogram (rgbHistogram 0)
                          greenHistogram (rgbHistogram 1)
                          blueHistogram (rgbHistogram 2)]
                      (showHistogram redHistogram "Red")
                      (showHistogram greenHistogram "Green")
                      (showHistogram blueHistogram "Blue")))))

(defn init-controller
  [root]

  "Init File menu items."
  (initOpenMenuItem root)

  "Init Edit menu items."
  (initNegativeMenuItem root)
  (initLogarithmMenuItem root)
  (initGraysMenuItem root)
  (initRobertsFilterMenuItem root)
  (initIncBrightnessMenuItem root)

  (initHistogramMenuItem root))