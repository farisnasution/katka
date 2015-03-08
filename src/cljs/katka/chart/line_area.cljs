(ns katka.chart.line-area
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as ax]
            [katka.util.data :as data]
            [katka.util.math :as math]
            [katka.scale.linear :as lsc])
  (:use-macros [katka.macro :only [defcomponent not-nil?]]))

(defn area-constructor
  [{:keys [x y
           x0 x1
           y0 y1
           interpolate tension
           defined]}]
  (cond-> (.area (.-svg js/d3))
          (not-nil? x) (.x x)
          (not-nil? y) (.y y)
          (not-nil? x0) (.x0 x0)
          (not-nil? x1) (.x1 x1)
          (not-nil? y0) (.y0 y0)
          (not-nil? y1) (.y1 y1)
          (not-nil? interpolate) (.interpolate interpolate)
          (not-nil? tension) (.tension tension)
          (not-nil? defined) (.defined defined)))

(defn line-constructor
  [{:keys [interpolation x y
           tension defined radius
           angle]}]
  (cond-> (.line (.-svg js/d3))
          (not-nil? interpolation) (.interpolate interpolation)
          (not-nil? x) (.x x)
          (not-nil? y) (.y y)
          (not-nil? tension) (.tension tension)
          (not-nil? defined) (.defined defined)))

(defn construct-path
  [path-opts constructor data]
  (assoc path-opts :d (constructor (apply array data))))

(defcomponent single-line-area
  [{:keys [g constructor path data]} owner]
  (display-name [_] "single-line")
  (render [_]
          [:g {:transform (data/translate g)}
           (om/build shape/path (construct-path path
                                                constructor
                                                data))]))

(defn construct-path-opts
  [path-opts]
  (into {:fill "steelblue"}
        path-opts))

(defcomponent area-chart
  [{:keys [svg area x-axis y-axis retriever-ks data]} owner]
  (display-name [_] "area-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [x-ks y-ks]} retriever-ks
                [y-max-ks y-min-ks] y-ks
                margin {:left  40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                new-data (data/format-data data x-ks max-y-ks min-y-ks)
                [x-num-data & y-num-data] (data/separate-data new-data)
                x-min-data (math/min-value x-num-data)
                x-max-data (math/max-value x-num-data)
                concated-y-num-data (if (nil? y-ks)
                                      (first y-num-data)
                                      (apply concat y-num-data))
                y-min-data (math/min-value concated-y-num-data)
                y-max-data (math/max-value concated-y-num-data)
                x-scale (lsc/linear-scale {:domain [x-min-data x-max-data]
                                           :range-scale [0 width]})
                y-scale (lsc/linear-scale {:domain [y-min-data y-max-data]
                                           :range-scale [height 0]})
                area-fn (-area-constructor {:x #(x-scale (first %))
                                            :y0 (if (nil? y-min-ks)
                                                  height
                                                  #(y-scale (last %)))
                                            :y1 #(y-scale (second %))})]
            [:svg {:width width
                   :height height}
             (om/build single-line-area
                       {:g {:pos-x (:left margin)
                            :pos-y (:top margin)}
                        :constructor area-fn
                        :path (construct-path-opts area)
                        :data new-data}
                       {:react-key "single-line-area"})
             (let [{:keys [orient line-axis end-text each]} x-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type "numerical"
                                  :scale-fn x-scale}
                          :end-text end-text
                          :line-axis line-axis
                          :data  {:min-data x-min-data
                                  :max-data x-max-data
                                  :all-data x-num-data}}
                         {:react-key "x-axis"}))
             (let [{:keys [orient line-axis end-text each ticks]} y-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type "numerical"
                                  :scale-fn y-scale
                                  :scale-ticks ticks}
                          :end-text end-text
                          :line-axis line-axis
                          :data  {:min-data y-min-data
                                  :max-data y-max-data
                                  :all-data concated-y-num-data}}
                         {:react-key "y-axis"}))])))

(defcomponent line-chart
  [{:keys [svg area x-axis y-axis retriever-ks data]} owner]
  (display-name [_] "line-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [x-ks y-ks]} retriever-ks
                margin {:left  40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                new-data (apply data/format-data data x-ks y-ks)
                [x-num-data & y-num-data] (data/separate-data new-data)
                x-min-data (math/min-value x-num-data)
                x-max-data (math/max-value x-num-data)
                concated-y-num-data (apply concat y-num-data)
                y-min-data (math/min-value concated-y-num-data)
                y-max-data (math/max-value concated-y-num-data)
                x-scale (lsc/linear-scale {:domain [x-min-data x-max-data]
                                           :range-scale [0 width]})
                y-scale (lsc/linear-scale {:domain [y-min-data y-max-data]
                                           :range-scale [height 0]})
                line-fn (-line-constructor {:x #(x-scale (first %))
                                            :y #(y-scale (second %))})]
            [:svg {:width width
                   :height height}
             (om/build-all single-line-area
                           (map-indexed (fn [idx ds]
                                          {:g {:pos-x (:left margin)
                                               :pos-y (:top margin)}
                                           :constructor line-fn
                                           :path (construct-path-opts area)
                                           :data (map vec x-num-data ds)
                                           :react-key idx})
                                        y-num-data)
                           {:key :react-key})
             (let [{:keys [orient line-axis end-text each]} x-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type "numerical"
                                  :scale-fn x-scale}
                          :end-text end-text
                          :line-axis line-axis
                          :data  {:min-data x-min-data
                                  :max-data x-max-data
                                  :all-data x-num-data}}
                         {:react-key "x-axis"}))
             (let [{:keys [orient line-axis end-text each ticks]} y-axis]
               (om/build ax/axis
                         {:outer-container {:size svg
                                            :margin margin}
                          :each each
                          :orient orient
                          :scale {:scale-type "numerical"
                                  :scale-fn y-scale
                                  :scale-ticks ticks}
                          :end-text end-text
                          :line-axis line-axis
                          :data  {:min-data y-min-data
                                  :max-data y-max-data
                                  :all-data concated-y-num-data}}
                         {:react-key "y-axis"}))])))
