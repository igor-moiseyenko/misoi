(ns misoi.lab1.core
  (:require [misoi.lab1.controller :as controller])
  (:use [misoi.lab1.view :only (f)]
        [seesaw.core]))

(defn -main
  [& args]
  (prn "Lab1 is running...")
  (controller/init f)
  (-> f
      pack!
      show!))
