(ns katka.chart.waterfall
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as ax]
            [katka.scale.linear :as lsc]
            [katka.scale.ordinal :as osc]
            [katka.util.math :as math]
            [katka.util.data :as data])
  (:use-macros [katka.macro :only [defcomponent]]))

(defcomponent waterfall-rect
  [{:keys [rect text line]} owner]
  (display-name [_] "waterfall-rect")
  (render [_]
          [:g {}
           (om/build shape/rect rect)
           (when (true? (:show-text? text))
             (om/build shape/text text))
           (om/build shape/line line)]))

(defn min-max-data
  [data]
  (letfn [(hi-lo [[min-data max-data] d]
            [(min min-data d) (max max-data d)])]
    (first (reduce (fn [p n]
                     (let [c (first p)
                           v (second p)
                           new-v (+ v n)]
                       [(hi-lo c new-v) new-v])) [[0 0] 0] data))))

(defn construct-rect-width
  [x-scale]
  (.rangeBand x-scale))

(defn construct-rect-height
  [y-scale]
  (fn [num-data]
    (-> (y-scale num-data)
        (- (y-scale 0))
        math/->pos)))

(defn construct-rect-x
  [x-scale]
  (fn [ord-data]
    (x-scale ord-data)))

(defn construct-rect-y
  [y-scale]
  (fn [num-data]
    (if (neg? num-data)
      (y-scale 0)
      (y-scale num-data))))

(defn construct-rect-style
  [style-opts]
  (into {:fill "steelblue"}
        style-opts))

(defn construct-rect
  [x-scale y-scale style-opts]
  (let [width-creator (construct-rect-width x-scale)
        height-creator (construct-rect-height y-scale)
        x-creator (construct-rect-x x-scale)
        y-creator (construct-rect-y y-scale)
        {:keys [fill stroke]} (construct-rect-style style-opts)]
    (fn [[ord-data y-data num-data]]
      {:width width-creator
       :height (height-creator num-data)
       :x (x-creator ord-data)
       :y (y-creator num-data)
       :fill fill
       :stroke stroke})))

(defn construct-text-x-dx
  [x-scale]
  (fn [_]
    {:x (/ (.rangeBand x-scale) 2)}))

(defn construct-text-y-dy
  [y-scale]
  (fn [num-data]
    {:y (+ 5 (y-scale num-data))
     :dy (if (neg? num-data)
           "-0.75em"
           "0.75em")}))

(defn construct-text
  [x-scale y-scale text-opts]
  (let [x-creator (construct-text-x-dx x-scale)
        y-creator (construct-text-y-dy y-scale)]
    (fn [[ord-data y-data num-data]]
      (merge (x-creator ord-data)
             (y-creator num-data)
             {:text-anchor "middle"
              :content ord-data}
             text-opts))))

(defn construct-line-x1
  [x-scale]
  (+ 5 (.rangeBand x-scale)))

(defn construct-line-y1
  [y-scale]
  (fn [d]
    (y-scale d)))

(defn construct-line-x2
  [x-scale padding]
  (/ (.rangeBand x-scale)
     (- (- 1 padding) 5)))

(defn construct-line-y2
  [y-scale]
  (fn [d]
    (y-scale d)))

(defn construct-line
  [x-scale y-scale line-opts]
  (let [x1-creator (construct-line-x1 x-scale)
        y1-creator (construct-line-y1 y-scale)
        x2-creator (construct-line-x2 x-scale (:padding line-opts))
        y2-creator (construct-line-y2 y-scale)]
    (fn [[ord-data y-data num-data]]
      {:x1 x1-creator
       :y1 (y1-creator y-data)
       :x2 x2-creator
       :y2 (y2-creator y-data)})))

(defn construct-data
  [data]
  (reduce (fn [p [ord-data num-data]]
            (let [prev-y (-> p last second)
                  prev-data (-> p last last)
                  y-data (if (and (nil? prev-y)
                                  (nil? prev-data))
                           (if (pos? num-data) num-data 0)
                           (if (pos? prev-data)
                             (if (pos? num-data)
                               (+ prev-y num-data)
                               prev-y)
                             (if (pos? num-data)
                               (+ (- prev-y prev-data) num-data)
                               (- prev-y prev-data))))]
              (conj p [ord-data y-data num-data])))
          []
          data))

(defcomponent waterfall-rects
  [{:keys [g each scale style data]} owner]
  (display-name [_] "waterfall-rects")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [{:keys [x-scale y-scale]} scale
                 rect-constructor (construct-rect x-scale y-scale style)
                 text-constructor (construct-text x-scale y-scale (:text each))
                 line-constructor (construct-line x-scale y-scale (:line each))]
             (om/build-all waterfall-rect
                           (map-indexed (fn [idx d]
                                          {:rect (rect-constructor d)
                                           :text (text-constructor d)
                                           :line (line-constructor d)
                                           :react-key idx})
                                        (construct-data data))
                           {:key :react-key}))]))
