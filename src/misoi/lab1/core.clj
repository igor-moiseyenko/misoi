(ns misoi.lab1.core)

(use 'misoi.lab1.view)
(use 'misoi.lab1.controller)
(use 'seesaw.core)

(defn -main
  [& args]
  (prn "Lab1 is running...")
  (init-controller f)
  (-> f
      pack!
      show!))
