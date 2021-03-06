(ns katka.util.data)

(defn map-to
  [data ks]
  (map #(get-in % ks) data))

(defn separate-data
  [m]
  (apply map vector m))

(defn format-data
  [data & ks]
  (let [fns (->> ks
                 (filter #(not (nil? %)))
                 (map (fn [k] #(get-in % k))))
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
