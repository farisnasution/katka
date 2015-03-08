(ns katka.scale.ordinal
  (:use-macros [katka.macro :only [not-nil?]]))

(defn -invoke-domain
  [scale d]
  (.domain scale (apply array d)))

(defn -invoke-range
  [scale r]
  (.range scale (apply array r)))

(defn -invoke-range-points
  [scale [interval padding]]
  (let [r (apply array interval)]
    (if-not (nil? padding)
      (.rangePoints scale r padding)
      (.rangePoints scale r))))

(defn -invoke-range-round-points
  [scale [interval padding]]
  (let [r (apply array interval)]
    (if-not (nil? padding)
      (.rangeRoundPoints scale r padding)
      (.rangeRoundPoints scale r))))

(defn -invoke-range-bands
  [scale [interval padding outer-padding]]
  (let [r (apply array interval)]
    (if-not (nil? padding)
      (if-not (nil? outer-padding)
        (.rangeBands scale r padding outer-padding)
        (.rangeBands scale r padding))
      (.rangeBands scale r))))

(defn -invoke-range-round-bands
  [scale [interval padding outer-padding]]
  (let [r (apply array interval)]
    (if-not (nil? padding)
      (if-not (nil? outer-padding)
        (.rangeRoundBands scale r padding outer-padding)
        (.rangeRoundBands scale r padding))
      (.rangeRoundBands scale r))))

(defn ordinal-scale
  [{:keys [domain range-scale range-points
           range-round-points range-bands
           range-round-bands]}]
  (cond-> (.ordinal (.-scale js/d3))
          (not-nil? domain) (-invoke-domain domain)
          (not-nil? range-scale) (-invoke-range range-scale)
          (not-nil? range-points) (-invoke-range-points range-points)
          (not-nil? range-round-points) (-invoke-range-round-points
                                         range-round-points)
          (not-nil? range-bands) (-invoke-range-bands range-bands)
          (not-nil? range-round-bands) (-invoke-range-round-bands
                                        range-round-bands)))

(defn ordinal-10
  []
  (.category10 (.-scale js/d3)))

(defn ordinal-20
  []
  (.category20 (.-scale js/d3)))

(defn ordinal-20b
  []
  (.category20b (.-scale js/d3)))

(defn ordinal-20c
  []
  (.category20c (.-scale js/d3)))
