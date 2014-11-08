(ns misoi.lab2.graphics
  (:require [misoi.graphics :as graphics])
  (:import (java.awt.image.BufferedImage)))

"Lab2 graphics interface"
(declare makeThresholding)

"Returns min byte value in case of x less than threshold, max byte value - otherwise."
(defn- thresholdOperator
  [x]
  (if (< x 100) 0 255))

"Returns RGBPixel after applying logical AND on each RGB component after applying threshold operator on each of them."
(defn- makeRGBPixelThresholding
  [RGBPixel]
  (let [RGBRedResult (thresholdOperator (graphics/getRGBRed RGBPixel))
        RGBGreenResult (thresholdOperator (graphics/getRGBGreen RGBPixel))
        RGBBlueResult (thresholdOperator (graphics/getRGBBlue RGBPixel))
        RGBResult (and RGBRedResult RGBGreenResult RGBBlueResult)]
    (-> RGBPixel
        (graphics/setRGBRed RGBResult)
        (graphics/setRGBGreen RGBResult)
        (graphics/setRGBBlue RGBResult))))

"Make thresholding on the specified buffered image."
(defn makeThresholding
  [bufferedImage]
  (graphics/traverseAndSetPixels bufferedImage makeRGBPixelThresholding))
