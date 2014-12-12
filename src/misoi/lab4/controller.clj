(ns misoi.lab4.controller
  (:require [seesaw.icon :as seesawIcon]
            [misoi.lab4.graphics :as lab4Graphics])
  (:use [seesaw.core]
        [seesaw.chooser])
  (:import (javax.imageio ImageIO)))

"Main controller interface"
(declare init)

"Shared objects"
(declare initialImageBuffer)
(def kohonenNetwork nil)

(defn- refresh-icon-label
  [root]
  (config! (select root [:#icon-label])
           :icon initialImageBuffer))

(defn- loadImagePath
  [root imagePath]
  (config! (select root [:#icon-path-label])
           :text imagePath))

(defn- loadImage
  [root file]

  (def initialImageBuffer (ImageIO/read file))
  (config! (select root [:#icon-label])
           :icon (seesawIcon/icon initialImageBuffer)))

(defn- imagesFileFilter
  [file]
  (.endsWith (clojure.string/lower-case (.getAbsolutePath file)) ".png"))

"Open menu item controller."
(defn- initOpenMenuItem
  [root]
  (listen (select root [:#open-menu-item])
          :action (fn [event]
                    (choose-file :filters [["Images" ["png" "jpeg"]]
                                           (file-filter "ImagesFF" imagesFileFilter)]
                                 :success-fn (fn [fc file]
                                               (loadImage root file)
                                               (loadImagePath root (.getAbsolutePath file)))))))

"Learning menu items controllers."

(defn- initILearningController
  [root]
  (listen (select root [:#i-menu-item])
          :action (fn [event]
                    (let [kohonenNetworkResult (lab4Graphics/run-learning initialImageBuffer kohonenNetwork :i)]
                      (def kohonenNetwork kohonenNetworkResult)))))

(defn- initVLearningController
  [root]
  (listen (select root [:#v-menu-item])
          :action (fn [event]
                    (let [kohonenNetworkResult (lab4Graphics/run-learning initialImageBuffer kohonenNetwork :v)]
                      (def kohonenNetwork kohonenNetworkResult)))))

(defn- initMLearningController
  [root]
  (listen (select root [:#m-menu-item])
          :action (fn [event]
                    (let [kohonenNetworkResult (lab4Graphics/run-learning initialImageBuffer kohonenNetwork :m)]
                      (def kohonenNetwork kohonenNetworkResult)))))

"Noise menu items controllers."

(defn- initNoise5percentController
  [root]
  (listen (select root [:#noise-5-percent-menu-item])
          :action (fn [event]
                    (lab4Graphics/make-noise initialImageBuffer 5)
                    (refresh-icon-label root))))

(defn- initNoise10percentController
  [root]
  (listen (select root [:#noise-10-percent-menu-item])
          :action (fn [event]
                    (lab4Graphics/make-noise initialImageBuffer 10)
                    (refresh-icon-label root))))

(defn- initNoise20percentController
  [root]
  (listen (select root [:#noise-20-percent-menu-item])
          :action (fn [event]
                    (lab4Graphics/make-noise initialImageBuffer 20)
                    (refresh-icon-label root))))

(defn- initNoise50percentController
  [root]
  (listen (select root [:#noise-50-percent-menu-item])
          :action (fn [event]
                    (lab4Graphics/make-noise initialImageBuffer 50)
                    (refresh-icon-label root))))

(defn- initNoise100percentController
  [root]
  (listen (select root [:#noise-100-percent-menu-item])
          :action (fn [event]
                    (lab4Graphics/make-noise initialImageBuffer 100)
                    (refresh-icon-label root))))

"Run menu items controllers."

(defn- initRunKohonenNetworkController
  [root]
  (listen (select root [:#run-kohonen-network-menu-item])
          :action (fn [event]
                    (let [result (lab4Graphics/run-kohonen-network initialImageBuffer kohonenNetwork)]
                      (prn (key result))
                      (-> (dialog :content (str "Result: " (key result))) pack! show!)))))

(defn init
  [root]
  (initOpenMenuItem root)

  "Init learning menu items."
  (initILearningController root)
  (initVLearningController root)
  (initMLearningController root)

  "Init noise menu items."
  (initNoise5percentController root)
  (initNoise10percentController root)
  (initNoise20percentController root)
  (initNoise50percentController root)
  (initNoise100percentController root)

  "Init Kohonen Network menu item controller."
  (initRunKohonenNetworkController root))
