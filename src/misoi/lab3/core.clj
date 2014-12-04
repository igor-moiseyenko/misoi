(ns misoi.lab3.core
  (:require [misoi.lab3.controller :as controller])
  (:use [misoi.lab3.view :only (f)]
        [seesaw.core]))

(defn -main
  [& args]
  (controller/init f)
  (-> f pack! show!))
