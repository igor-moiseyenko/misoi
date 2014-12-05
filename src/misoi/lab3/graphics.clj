(ns misoi.lab3.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util]))

"Lab3 graphics interface."
(declare applyGaussFilter)

"Returns gauss koefficient.
 Row - stands for gauss matrix row,
 col - stands for gauss matrix col."
(defn- gaussKoefficient
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
                              (conj gaussMatrixRow (gaussKoefficient row col r)))
                            []
                            cols)))
            []
            rows)))

"Returns image element intensity.
 Returns 0 in case of the image element with specified row and col doesn't exists."
(defn- getIntensity
  [imageMatrix row col getComponentIntensityFn]
  (if (and (>= row 0) (>= col 0) (< row (count imageMatrix)) (< col (count (get imageMatrix 0))))
    (getComponentIntensityFn (get-in imageMatrix [row col]))
    0))

"Returns matrix with products of Gauss koefficients and image elements intensities."
(defn- initGaussMatrixIntensities
  [imageMatrix row col gaussMatrix getComponentIntensityFn]
  (map (fn [gaussMatrixRow gaussMatrixRowIndex]
         (map (fn [gaussMatrixEl gaussMatrixColIndex]
                (* gaussMatrixEl (getIntensity imageMatrix (+ row gaussMatrixRowIndex) (+ col gaussMatrixColIndex) getComponentIntensityFn)))
              gaussMatrixRow
              (iterate inc (- (quot (count gaussMatrixRow) 2)))))
       gaussMatrix
       (iterate inc (- (quot (count gaussMatrix) 2)))))

(defn- getMatrixElementsSum
  [matrix]
  (reduce (fn [result matrixRow]
            (+ result (reduce + matrixRow)))
          0
          matrix))

"Apply Gauss filtering on specified element (can be pixel)."
(defn- gaussFilter
  [imageMatrix rgbElement row col r]
  (let [gaussMatrix (initGaussMatrix r)
        gaussDenominator (* 2 Math/PI (* r r))
        gaussMatrixRedIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBRed)
        gaussMatrixGreenIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBGreen)
        gaussMatrixBlueIntensities (initGaussMatrixIntensities imageMatrix row col gaussMatrix graphics/getRGBBlue)
        redResult (/ (getMatrixElementsSum gaussMatrixRedIntensities) gaussDenominator)
        greenResult (/ (getMatrixElementsSum gaussMatrixGreenIntensities) gaussDenominator)
        blueResult (/ (getMatrixElementsSum gaussMatrixBlueIntensities) gaussDenominator)]
    (-> rgbElement
        (graphics/setRGBRed (int redResult))
        (graphics/setRGBGreen (int greenResult))
        (graphics/setRGBBlue (int blueResult)))))

(defn applyGaussFilter
  [bufferedImage r]
  (let [imageMatrix (graphics/getImageMatrix bufferedImage)
        gaussImageMatrix (graphics/traverseImageMatrix imageMatrix (fn [imageMatrix element row col] (gaussFilter imageMatrix element row col r)))]
    (prn imageMatrix)
    (prn gaussImageMatrix)
    (graphics/setImageMatrix bufferedImage gaussImageMatrix)))
