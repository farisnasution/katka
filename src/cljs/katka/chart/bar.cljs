(ns katka.chart.bar
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as ax]
            [katka.scale.linear :as lsc]
            [katka.scale.ordinal :as osc]
            [katka.util.math :as math]
            [katka.util.data :as data])
  (:use-macros [katka.macro :only [defcomponent]]))

(defn construct-width
  [scale rect-type]
  (if (= rect-type "horizontal")
    (fn [[_ d]]
      (-> (scale d)
          (- (scale 0))
          math/->pos))
    (fn [_] (.rangeBand scale))))

(defn construct-height
  [scale rect-type]
  (if (= rect-type "horizontal")
    (fn [_] (.rangeBand scale))
    (fn [[_ d]]
      (-> (scale d)
          (- (scale 0))
          math/->pos))))

(defn construct-x
  [scale rect-type]
  (if (= rect-type "horizontal")
    (fn [[_ d]]
      (if (pos? d)
        (scale 0)
        (scale d)))
    (fn [[d _]] (scale d))))

(defn construct-y
  [scale rect-type]
  (if (= rect-type "horizontal")
    (fn [[d _]] (scale d))
    (fn [[_ d]]
      (if (neg? d)
        (scale 0)
        (scale d)))))

(defn construct-style
  [style-opts]
  (into {:fill "steelblue"}
        style-opts))

(defcomponent rects
  [{:keys [g scale style rect-type data]} owner]
  (display-name [_] (str rect-type "-rects"))
  (render [_]
          [:g {:transform (data/translate g)}
           (let [{:keys [x-scale y-scale]} scale
                 {:keys [fill stroke]} (construct-style style)
                 width-creator (construct-width x-scale rect-type)
                 height-creator (construct-height y-scale rect-type)
                 x-creator (construct-x x-scale rect-type)
                 y-creator (construct-y y-scale rect-type)]
             (om/build-all shape/rect
                           (map-indexed (fn [idx d]
                                          {:width (width-creator d)
                                           :height (height-creator d)
                                           :x (x-creator d)
                                           :y (y-creator d)
                                           :fill fill
                                           :stroke stroke
                                           :react-key idx})
                                        data)
                           {:key :react-key}))]))

(defn construct-scale
  [rect-type min-data max-data ord-data padding {:keys [width height]}]
  (if (= "horizontal" rect-type)
    {:x-scale (lsc/linear-scale {:domain [min-data max-data]
                                 :range-scale [0 width]})
     :y-scale (osc/ordinal-scale {:domain ord-data
                                  :range-bands [[0 height] padding]})}
    {:x-scale (osc/ordinal-scale {:domain ord-data
                                  :range-bands [[0 width] padding]})
     :y-scale (lsc/linear-scale {:domain [min-data max-data]
                                 :range-scale [height 0]}) }))

(defcomponent bar-chart
  [{:keys [svg rect-type rect x-axis y-axis retriever-ks data]} owner]
  (display-name [_] (str rect-type "-bar-chart"))
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [padding fill stroke]} rect
                {:keys [ord-ks num-ks]} retriever-ks
                margin {:left  40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                new-data (data/format-data data ord-ks num-ks)
                [ord-data num-data] (data/separate-data new-data)
                min-data (math/min-value num-data)
                max-data (math/max-value num-data)
                {:keys [x-scale y-scale]} (construct-scale rect-type
                                                           min-data
                                                           max-data
                                                           ord-data
                                                           padding
                                                           container)]
            [:svg {:width width
                   :height height}
             (om/build rects
                       {:g {:pos-x (:left margin)
                            :pos-y (:top margin)}
                        :scale {:x-scale x-scale
                                :y-scale y-scale}
                        :style {:fill fill
                                :stroke stroke}
                        :rect-type rect-type
                        :data new-data}
                       {:react-key "rects"})
             (let [{:keys [orient line-axis end-text each ticks]} x-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type (if (= rect-type "horizontal")
                                                "numerical"
                                                "ordinal")
                                  :scale-fn x-scale
                                  :scale-ticks ticks}
                          :end-text end-text
                          :line-axis line-axis
                          :data (if (= rect-type "horizontal")
                                  {:min-data min-data
                                   :max-data max-data
                                   :all-data num-data}
                                  {:all-data ord-data})}
                         {:react-key "x-axis"}))
             (let [{:keys [orient line-axis end-text each ticks]} y-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type (if (= rect-type "horizontal")
                                                "ordinal"
                                                "numerical")
                                  :scale-fn y-scale
                                  :scale-ticks ticks}
                          :end-text end-text
                          :line-axis line-axis
                          :data (if (= rect-type "horizontal")
                                  {:all-data ord-data}
                                  {:min-data min-data
                                   :max-data max-data
                                   :all-data num-data})}
                         {:react-key "y-axis"}))])))
