(ns misoi.lab3.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util]))

"Lab3 graphics interface."
(declare applyGaussMatrixFilter)
(declare applyGaussVectorFilter)
(declare runForstnerDetector)

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
  (let [tripleSigma (* r 3)
        rows (range (- tripleSigma) (inc tripleSigma))
        cols (range (- tripleSigma) (inc tripleSigma))]
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
    (graphics/setImageMatrix bufferedImage gaussImageMatrix)))

"Gauss is a separable filter.
 Performance improve: use Gauss vectors instead of Gauss matrix.
 Not fixed yet."

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

"End of Gauss fast filtering.
 Not fixed yet."


"Forstner detector functions."

"Returns gradient x for element at specified row & col."
(defn- getGradientX
  [imageMatrix numOfCols rgbElementIntensity row col]
  (let [rightCol (+ col 1)
        leftCol (- col 1)
        rightGradient (if (< rightCol numOfCols)
                        (Math/abs (- (graphics/rgbIntensity (get-in imageMatrix [row rightCol]))
                                     rgbElementIntensity))
                        0)
        leftGradient (if (> leftCol 0)
                       (Math/abs (- (graphics/rgbIntensity (get-in imageMatrix [row leftCol]))
                                    rgbElementIntensity))
                       0)]
    (max rightGradient leftGradient)))

"Returns gradient y for element at specified row & col."
(defn- getGradientY
  [imageMatrix numOfRows rgbElementIntensity row col]
  (let [topRow (- row 1)
        bottomRow (+ row 1)
        topGradient (if (> topRow 0)
                      (Math/abs (- (graphics/rgbIntensity (get-in imageMatrix [topRow col]))
                                   rgbElementIntensity))
                      0)
        bottomGradient (if (< bottomRow numOfRows)
                         (Math/abs (- (graphics/rgbIntensity (get-in imageMatrix [bottomRow col]))
                                      rgbElementIntensity))
                         0)]
    (max topGradient bottomGradient)))

"Returns gradients x & y for image element located at the specified row & col of the image matrix."
(defn- getGradientsXY
  [imageMatrix numOfRows numOfCols rgbElementIntensity row col]
  [(getGradientX imageMatrix numOfCols rgbElementIntensity row col)
   (getGradientY imageMatrix numOfRows rgbElementIntensity row col)])

"Returns gradients for each element of the specified image matrix."
(defn- initImageMatrixGradients
  [imageMatrix numOfRows numOfCols]
  (graphics/traverseImageMatrix imageMatrix
                                (fn [imageMatrix rgbElement row col]
                                  (getGradientsXY imageMatrix numOfRows numOfCols (graphics/rgbIntensity rgbElement) row col))))

"Returns response of matrix according to the Forstner algorithm.
 Response = det(matrix) / trace(matrix)."
(defn- forstnerResponse
  [matrix]
  (let [matrix00 (nth (nth matrix 0) 0)
        matrix01 (nth (nth matrix 0) 1)
        matrix10 (nth (nth matrix 1) 0)
        matrix11 (nth (nth matrix 1) 1)
        det (- (* matrix00 matrix11) (* matrix01 matrix10))
        trace (+ matrix00 matrix11)
        zeroSafeTrace (if (not= (int trace) 0) trace 0.0001)]
    (/ det zeroSafeTrace)))

"Returns response for specified gradients."
(defn- getResponse
  [gaussMatrix gradX gradY]
  (let [gradMatrix [[(* gradX gradX) (* gradX gradY)]
                    [(* gradX gradY) (* gradY gradY)]]
        resultMatrix (apply util/matrixAddition (reduce (fn [matrices gaussMatrixRow]
                                                          (into matrices (map (fn [gaussMatrixEl]
                                                                                (util/matrixScalarMultiplication gaussMatrixEl gradMatrix))
                                                                              gaussMatrixRow)))
                                                        []
                                                        gaussMatrix))]
    (forstnerResponse resultMatrix)))

"Returns image matrix responses."
(defn- initImageMatrixResponses
  [imageMatrixGradients gaussMatrix]
  (graphics/traverseImageMatrix imageMatrixGradients
                                (fn [_ gradientsXY _ _]
                                  (getResponse gaussMatrix (get gradientsXY 0) (get gradientsXY 1)))))

"Returns matrix with corner indicators."
(defn- initImageMatrixCornerIndicators
  [imageMatrixResponses threshold]
  (map (fn [imageMatrixResponsesRow]
         (map (fn [imageMatrixResponsesEl]
                (if (> imageMatrixResponsesEl threshold) 1 0))
              imageMatrixResponsesRow))
       imageMatrixResponses))

"Returns image matrix with applied corner indicators."
(defn- applyImageMatrixCornerIndicators
  [imageMatrix imageMatrixCornerIndicators]
  (graphics/traverseImageMatrix imageMatrix
                                (fn [_ rgbElement row col]
                                  (if (= (nth (nth imageMatrixCornerIndicators row) col) 1)
                                    (-> rgbElement
                                        (graphics/setRGBRed 0)
                                        (graphics/setRGBGreen 255)
                                        (graphics/setRGBBlue 0))
                                    rgbElement))))

"Run Forstner corner detection algorithm."
(defn runForstnerDetector
  [bufferedImage gaussRadius]
  (let [imageMatrix (graphics/getImageMatrix bufferedImage)
        numOfRows (.getHeight bufferedImage)
        numOfCols (.getWidth bufferedImage)
        imageMatrixGradients (initImageMatrixGradients imageMatrix numOfRows numOfCols)
        gaussMatrix (initGaussMatrix gaussRadius)
        imageMatrixResponses (initImageMatrixResponses imageMatrixGradients gaussMatrix)
        threshold (Math/pow 10 (- 12))
        imageMatrixCornerIndicators (initImageMatrixCornerIndicators imageMatrixResponses threshold)
        resultImageItems (applyImageMatrixCornerIndicators imageMatrix imageMatrixCornerIndicators)]
    (graphics/setImageMatrix bufferedImage resultImageItems)))
