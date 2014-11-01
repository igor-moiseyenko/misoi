(ns misoi.lab1.graphics
  (:require [misoi.graphics :as graphics])
  (:import (java.awt.image.BufferedImage)))


"
Functions to apply algorithm of item-processing of source image to negate its pixels.
"

"Negate operator"
(defn negateOperator
  [x]
  (- 255 x))

"Apply algorith to negate concrete pixel of source image."
(defn negateRGBPixel
  [bufferedImage x y]
  (let [RGBPixel (.getRGB bufferedImage x y)]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (graphics/setRGBRed (negateOperator (graphics/getRGBRed RGBPixel)))
                                   (graphics/setRGBGreen (negateOperator (graphics/getRGBGreen RGBPixel)))
                                   (graphics/setRGBBlue (negateOperator (graphics/getRGBBlue RGBPixel)))))))

"Make negative image from its source."
(defn makeNegative
  [bufferedImage]
  (graphics/traversePixels bufferedImage negateRGBPixel))

"
Functions to apply logarithmic algorithm of item-processing of source image.
"

"Logarithmic operator."
(defn logarithmicOperator
  [x]
  (* 50 (int (Math/log (+ 1 x)))))

"Apply logarithmic algorithm for concrete pixel of source image."
(defn logarithmicTransform
  [bufferedImage x y]
  (let [RGBPixel (.getRGB bufferedImage x y)]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (graphics/setRGBRed (logarithmicOperator (graphics/getRGBRed RGBPixel)))
                                   (graphics/setRGBGreen (logarithmicOperator (graphics/getRGBGreen RGBPixel)))
                                   (graphics/setRGBBlue (logarithmicOperator (graphics/getRGBBlue RGBPixel)))))))

"Apply logarithmic transformation to the source image."
(defn makeLogarithm
  [bufferedImage]
  (graphics/traversePixels bufferedImage logarithmicTransform))

"
Functions to make shades of gray for source image.
"

"Shades of gray operator."
(defn shadesOfGrayOperator
  [red green blue]
  (int (+ (* 0.3 red) (* 0.59 green) (* 0.11 blue))))

"Shades of gray pixel transformation"
(defn shadesOfGraysTransform
  [bufferedImage x y]
  (let [RGBPixel (.getRGB bufferedImage x y)
        grayResult (shadesOfGrayOperator (graphics/getRGBRed RGBPixel)
                                         (graphics/getRGBGreen RGBPixel)
                                         (graphics/getRGBBlue RGBPixel))]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (graphics/setRGBRed grayResult)
                                   (graphics/setRGBGreen grayResult)
                                   (graphics/setRGBBlue grayResult)))))

"Make grays for image from its source."
(defn makeShadesOfGrays
  [bufferedImage]
  (graphics/traversePixels bufferedImage shadesOfGraysTransform))

"
Robert filter functions.
"

(defn getRightRGBPixel
  [bufferedImage x y]
  (if (< x (- (.getWidth bufferedImage) 1))
    (.getRGB bufferedImage (+ x 1) y)
    0))

(defn getBottomRGBPixel
  [bufferedImage x y]
  (if (< y (- (.getHeight bufferedImage) 1))
    (.getRGB bufferedImage x (+ y 1))
    0))

(defn getRightBottomRGBPixel
  [bufferedImage x y]
  (if (and (< x (- (.getWidth bufferedImage) 1))
           (< y (- (.getHeight bufferedImage) 1)))
    (.getRGB bufferedImage (+ x 1) (+ y 1))
    0))

(defn robertsOperator
  [xy x1y1 x1y xy1]
  (int (Math/sqrt (+ (Math/pow (- xy x1y1) 2)
                     (Math/pow (- x1y xy1) 2)))))

"Apply Roberts filter to the source image."
(defn robertsFilterCommon
  [bufferedImage x y RGBPixel]
  (let [rightRGBPixel (getRightRGBPixel bufferedImage x y)
        bottomRGBPixel (getBottomRGBPixel bufferedImage x y)
        rightBottomRGBPixel (getRightBottomRGBPixel bufferedImage x y)]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (graphics/setRGBRed (robertsOperator (graphics/getRGBRed RGBPixel)
                                                                        (graphics/getRGBRed rightBottomRGBPixel)
                                                                        (graphics/getRGBRed rightRGBPixel)
                                                                        (graphics/getRGBRed bottomRGBPixel)))
                                   (graphics/setRGBGreen (robertsOperator (graphics/getRGBGreen RGBPixel)
                                                                          (graphics/getRGBGreen rightBottomRGBPixel)
                                                                          (graphics/getRGBGreen rightRGBPixel)
                                                                          (graphics/getRGBGreen bottomRGBPixel)))
                                   (graphics/setRGBBlue (robertsOperator (graphics/getRGBBlue RGBPixel)
                                                                         (graphics/getRGBBlue rightBottomRGBPixel)
                                                                         (graphics/getRGBBlue rightRGBPixel)
                                                                         (graphics/getRGBBlue bottomRGBPixel)))))))

(defn robertsFilter
  [bufferedImage x y]
  (robertsFilterCommon bufferedImage x y (.getRGB bufferedImage x y)))

(defn makeRobertsFilter
  [bufferedImage]
  (graphics/traversePixels bufferedImage robertsFilter))

(defn makeRobertsFilter2
  [bufferedImage]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)
        imageMatrix (transient [])]
    (doall (for [x (range width)
                 y (range height)]
             (conj! imageMatrix (.getRGB bufferedImage x y))))
    (doall (for [x (range width)
                 y (range height)]
             (robertsFilterCommon bufferedImage x y (imageMatrix (+ y (* x width))))))))

(defn incBrightness
  [value fmax]
  (if (< (+ value fmax) 255)
    (+ value fmax)
    255))

(defn incBrightnessOperator
  [fmax]
  (fn
    [bufferedImage x y]
    (let [RGBPixel (.getRGB bufferedImage x y)]
      (.setRGB bufferedImage x y (-> RGBPixel
                                     (graphics/setRGBRed (incBrightness (graphics/getRGBRed RGBPixel)
                                                                        fmax))
                                     (graphics/setRGBGreen (incBrightness (graphics/getRGBGreen RGBPixel)
                                                                          fmax))
                                     (graphics/setRGBBlue (incBrightness (graphics/getRGBBlue RGBPixel)
                                                                         fmax)))))))

"Increment brightness of source image."
(defn incImageBrightness
  [bufferedImage]
  (graphics/traversePixels bufferedImage (incBrightnessOperator 30)))

"RGB histogram"
(defn getRGBHistogram
  [bufferedImage]
  (graphics/getRGBHistogram bufferedImage))
