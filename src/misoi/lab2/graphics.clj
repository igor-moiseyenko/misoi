(ns misoi.lab2.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util])
  (:import (java.awt.image.BufferedImage)))

"Lab2 graphics interface"
(declare makeBinaryThresholding)
(declare makeRecursiveSegmentation)

(declare initialLabels)

"Returns min byte value in case of x less than threshold, max byte value - otherwise."
(defn- binaryThresholdOperator
  [x]
  (if (< x 100) 0 255))

"Returns RGBPixel after applying logical AND on each RGB component after applying threshold operator on each of them."
(defn- makeRGBPixelThresholding
  [RGBPixel]
  (let [RGBRedResult (binaryThresholdOperator (graphics/getRGBRed RGBPixel))
        RGBGreenResult (binaryThresholdOperator (graphics/getRGBGreen RGBPixel))
        RGBBlueResult (binaryThresholdOperator (graphics/getRGBBlue RGBPixel))
        RGBResult (and RGBRedResult RGBGreenResult RGBBlueResult)]
    (-> RGBPixel
        (graphics/setRGBRed RGBResult)
        (graphics/setRGBGreen RGBResult)
        (graphics/setRGBBlue RGBResult))))

"Make binary thresholding of the specified buffered image."
(defn makeBinaryThresholding
  [bufferedImage]
  (graphics/traverseAndSetPixels bufferedImage makeRGBPixelThresholding))

(defn- initLabels
  [xMax yMax]
  (def initialLabels (vec (repeat xMax (vec (repeat yMax 0))))))

(defn- recursiveSegmentation
  [bufferedImage x y labels label]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)]
    (when (and (= (get-in labels [x y]) 0)
               (= (graphics/getRGBRed (.getRGB bufferedImage x y)) 255))
      (util/assoc-in! labels [x y] label)
      (if (> x 0)
        (recursiveSegmentation bufferedImage (- x 1) y labels label))
      (if (< x width)
        (recursiveSegmentation bufferedImage (+ x 1) y labels label))
      (if (> y 0)
        (recursiveSegmentation bufferedImage x (- y 1) labels label))
      (if (< y height)
        (recursiveSegmentation bufferedImage x (+ y 1) labels label)))))

"Make recursive segmentation of the specified buffered image."
(defn makeRecursiveSegmentation
  [bufferedImage]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)
        labels (util/convert-matrix-to-transient (util/create-matrix width height))
        label 1]

    (doall (for [x (range width)
                 y (range height)]
             (recursiveSegmentation bufferedImage x y labels label)))

    (prn (util/convert-matrix-to-persistent labels))))

"Returns true in the case if top element is labeled, false - otherwise."
(defn- isTopLabeled
  [labels row col]
  (let [rowTop (- row 1)]
    (if (and (> rowTop 0)
             (not= (get-in labels [rowTop col]) 0))
      true
      false)))

"Returns true in the case if left element is labeled, false - otherwise."
(defn- isLeftLabeled
  [labels row col]
  (let [colLeft (- col 1)]
    (if (and (> colLeft 0)
             (not= (get-in labels [row colLeft]) 0))
      true
      false)))

"Returns label of top element."
(defn- getTopLabel
  [labels row col]
  (get-in labels [(- row 1) col]))

"Returns label of left element."
(defn- getLeftLabel
  [labels row col]
  (get-in labels [row (- col 1)]))

"Xor operation of 2 statements."
(defn- xor2
  [stmt1 stmt2]
  (if (and (= (or stmt1 stmt2) true)
           (= (and stmt1 stmt2) false))
    true
    false))

"Returns label of left element if labeled, label of top element - otherwise."
(defn- getTopOrLeftLabel
  [labels row col]
  (if (isLeftLabeled labels row col)
    (getLeftLabel labels row col)
    (getTopLabel labels row col)))

"Put labels association in the map."
(defn- assocEqualityLables!
  [labelsEqualityTable leftLabel topLabel]
  (if (not= (get labelsEqualityTable leftLabel) nil)
    (assoc! labelsEqualityTable leftLabel (conj (get labelsEqualityTable leftLabel) topLabel))
    (assoc! labelsEqualityTable leftLabel #{ topLabel }))
  (if (not= (get labelsEqualityTable topLabel) nil)
    (assoc! labelsEqualityTable topLabel (conj (get labelsEqualityTable topLabel) leftLabel))
    (assoc! labelsEqualityTable topLabel #{ leftLabel })))

"Workaround:
clojure.lang.PersistentArrayMap by default without defining value,
clojure.lang.PersistentHashMap only after value definition."
(def labelsEqualityTablePrototype {:prototype 1})

"Sequential segmentation."
(defn makeSequentialSegmentation
  [bufferedImage]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)
        labels (util/convert-matrix-to-transient (util/create-matrix width height))
        labelsEqualityTable (transient labelsEqualityTablePrototype)
        labelWrapper (transient [1])]
    (doall (for [row (range height)
                 col (range width)]
             (if (not= (graphics/getRGBRed (.getRGB bufferedImage col row)) 0)
               (if (and (not (isLeftLabeled labels row col))
                        (not (isTopLabeled labels row col)))
                 (do (util/assoc-in! labels [row col] (get labelWrapper 0))
                     (assoc! labelWrapper 0 (inc (get labelWrapper 0))))
                 (if (xor2 (isLeftLabeled labels row col)
                           (isTopLabeled labels row col))
                   (util/assoc-in! labels [row col] (getTopOrLeftLabel labels row col))
                   (when (and (isLeftLabeled labels row col)
                              (isTopLabeled labels row col))
                     (util/assoc-in! labels [row col] (getLeftLabel labels row col))
                     (if (not= (getLeftLabel labels row col)
                               (getTopLabel labels row col))
                       (assocEqualityLables! labelsEqualityTable (getLeftLabel labels row col) (getTopLabel labels row col)))))))))
    (prn (persistent! labelsEqualityTable))))