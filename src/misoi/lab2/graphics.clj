(ns misoi.lab2.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util])
  (:import (java.awt.image.BufferedImage)))

"Lab2 graphics interface"
(declare makeBinaryThresholding)
(declare makeRecursiveSegmentation)
(declare makeSequentialSegmentation)
(declare makeKMedoidsClustering)

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

"Depth-first search algorithm for traversing graph."
(defn- depth-first-search
  [m k visited areaLabel]
  (when (= (get visited k) nil)
    (assoc! visited k areaLabel)
    (doall (map (fn
                  [v]
                  (depth-first-search m v visited areaLabel))
                (get m k)))))

"Workaround:
clojure.lang.PersistentArrayMap by default without defining value,
clojure.lang.PersistentHashMap only after value definition."
(def visitedPrototype { :prototype 1 })

"Create label - connected area mapping with depth-first search algorithm."
(defn- label-areas
  [m]
  (let [visited (transient visitedPrototype)
       areaLabelWrapper (transient [0])]
    (doall (map (fn
                  [k]
                  (if (= (get visited k) nil)
                    (do (assoc! areaLabelWrapper 0 (inc (get areaLabelWrapper 0)))
                        (depth-first-search m k visited (get areaLabelWrapper 0)))))
                (keys m)))
    { :label-area-map (dissoc (persistent! visited) :prototype)
     :areas (get (persistent! areaLabelWrapper) 0) }))

"Workaround:
clojure.lang.PersistentArrayMap by default without defining value,
clojure.lang.PersistentHashMap only after value definition."
(def labelsEqualityTablePrototype { :prototype 1 })

"Create map with colors."
(defn- create-colors-map
  []
  { 0 [ 20 60 80]
   1 [ 100 120 140]
   2 [ 120 140 160]
   3 [ 140 160 180]
   4 [ 160 180 200]
   5 [ 180 200 220]
   6 [ 200 220 240]
   7 [ 220 240 40]
   8 [ 240 40 60]
   9 [ 40 60 80]})

"Set colors to pixels according to the label-color map."
(defn- set-color-to-labels
  [bufferedImage labels labelAreaMap]
  (let [colorsMap (create-colors-map)]
    (graphics/traversePixels bufferedImage
                             (fn [bufferedImage col row]
                               (let [label (get-in labels [row col])
                                     area (get labelAreaMap label)]
                                 (if (not= area nil)
                                   (let [colorMapIndex (mod area (count colorsMap))
                                         RGBPixel (.getRGB bufferedImage col row)]
                                     (.setRGB bufferedImage
                                              col
                                              row
                                              (-> RGBPixel
                                                  (graphics/setRGBRed (get (get colorsMap colorMapIndex) 0))
                                                  (graphics/setRGBGreen (get (get colorsMap colorMapIndex) 1))
                                                  (graphics/setRGBBlue (get (get colorsMap colorMapIndex) 2)))))))))))

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
    (let [labelAreas (label-areas (dissoc (persistent! labelsEqualityTable) :prototype))
          labelAreaMap (get labelAreas :label-area-map)
          areas (get labelAreas :areas)
          pixelLabels (util/convert-matrix-to-persistent labels)]
      (prn (get labelAreas :label-area-map))
      (set-color-to-labels bufferedImage pixelLabels labelAreaMap)
      { :pixel-labels pixelLabels
       :label-area-map labelAreaMap
       :areas areas})))

(def areasOfItemsPrototype { :prototype 1 } )

"Group items by area which it belongs."
(defn- get-areas-of-items
  [rows cols pixelLabels labelAreaMap]
  (let [areasOfItems (transient areasOfItemsPrototype)]
    (doall (for [row (range rows)
                 col (range cols)]
             (let [pixelLabel (get-in pixelLabels [row col])
                   area (get labelAreaMap pixelLabel)]
               (if (not= area nil)
                 (if (not= (get areasOfItems area) nil)
                   (assoc! areasOfItems area (conj (get areasOfItems area) [row col]))
                   (assoc! areasOfItems area [[row col]]))))))
    (dissoc (persistent! areasOfItems) :prototype)))

"Get square area attribute."
(defn- get-area-square
  [areaItems]
  (count areaItems))

"Get attribute vector for each area."
(defn- get-areas-attribute-vectors
  [pixelLabels areasOfItems]
  (reduce (fn [areasAttributeVectors areaOfItems]
            (let [area (get areaOfItems 0)
                  items (get areaOfItems 1)]
              (assoc areasAttributeVectors area
                     { :square (get-area-square items)})))
          {}
          areasOfItems))

"k-medoids clustering."
(defn makeKMedoidsClustering
  [bufferedImage pixelLabels labelAreaMap areas]
  (let [rows (.getHeight bufferedImage)
        cols (.getWidth bufferedImage)
        areasOfItems (get-areas-of-items rows cols pixelLabels labelAreaMap)]
    (prn labelAreaMap)
    (prn areas)
    (prn (count pixelLabels))
    (prn (count (get pixelLabels 0)))
    (prn (get-areas-attribute-vectors pixelLabels areasOfItems))))
