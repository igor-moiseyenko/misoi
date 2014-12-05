(ns misoi.graphics
  (:import (java.awt.image.BufferedImage)))

"
Common functions to work with images & its pixels (RGB values).
"

(defn getImageWidth
  [image]
  (.getWidth image))

(defn getImageHeight
  [image]
  (.getHeight image))

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

"Traverse all pixels of source image and set value to each of them equal to the result of callback function."
(defn traverseAndSetPixels
  [bufferedImage callback]
  (traversePixels bufferedImage (fn [bufferedImage x y]
                                  (let [RGBPixel (.getRGB bufferedImage x y)]
                                    (.setRGB bufferedImage x y (callback RGBPixel))))))

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

"Returns matrix, that represents specified buffered image.
 col - stands for x in buffered image functions,
 row - stands for y in buffered image functions."
(defn getImageMatrix
  [bufferedImage]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)
        rows (range height)
        cols (range width)]
    (reduce (fn [imageMatrix row]
              (conj imageMatrix
                    (reduce (fn [imageRow col]
                              (conj imageRow (.getRGB bufferedImage col row)))
                            []
                            cols)))
            []
            rows)))

(defn setImageMatrix
  [bufferedImage imageMatrix]
  (map (fn [imageMatrixRow imageMatrixRowIndex]
         (map (fn [imageMatrixEl imageMatrixColIndex]
                (.setRGB bufferedImage imageMatrixColIndex imageMatrixRowIndex imageMatrixEl))
              imageMatrixRow
              (iterate inc 0)))
       imageMatrix
       (iterate inc 0)))

"Traverse image matrix and call specified callback function on each element."
(defn traverseImageMatrix
  [imageMatrix callback]
  (map (fn [rowElements row]
         (map (fn [element col]
                (callback imageMatrix element row col))
              rowElements
              (iterate inc 0)))
       imageMatrix
       (iterate inc 0)))
