(ns katka.util.data)

(defn map-to
  [data ks]
  (map #(get-in % ks) data))

(defn separate-data
  [data]
  [(map first data) (map last data)])

(defn format-data
  [data & ks]
  (let [fns (map (fn [k]
                   #(get-in % k)) ks)
        f (apply juxt fns)]
    (map f data)))

(defn pos-data
  [data ks]
  (filter (fn [d]
            (let [new-d (get-in d ks)]
              (or (pos? new-d)
                  (zero? new-d)))) data))

(defn neg-data
  [data ks]
  (filter (fn [d]
            (let [new-d (get-in d ks)]
              (neg? new-d))) data))

(defn inner-container
  [width height {:keys [left right top bottom]}]
  {:width (- width left right)
   :height (- height top bottom)})

(defn translate
  [{:keys [pos-x pos-y]}]
  (let [x (if (number? pos-x) pos-x 0)
        y (if (number? pos-y) pos-y 0)]
    (str "translate(" x "," y ")")))
