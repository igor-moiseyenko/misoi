(ns misoi.lab2.core
  (:require [misoi.lab2.controller :as controller])
  (:use [misoi.lab2.view :only (f)]
        [seesaw.core]))

(defn -main
  [& args]
  (prn "Lab2 is running...")
  (controller/init f)
  (-> f pack! show!))
