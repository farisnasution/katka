(ns katka.util.math)

(defn ->pos
  [n]
  (cond
   (not (number? n)) 0
   (neg? n) (* -1 n)
   :else n))

(defn ->neg
  [n]
  (cond
   (not (number? n)) 0
   (pos? n) (* -1 n)
   :else n))

(defn max-value
  [xs]
  (apply max xs))

(defn min-value
  [xs]
  (let [d (apply min xs)]
    (if (neg? d)
      d
      0)))

(defn floor
  [x]
  (.floor js/Math x))

(defn ceil
  [x]
  (.ceil js/Math x))
