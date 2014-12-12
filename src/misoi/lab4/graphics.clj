(ns misoi.lab4.graphics
  (:require [misoi.graphics :as graphics]
            [misoi.util :as util])
  (:import (java.awt.image.BufferedImage)))

(declare run-learning)
(declare make-noise)
(declare run-kohonen-network)

(def learningVelocity 0.7)
(def inputHigh 1)
(def inputLow 0)

(defn- init-kohonen-network-neuron
  [imageMatrix]
  (graphics/traverseImageMatrix imageMatrix (fn [_ _ _ _]
                                              1)))

(defn- init-kohonen-network
  [imageMatrix]
  { :i (init-kohonen-network-neuron imageMatrix)
    :v (init-kohonen-network-neuron imageMatrix)
    :m (init-kohonen-network-neuron imageMatrix) })

(defn- define-input
  [input]
  (if (= input 0) inputHigh inputLow))

(defn- get-neuron-distance
  [neuron1 neuron2]
  (util/matrixElementsSum (map (fn [neuron1Row neuron2Row]
                                 (map (fn [neuron1El neuron2El]
                                        (+ neuron1El neuron2El))
                                      neuron1Row neuron2Row))
                               neuron1
                               neuron2)))

(defn- recalculate-weight
  [input weight learningVelocity]
  (+ weight (* learningVelocity (- input weight))))

"Run Kohonen Network learning mechanism."
(defn run-learning
  [imageBuffer kohonenNetwork correctResult]
  (let [imageMatrix (graphics/getImageMatrix imageBuffer)
        kohonenNetwork (if (not= kohonenNetwork nil) kohonenNetwork (init-kohonen-network imageMatrix))
        kohonenNetworkNeuron (get kohonenNetwork correctResult)
        kohonenNetworkNeuronResult (graphics/traverseImageMatrix imageMatrix
                                                                 (fn [imageMatrix element row col]
                                                                   (let [input (define-input (graphics/getRGBRed element))
                                                                         weight (nth (nth kohonenNetworkNeuron row) col)]
                                                                     (recalculate-weight input weight learningVelocity))))]
    (prn kohonenNetworkNeuronResult)
    (assoc kohonenNetwork correctResult kohonenNetworkNeuronResult)))

(defn- kohonen-network-neuron-power
  [neuronMapEntry imageMatrix]
  (let [kohonenNetworkNeuron (val neuronMapEntry)
        sum (util/matrixElementsSum (map (fn [imageMatrixRow neuronMatrixRow]
                                           (map (fn [imageMatrixEl neuronMatrixEl]
                                                  (* (define-input (graphics/getRGBRed imageMatrixEl)) neuronMatrixEl))
                                                imageMatrixRow
                                                neuronMatrixRow))
                                         imageMatrix
                                         kohonenNetworkNeuron))]
    (+ sum 0)))

(defn- negateImageElement
  [element]
  (let [red (graphics/getRGBRed element)
        green (graphics/getRGBGreen element)
        blue (graphics/getRGBBlue element)]
    (-> element
        (graphics/setRGBRed (- 255 red))
        (graphics/setRGBGreen (- 255 green))
        (graphics/setRGBBlue (- 255 blue)))))

"Make noise on the specified image.
 Function applies inversion on the specified percentage of image elements."
(defn make-noise
  [imageBuffer percentage]
  (let [imageMatrix (graphics/getImageMatrix imageBuffer)
        denominator (/ 100 percentage)
        imageMatrixWithNoise (graphics/traverseImageMatrix imageMatrix
                                                           (fn [imageMatrix element row col]
                                                             (if (not= (mod col denominator) 0)
                                                               element
                                                               (negateImageElement element))))]
    (prn imageMatrixWithNoise)
    (graphics/traversePixels imageBuffer (fn [imageBuffer col row]
                                           (.setRGB imageBuffer row col (nth (nth imageMatrixWithNoise col) row))))))

(defn run-kohonen-network
  [imageBuffer kohonen-network]
  (let [imageMatrix (graphics/getImageMatrix imageBuffer)]
    (apply max-key (fn [neuronMapEntry]
                     (kohonen-network-neuron-power neuronMapEntry imageMatrix))
           kohonen-network)))
