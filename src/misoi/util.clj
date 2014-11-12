(ns misoi.util)

(defn assoc-in!
  [m [k & ks] v]
  (if ks
    (assoc! m k (assoc-in! (get m k) ks v))
    (assoc! m k v)))

(def create-vector (comp vec repeat))

(take 10 (repeat 10 3))

(defn create-matrix
  [xMax yMax]
  (create-vector yMax
                 (create-vector xMax 0)))

(defn convert-matrix-to-transient
  [m]
  (reduce (fn [row1 row2]
            (conj! row1
                   (transient row2)))
          (transient [])
          m))

(defn convert-matrix-to-persistent
  [m]
  (reduce (fn [row1 row2]
            (conj row1 (persistent! row2)))
          []
          (persistent! m)))
