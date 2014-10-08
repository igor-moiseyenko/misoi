(ns misoi.core
  (:gen-class))

(require '[misoi.lab1.core :as lab1-core])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (lab1-core/-main args))
