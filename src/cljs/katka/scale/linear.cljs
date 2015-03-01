(ns katka.scale.linear
  (:use-macros [katka.macro :only [not-nil?]]))

(defn -invoke-domain
  [scale d]
  (.domain scale (apply array d)))

(defn -invoke-range
  [scale r]
  (.range scale (apply array r)))

(defn -invoke-range-round
  [scale r]
  (.rangeRound scale r))

(defn -invoke-interpolate
  [scale r]
  (.interpolate scale r))

(defn -invoke-clamp
  [scale r]
  (.clamp scale r))

(defn -invoke-nice
  [scale r]
  (.nice scale r))

(defn -invoke-ticks
  [scale r]
  (.ticks scale r))

(defn -invoke-tick-format
  [scale [c f]]
  (if-not (nil? f)
    (.tickFormat scale c f)
    (.tickFormat scale c)))

(defn linear-scale
  [{:keys [domain range-scale range-round
           interpolate clamp nice
           ticks tick-format]}]
  (cond-> (.linear (.-scale js/d3))
          (not-nil? domain) (-invoke-domain domain)
          (not-nil? range-scale) (-invoke-range range-scale)
          (not-nil? range-round) (-invoke-range-round range-round)
          (not-nil? interpolate) (-invoke-interpolate interpolate)
          (not-nil? clamp) (-invoke-clamp clamp)
          (not-nil? nice) (-invoke-nice nice)
          (not-nil? ticks) (-invoke-ticks ticks)
          (not-nil? tick-format) (-invoke-tick-format tick-format)))
