(ns misoi.lab1.graphics
  (:import (java.awt.image.BufferedImage)))

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

(defn negateRGBPixel
  [bufferedImage x y RGBPixel]
  (.setRGB bufferedImage x y (-> RGBPixel
                                 (setRGBRed (- 255 (getRGBRed RGBPixel)))
                                 (setRGBGreen (- 255 (getRGBGreen RGBPixel)))
                                 (setRGBBlue (- 255 (getRGBBlue RGBPixel))))))

(defn traverseRGBPixels
  [bufferedImage callback]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)]
    (for [x (range width)
          y (range height)]
      (callback bufferedImage x y (.getRGB bufferedImage x y)))))

(defn makeNegative
  [bufferedImage]
  (traverseRGBPixels bufferedImage negateRGBPixel))
