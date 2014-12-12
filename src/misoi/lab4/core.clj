(ns misoi.lab4.core
  (:require [misoi.lab4.controller :as controller])
  (:use [misoi.lab4.view :only (f)]
        [seesaw.core]))

(defn -main
  [& args]
  (controller/init f)
  (-> f pack! show!))
