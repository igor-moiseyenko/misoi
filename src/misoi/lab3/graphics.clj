(ns misoi.lab3.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util]))

"Lab3 graphics interface."
(declare applyGaussMatrixFilter)
(declare applyGaussVectorFilter)

"Returns gauss koefficient.
 Row - stands for gauss matrix row,
 col - stands for gauss matrix col."
(defn- gaussMatrixKoefficient
  [row col r]
  (Math/pow Math/E (- (/ (+ (* row row) (* col col)) (* 2 (* r r))))))

"Returns gauss matrix with koefficients.
 Implements rule of 3 sigma (3 radius) - matrix 6 x 6 sigma."
(defn- initGaussMatrix
  [r]
  (let [halfN (* r 3)
        rows (range (- halfN) halfN)
        cols (range (- halfN) halfN)]
    (reduce (fn [gaussMatrix row]
              (conj gaussMatrix
                    (reduce (fn [gaussMatrixRow col]
                              (conj gaussMatrixRow (gaussMatrixKoefficient row col r)))
                            []
                            cols)))
            []
            rows)))

"Returns matrix with products of Gauss koefficients and image elements intensities."
(defn- initGaussMatrixIntensities
  [imageMatrix row col gaussMatrix getComponentIntensityFn]
  (map (fn [gaussMatrixRow gaussMatrixRowIndex]
         (map (fn [gaussMatrixEl gaussMatrixColIndex]
                (* gaussMatrixEl (graphics/getIntensity imageMatrix (+ row gaussMatrixRowIndex) (+ col gaussMatrixColIndex) getComponentIntensityFn)))
              gaussMatrixRow
              (iterate inc (- (quot (count gaussMatrixRow) 2)))))
       gaussMatrix
       (iterate inc (- (quot (count gaussMatrix) 2)))))

"Returns sum of matrix elements."
(defn- getMatrixElementsSum
  [matrix]
  (reduce (fn [result matrixRow]
            (+ result (reduce + matrixRow)))
          0
          matrix))

"Apply Gauss filtering on specified element (can be pixel)."
(defn- gaussFilter
  [imageMatrix gaussMatrix gaussDenominator rgbElement row col]
  (let [gaussMatrixRedIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBRed)
        gaussMatrixGreenIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBGreen)
        gaussMatrixBlueIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBBlue)
        redResult (/ (getMatrixElementsSum gaussMatrixRedIntensities) gaussDenominator)
        greenResult (/ (getMatrixElementsSum gaussMatrixGreenIntensities) gaussDenominator)
        blueResult (/ (getMatrixElementsSum gaussMatrixBlueIntensities) gaussDenominator)]
    (-> rgbElement
        (graphics/setRGBRed (int redResult))
        (graphics/setRGBGreen (int greenResult))
        (graphics/setRGBBlue (int blueResult)))))

"Apply Gauss filter on the specified image matrix wit specified radius."
(defn- applyGaussMatrixFilterOnImageMatrix
  [imageMatrix gaussMatrix gaussDenominator]
  (graphics/traverseImageMatrix imageMatrix
                                (fn [imageMatrix rgbElement row col]
                                  (gaussFilter imageMatrix gaussMatrix gaussDenominator rgbElement row col))))

(defn applyGaussMatrixFilter
  [bufferedImage r]
  (let [imageMatrix (graphics/getImageMatrix bufferedImage)
        gaussDenominator (* 2 Math/PI (* r r))
        gaussMatrix (initGaussMatrix r)
        gaussImageMatrix (applyGaussMatrixFilterOnImageMatrix imageMatrix gaussMatrix gaussDenominator)]
    (prn gaussImageMatrix)
    (graphics/setImageMatrix bufferedImage gaussImageMatrix)))

"Gauss is a separable filter.
 Performance improve: use Gauss vectors instead of Gauss matrix."

(defn- gaussVectorKoefficient
  [index r]
  (Math/pow Math/E (- (/ (* index index) (* 2 r r)))))

(defn- initGaussVector
  [r]
  (let [tripleSigma (* r 3)
        indices (range (- tripleSigma) tripleSigma)]
    (reduce (fn [gaussVector index]
              (conj gaussVector (gaussVectorKoefficient index r)))
            []
            indices)))

(defn- initGaussVectorRowIntensities
  [imageMatrix row col gaussVector getComponentIntensityFn]
  (map (fn [gaussVectorEl gaussVectorIndex]
         (* gaussVectorEl (graphics/getIntensity imageMatrix (+ row gaussVectorIndex) col getComponentIntensityFn)))
       gaussVector
       (iterate inc (- (quot (count gaussVector) 2)))))

(defn- initGaussVectorColIntensities
  [imageMatrix row col gaussVector getComponentIntensityFn]
  (map (fn [gaussVectorEl gaussVectorIndex]
         (* gaussVectorEl (graphics/getIntensity imageMatrix row (+ col gaussVectorIndex) getComponentIntensityFn)))
       gaussVector
       (iterate inc (- (quot (count gaussVector) 2)))))

(defn- gaussRowVectorFilter
  [imageMatrix gaussVector gaussDenominator rgbElement row col]
  (let [gaussVectorRowRedIntensities (initGaussVectorRowIntensities imageMatrix row col gaussVector graphics/getRGBRed)
        gaussVectorRowGreenIntensities (initGaussVectorRowIntensities imageMatrix row col gaussVector graphics/getRGBGreen)
        gaussVectorRowBlueIntensities (initGaussVectorRowIntensities imageMatrix row col gaussVector graphics/getRGBBlue)
        redResult (/ (reduce + gaussVectorRowRedIntensities) gaussDenominator)
        greenResult (/ (reduce + gaussVectorRowGreenIntensities) gaussDenominator)
        blueResult (/ (reduce + gaussVectorRowBlueIntensities) gaussDenominator)]
    (-> rgbElement
        (graphics/setRGBRed (int redResult))
        (graphics/setRGBGreen (int greenResult))
        (graphics/setRGBBlue (int blueResult)))))

(defn- gaussColVectorFilter
  [imageMatrix gaussVector gaussDenominator rgbElement row col]
  (let [gaussVectorColRedIntensities (initGaussVectorColIntensities imageMatrix row col gaussVector graphics/getRGBRed)
        gaussVectorColGreenIntensities (initGaussVectorColIntensities imageMatrix row col gaussVector graphics/getRGBGreen)
        gaussVectorColBlueIntensities (initGaussVectorColIntensities imageMatrix row col gaussVector graphics/getRGBBlue)
        redResult (/ (reduce + gaussVectorColRedIntensities) gaussDenominator)
        greenResult (/ (reduce + gaussVectorColGreenIntensities) gaussDenominator)
        blueResult (/ (reduce + gaussVectorColBlueIntensities) gaussDenominator)]
    (-> rgbElement
        (graphics/setRGBRed (int redResult))
        (graphics/setRGBGreen (int greenResult))
        (graphics/setRGBBlue (int blueResult)))))

(defn- applyGaussVectorFilterOnImageMatrix
  [imageMatrix gaussVector gaussDenominator]
  (let [gaussMatrixRowsApplied (graphics/traverseImageMatrix imageMatrix
                                                             (fn [imageMatrix rgbElement row col]
                                                               (gaussRowVectorFilter imageMatrix gaussVector gaussDenominator rgbElement row col)))]
    (graphics/traverseImageMatrix gaussMatrixRowsApplied
                                  (fn [imageMatrix rgbElement row col]
                                    (gaussColVectorFilter imageMatrix gaussVector gaussDenominator rgbElement row col)))))

(defn- applyGaussVectorFilterOnRows
  [imageMatrix gaussVector gaussDenominator]
  (graphics/traverseImageMatrix imageMatrix
                                (fn [imageMatrix rgbElement row col]
                                  (gaussRowVectorFilter imageMatrix gaussVector gaussDenominator rgbElement row col))))

(defn- applyGaussVectorFilterOnCols
  [imageMatrix gaussVector gaussDenominator]
  (graphics/traverseImageMatrix imageMatrix
                                (fn [imageMatrix rgbElement row col]
                                  (gaussColVectorFilter imageMatrix gaussVector gaussDenominator rgbElement row col))))

(defn applyGaussVectorFilter
  [bufferedImage r]
  (let [imageMatrix (graphics/getImageMatrix bufferedImage)
        gaussVector (initGaussVector r)
        gaussDenominator (reduce + gaussVector)
        gaussMatrixRowsApplied (applyGaussVectorFilterOnRows imageMatrix gaussVector gaussDenominator)
        gaussImageMatrix (applyGaussVectorFilterOnCols gaussMatrixRowsApplied gaussVector gaussDenominator)]
    (prn gaussImageMatrix)
    (graphics/setImageMatrix bufferedImage gaussImageMatrix)))
