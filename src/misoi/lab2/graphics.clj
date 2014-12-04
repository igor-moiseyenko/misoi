(ns misoi.lab2.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util])
  (:import (java.awt.image.BufferedImage)))

"Lab2 graphics interface"
(declare makeBinaryThresholding)

(declare makeMedianFilter)

(declare makeRecursiveSegmentation)
(declare makeSequentialSegmentation)
(declare makeKMedoidsClustering)

(declare initialLabels)

"Returns min byte value in case of x less than threshold, max byte value - otherwise."
(defn- binaryThresholdOperator
  [x thresholdingValue]
  (if (< x thresholdingValue) 0 255))

"Returns RGBPixel after applying logical AND on each RGB component after applying threshold operator on each of them."
(defn- makeRGBPixelThresholding
  [RGBPixel thresholdingValue]
  (let [RGBRedResult (binaryThresholdOperator (graphics/getRGBRed RGBPixel) thresholdingValue)
        RGBGreenResult (binaryThresholdOperator (graphics/getRGBGreen RGBPixel) thresholdingValue)
        RGBBlueResult (binaryThresholdOperator (graphics/getRGBBlue RGBPixel) thresholdingValue)
        RGBResult (and RGBRedResult RGBGreenResult RGBBlueResult)]
    (-> RGBPixel
        (graphics/setRGBRed RGBResult)
        (graphics/setRGBGreen RGBResult)
        (graphics/setRGBBlue RGBResult))))

"Make binary thresholding of the specified buffered image."
(defn makeBinaryThresholding
  [bufferedImage thresholdingValue]
  (graphics/traverseAndSetPixels bufferedImage (fn [RGBPixel]
                                                 (makeRGBPixelThresholding RGBPixel thresholdingValue))))

"
Functions to work with image noise.
"

"Median filter operator."
(defn- medianFilterOperator
  [pixels]
  (let [sortedPixels (vec (sort pixels))
        len (count sortedPixels)
        midIndex (quot len 2)]
    (if (odd? len)
      (get sortedPixels midIndex)
      (quot (+ (get sortedPixels (- midIndex 1)) (get sortedPixels midIndex)) 2))))

"Median filter."
(defn- medianFilter
  [bufferedImage width height col row filterSize]
  (let [RGBPixel (.getRGB bufferedImage col row)
        indent (quot filterSize 2)
        colIndices (range (- col indent) (inc (+ col indent)))
        rowIndices (range (- row indent) (inc (+ row indent)))
        pixelsUnderFilter (reduce (fn [pixels colIndex]
                                    (into pixels (reduce (fn [column rowIndex]
                                                           (conj column (if (and (> colIndex 0) (> rowIndex 0) (< colIndex width) (< rowIndex height))
                                                                          (.getRGB bufferedImage colIndex rowIndex)
                                                                          0)))
                                                         []
                                                         rowIndices)))
                                  []
                                  colIndices)
        redValues (map graphics/getRGBRed pixelsUnderFilter)
        greenValues (map graphics/getRGBGreen pixelsUnderFilter)
        blueValues (map graphics/getRGBBlue pixelsUnderFilter)]
    (.setRGB bufferedImage col row (-> RGBPixel
                                   (graphics/setRGBRed (medianFilterOperator redValues))
                                   (graphics/setRGBGreen (medianFilterOperator greenValues))
                                   (graphics/setRGBBlue (medianFilterOperator blueValues))))))

"Median filter."
(defn makeMedianFilter
  [bufferedImage filterSize]
  (let [width (.getWidth bufferedImage)
        height (.getHeight bufferedImage)]
    (graphics/traversePixels bufferedImage (fn [bufferedImage col row]
                                             (medianFilter bufferedImage width height col row filterSize)))))

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
   1 [ 100 200 140]
   2 [ 120 140 160]
   3 [ 200 160 180]
   4 [ 160 180 200]
   5 [ 40 200 220]
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

"Return true if the item is not a perimeter item with 4-connection type."
(defn- is-inner4-item
  [pixelLabels item]
  (let [row (get item 0)
        col (get item 1)]
    (and (not= (get-in pixelLabels [row (- col 1)]) 0)
         (not= (get-in pixelLabels [(- row 1) col]) 0)
         (not= (get-in pixelLabels [row (+ col 1)]) 0)
         (not= (get-in pixelLabels [(+ row 1) col]) 0))))

"Get perimeter of area items, which is equal "
(defn- get-area-perimeter
  [pixelLabels areaItems]
  (- (count areaItems)
     (count (reduce (fn [innerItems item]
                      (if (is-inner4-item pixelLabels item)
                        (conj innerItems item)
                        innerItems))
                    []
                    areaItems))))

"Get attribute vector for each area."
(defn- get-areas-attribute-vectors
  [pixelLabels areasOfItems]
  (reduce (fn [areasAttributeVectors areaOfItems]
            (let [area (get areaOfItems 0)
                  items (get areaOfItems 1)
                  square (get-area-square items)
                  perimeter (get-area-perimeter pixelLabels items)
                  capacity (/ (Math/pow perimeter 2) square)]
              (assoc areasAttributeVectors area
                     [square perimeter capacity])))
          {}
          areasOfItems))

"
Clustering.
PAM - partition around medoids.
Step 1 - build.
Step 2 - swap.
"

"Get distance between specified objects by their attributes."
(defn- object-distance
  [obj1Attributes obj2Attributes]
  (apply + (map (fn [attr1 attr2]
                  (Math/abs (- attr1 attr2)))
                obj1Attributes
                obj2Attributes)))

"Get sum of distances between objects."
(defn- get-sum-of-distances
  [centerAttributes objectsAttributes]
  (apply + (map (fn [objectAttributes]
                  (object-distance centerAttributes objectAttributes))
                objectsAttributes)))

"Returns object with the least of the distances sum."
(defn- min-object-distances-sum
  ([obj1] obj1)
  ([obj1 obj2] (if (< (get obj1 1) (get obj2 1)) obj1 obj2))
  ([obj1 obj2 & more] (reduce min-object-distances-sum (min-object-distances-sum obj1 obj2) more)))

"Get first selected center"
(defn- get-first-selected-objectId
  [objects]
  (get (apply min-object-distances-sum
              (map (fn [objectId]
                     [objectId (get-sum-of-distances (get objects objectId) (vals (dissoc objects objectId)))])
                   (keys objects)))
       0))

"Returns distance between object and its closest central object."
(defn- closest-center-object-distance
  [objects centerIds objectAttrs]
  (apply min (map (fn [centerId]
                    (object-distance objectAttrs (get objects centerId)))
                  centerIds)))

"Returns total gain for potential center with specified id."
(defn- potential-center-total-gain
  [objects centerIds potentialCenterId]
  (apply + (map (fn [objectId]
                  (max (- (closest-center-object-distance objects centerIds (get objects objectId))
                          (object-distance (get objects objectId) (get objects potentialCenterId)))
                       0))
                (keys (util/dissoc-keys objects (conj centerIds potentialCenterId))))))

"Returns potential center with the max total gain"
(defn- max-object-total-gain
  ([obj1] obj1)
  ([obj1 obj2] (if (> (get obj1 1) (get obj2 1)) obj1 obj2))
  ([obj1 obj2 & more] (reduce max-object-total-gain (max-object-total-gain obj1 obj2) more)))

"Returns all selected object ids based on first selected object and needed number of centers."
(defn- get-all-selected-objectIds
  [numOfCenters objects centerIds]
  (let [restCentersSize (- numOfCenters (count centerIds))]
    (if (> restCentersSize 0)
      (get-all-selected-objectIds numOfCenters
                                      objects
                                      (conj centerIds (get (apply max-object-total-gain
                                                                  (map (fn [objectId]
                                                                         [objectId (potential-center-total-gain objects centerIds objectId)])
                                                                       (keys (util/dissoc-keys objects centerIds))))
                                                           0)))
      centerIds)))

"PAM algorithm build phase - complete initial set of selected object (potential medoids)."
(defn- pam-build-phase
  [numOfMedoids objects]
  (let [centerIds [(get-first-selected-objectId objects)]]
    (prn centerIds)
    (get-all-selected-objectIds numOfMedoids objects centerIds)))

"Returns closest medoid."
(defn- closest-medoid
  [objects medoidIds objectAttributes]
  (get (apply min-object-distances-sum
              (map (fn [medoidId]
                     [medoidId (object-distance (get objects medoidId) objectAttributes)])
                   medoidIds))
       0))

"PAM (partition around medoids) algorithm implementation."
(defn- make-pam-clustering
  [numOfMedoids areasAttributeVectors]
  (prn numOfMedoids areasAttributeVectors)
  (let [medoidIds (pam-build-phase numOfMedoids areasAttributeVectors)]
    (prn medoidIds)
    (reduce (fn [areasMedoidsMap area]
              (assoc areasMedoidsMap area (closest-medoid areasAttributeVectors medoidIds (get areasAttributeVectors area))))
            {}
            (keys areasAttributeVectors))))

"Returns cluster - area mapping."
(defn- cluster-area-map
  [labelAreaMap areasMedoidsMap]
  (reduce (fn [clusteredLabelAreaMap label]
            (assoc clusteredLabelAreaMap label (get areasMedoidsMap (get labelAreaMap label))))
          {}
          (keys labelAreaMap)))

"k-medoids clustering."
(defn makeClustering
  [bufferedImage pixelLabels labelAreaMap numOfMedoids]
  (let [rows (.getHeight bufferedImage)
        cols (.getWidth bufferedImage)
        areasOfItems (get-areas-of-items rows cols pixelLabels labelAreaMap)
        areasAttributeVectors (get-areas-attribute-vectors pixelLabels areasOfItems)
        areasMedoidsMap (make-pam-clustering numOfMedoids areasAttributeVectors)]
    (prn labelAreaMap)
    (prn areasMedoidsMap)
    (let [clusteredLabelAreaMap (cluster-area-map labelAreaMap areasMedoidsMap)]
      (prn clusteredLabelAreaMap)
      (set-color-to-labels bufferedImage pixelLabels clusteredLabelAreaMap))))
