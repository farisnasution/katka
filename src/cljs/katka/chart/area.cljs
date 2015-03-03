(ns katka.chart.area
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as axis]
            [katka.util.data :as data]
            [katka.util.math :as math]
            [katka.scale.linear :as lsc])
  (:use-macros [katka.macro :only [defcomponent not-nil?]]))

(defn -area-constructor
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

(defcomponent single-area
  [{:keys [g scale path data]} owner]
  (display-name [_] "single-area")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [{:keys [width height]} scale
                 p (select-keys path [:stroke :stroke-width :fill])
                 [x-num-data y-num-data] (data/separate-data data)
                 y-min-data (math/min-value y-num-data)
                 y-max-data (math/max-value y-num-data)
                 x-min-data (math/min-value x-num-data)
                 x-max-data (math/max-value x-num-data)
                 height-fn (lsc/linear-scale {:domain [y-min-data y-max-data]
                                              :range-scale [height 0]})
                 width-fn (lsc/linear-scale {:domain [x-min-data x-max-data]
                                             :range-scale [0 width]})
                 area-fn (-area-constructor {:x #(width-fn (first %))
                                             :y0 height
                                             :y1 #(height-fn (last %))})]
             (om/build shape/path (assoc p :d (area-fn (apply array data)))))]))

(defcomponent area-chart
  [{:keys [svg area x-axis y-axis retriever-ks data]} owner]
  (display-name [_] "area-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [x-ks y-ks]} retriever-ks
                margin {:left  40
                        :right 40
                        :top 30
                        :bottom 30}
                inner-size {:width (- width
                                      (:left margin)
                                      (:right margin))
                            :height (- height
                                       (:top margin)
                                       (:bottom margin))}
                new-data (data/format-data data x-ks y-ks)]
            [:svg {:width width
                   :height height}
             (om/build single-area {:g {:pos-x (:left margin)
                                        :pos-y (:top margin)}
                                    :scale {:width (:width inner-size)
                                            :height (:height inner-size)}
                                    :path (into {:fill "steelblue"}
                                                area)
                                    :data new-data}
                       {:react-key "single-path"})
             (let [{:keys [orient line-axis each rbd]} x-axis
                   {:keys [text line]} each]
               (om/build axis/numerical-x-axis
                         {:scale {:width (:width inner-size)
                                  :rbd (if (number? rbd) rbd 20)}
                          :g {:pos-x (:left margin)
                              :pos-y (if (= orient "top")
                                       (:top margin)
                                       (- height
                                          (:bottom margin)))}
                          :each {:text (into {:show-text? true
                                              :dy (if (= orient "top")
                                                    "-1.4em"
                                                    "1.4em")
                                              :text-anchor "middle"}
                                             text)
                                 :line (into {:y2 (if (= orient "top")
                                                    "-0.5em"
                                                    "0.5em")
                                              :stroke "black"}
                                             line)}
                          :line-axis (into {:show-line? true
                                            :stroke "black"}
                                           line-axis)
                          :data new-data}
                         {:react-key "numerical-x-axis"}))
             (let [{:keys [orient line-axis each rbd]} y-axis
                   {:keys [text line]} each]
               (om/build axis/numerical-y-axis
                         {:scale {:height (:height inner-size)
                                  :rbd (if (number? rbd) rbd 20)}
                          :g {:pos-x (if (= orient "right")
                                       (- width
                                          (:right margin))
                                       (:left margin))
                              :pos-y (:top margin)}
                          :each {:text (into {:show-text? true
                                              :text-anchor (if (= orient "right")
                                                             "start"
                                                             "end")
                                              :dy "0.32em"
                                              :x (if (= orient "right")
                                                   9
                                                   -9)}
                                             text)
                                 :line (into {:x2 (if (= orient "right")
                                                    6
                                                    -6)
                                              :stroke "black"}
                                             line)}
                          :line-axis (into {:show-line? true
                                            :stroke "black"}
                                           line-axis)
                          :data new-data}
                         {:react-key "numerical-y-axis"}))])))
