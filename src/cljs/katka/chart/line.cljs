(ns katka.chart.line
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as axis]
            [katka.util.data :as data]
            [katka.util.scale :as scale]
            [katka.util.dev :as d]))

(defn -line-constructor
  [{:keys [interpolation x-fn y-fn]}]
  (let [constructor (-> js/d3 (.-svg) (.line))
        interpolated (if (nil? interpolation)
                       constructor
                       (.interpolate constructor interpolation))
        x-ed (if (nil? x-fn)
               interpolated
               (.x interpolated x-fn))
        y-ed (if (nil? y-fn)
               x-ed
               (.y x-ed y-fn))]
    y-ed))

(defn single-path
  [{:keys [g scale style data]} owner]
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [{:keys [width height]} scale
                   s (select-keys style [:stroke :stroke-width :fill])
                   [x-num-data y-num-data] [(map first data)
                                            (map last data)]
                   height-fn (scale/simple-linear-scale y-num-data height 0)
                   width-fn (scale/simple-linear-scale x-num-data width)
                   path-fn (-line-constructor {:interpolation (:interpolation style)
                                               :x-fn #(width-fn (first %))
                                               :y-fn #(height-fn (last %))})]
               (om/build shape/path (assoc s :d (path-fn (apply array data)))))]))))

(defn line-chart
  [{:keys [svg line x-axis y-axis retriever-ks data]} owner]
  (reify
    om/IRender
    (render [_]
      (html (let [{:keys [width height]} svg
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
              [:svg (select-keys svg [:width :height])
               (om/build single-path {:g {:pos-x (:left margin)
                                          :pos-y (:top margin)}
                                      :scale {:width (:width inner-size)
                                              :height (:height inner-size)}
                                      :style (into {:stroke "black"
                                                    :fill "white"}
                                                   line)
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
                           {:react-key "numerical-y-axis"}))])))))
