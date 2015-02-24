(ns katka.util.scale
  (:require [katka.util.math :as math]))

(defn -construct-domain-range
  [d r]
  (let [default (array 0 1)]
    {:d (if (nil? d)
          default
          (apply array d))
     :r (if (nil? d)
          default
          (apply array r))}))

(defn linear
  [{:keys [d r]}]
  (let [{:keys [d r]} (-construct-domain-range d r)]
    (-> js/d3
        (.-scale)
        (.linear)
        (.domain d)
        (.range r))))

(defn ordinal-range-bands
  [{:keys [d r]}]
  (let [{:keys [d r]} (-construct-domain-range d r)]
    (-> js/d3
        (.-scale)
        (.ordinal)
        (.domain d)
        (.rangeBands r))))

(defn ordinal-range-round-bands
  [{:keys [d r p]}]
  (let [{:keys [d r]} (-construct-domain-range d r)]
    (-> js/d3
        (.-scale)
        (.ordinal)
        (.domain d)
        (.rangeRoundBands r p))))

(defn simple-linear-scale
  [num-data max-range]
  (let [min-value (math/min-value num-data)
        min-domain (if (neg? min-value) min-value 0)
        max-domain (math/max-value num-data)]
    (linear {:d [min-domain max-domain]
             :r [0 max-range]})))

(defn simple-ordinal-scale
  ([ord-data max-range padding]
   (ordinal-range-round-bands {:d ord-data
                               :r [0 max-range]
                               :p padding}))
  ([ord-data max-range]
   (simple-ordinal-scale ord-data max-range 0)))
