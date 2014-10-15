(ns misoi.lab1.graphics
  (:import (java.awt.image.BufferedImage)))

"
Common functions to work with images & its pixels (RGB values).
"

(defn getRGBRed
  [RGBPixel]
  (bit-and (unsigned-bit-shift-right RGBPixel 16) 0x000000FF))

(defn setRGBRed
  [RGBPixel RGBRed]
  (bit-or (bit-and RGBPixel 0xFF00FFFF) (bit-shift-left RGBRed 16)))

(defn getRGBGreen
  [RGBPixel]
  (bit-and (unsigned-bit-shift-right RGBPixel 8) 0x000000FF))

(defn setRGBGreen
  [RGBPixel RGBGreen]
  (bit-or (bit-and RGBPixel 0xFFFF00FF) (bit-shift-left RGBGreen 8)))

(defn getRGBBlue
  [RGBPixel]
  (bit-and RGBPixel 0x000000FF))

(defn setRGBBlue
  [RGBPixel RGBBlue]
  (bit-or (bit-and RGBPixel 0xFFFFFF00) RGBBlue))

"Traverse all pixels of source image with invoking callback function on each of them."
(defn traversePixels
  [bufferedImage callback]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)]
    (doall (for [x (range width)
                 y (range height)]
             (callback bufferedImage x y)))))

"Get histogram for RGB values of source image."
(defn getRGBHistogram
  [bufferedImage]
  (let [redValues (transient [])
        greenValues (transient [])
        blueValues (transient [])]
    (traversePixels bufferedImage (fn
                                    [image x y]
                                    (let [RGBPixel (.getRGB image x y)]
                                      (conj! redValues (getRGBRed RGBPixel))
                                      (conj! greenValues (getRGBGreen RGBPixel))
                                      (conj! blueValues (getRGBBlue RGBPixel)))))
    [(frequencies (persistent! redValues))
     (frequencies (persistent! greenValues))
     (frequencies (persistent! blueValues))]))

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
                                   (setRGBRed (negateOperator (getRGBRed RGBPixel)))
                                   (setRGBGreen (negateOperator (getRGBGreen RGBPixel)))
                                   (setRGBBlue (negateOperator (getRGBBlue RGBPixel)))))))

"Make negative image from its source."
(defn makeNegative
  [bufferedImage]
  (traversePixels bufferedImage negateRGBPixel))

"
Functions to apply logarithmic algorithm of item-processing of source image.
"

"Logarithmic operator."
(defn logarithmicOperator
  [x]
  (* 30 (int (Math/log (+ 1 x)))))

"Apply logarithmic algorithm for concrete pixel of source image."
(defn logarithmicTransform
  [bufferedImage x y]
  (let [RGBPixel (.getRGB bufferedImage x y)]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (setRGBRed (logarithmicOperator (getRGBRed RGBPixel)))
                                   (setRGBGreen (logarithmicOperator (getRGBGreen RGBPixel)))
                                   (setRGBBlue (logarithmicOperator (getRGBBlue RGBPixel)))))))

"Apply logarithmic transformation to the source image."
(defn makeLogarithm
  [bufferedImage]
  (traversePixels bufferedImage logarithmicTransform))

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
        grayResult (shadesOfGrayOperator (getRGBRed RGBPixel) (getRGBGreen RGBPixel) (getRGBBlue RGBPixel))]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (setRGBRed grayResult)
                                   (setRGBGreen grayResult)
                                   (setRGBBlue grayResult)))))

"Make grays for image from its source."
(defn makeShadesOfGrays
  [bufferedImage]
  (traversePixels bufferedImage shadesOfGraysTransform))

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
(defn robertsFilter
  [bufferedImage x y]
  (let [RGBPixel (.getRGB bufferedImage x y)
        rightRGBPixel (getRightRGBPixel bufferedImage x y)
        bottomRGBPixel (getBottomRGBPixel bufferedImage x y)
        rightBottomRGBPixel (getRightBottomRGBPixel bufferedImage x y)]
    (.setRGB bufferedImage x y (-> RGBPixel
                                   (setRGBRed (robertsOperator (getRGBRed RGBPixel)
                                                               (getRGBRed rightBottomRGBPixel)
                                                               (getRGBRed rightRGBPixel)
                                                               (getRGBRed bottomRGBPixel)))
                                   (setRGBGreen (robertsOperator (getRGBGreen RGBPixel)
                                                                 (getRGBGreen rightBottomRGBPixel)
                                                                 (getRGBGreen rightRGBPixel)
                                                                 (getRGBGreen bottomRGBPixel)))
                                   (setRGBBlue (robertsOperator (getRGBBlue RGBPixel)
                                                                (getRGBBlue rightBottomRGBPixel)
                                                                (getRGBBlue rightRGBPixel)
                                                                (getRGBBlue bottomRGBPixel)))))))

(defn makeRobertsFilter
  [bufferedImage]
  (traversePixels bufferedImage robertsFilter))
