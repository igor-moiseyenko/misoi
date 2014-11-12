(ns misoi.core
  (:require [misoi.lab1.core :as lab1-core]
            [misoi.lab2.core :as lab2-core]))

(defn f1 [v]
  (reduce (fn [row1 row2]
            (conj row1
                  (persistent! row2)))
          []
          (persistent! v)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (lab2-core/-main args))
