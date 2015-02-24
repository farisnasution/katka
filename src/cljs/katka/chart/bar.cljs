(ns katka.chart.bar
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.chart.axis :as axis]
            [katka.util.scale :as scale]
            [katka.util.math :as math]
            [katka.util.data :as data]))

(defn vertical-rects
  "Creates a series of React rectangle element.

   Accept:

   {:g {:pos-x <num>
        :pos-y <num>}
    :scale {:padding <num>
            :width <num>
            :height <num>}
    :style {:fill <string>}
    :data <vector of map>}"
  [{:keys [g scale style data]} owner]
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [{:keys [padding width height]} scale
                   {:keys [fill]} style
                   [ord-data num-data] [(map first data)
                                        (map last data)]
                   height-fn (scale/simple-linear-scale num-data height)
                   width-fn (scale/simple-ordinal-scale ord-data width padding)
                   z-bottom (height-fn 0)
                   z-top (- height z-bottom)]
               (om/build-all shape/rect
                             (->> data
                                  (map-indexed (fn [idx [ord-d num-d]]
                                                 (let [h (-> (height-fn num-d)
                                                             (- z-bottom)
                                                             math/->pos)]
                                                   {:width (.rangeBand width-fn)
                                                    :height h
                                                    :x (width-fn ord-d)
                                                    :y (if (neg? num-d)
                                                         z-top
                                                         (- z-top h))
                                                    :fill fill
                                                    :react-key idx})))
                                  vec)
                             {:key :react-key}))]))))

(defn vertical-bar-chart
  "Creates a React vertical bar chart.

  Accept:

  {:svg {:width <num>
         :height <num>}
  :rects {:padding <num>
          :fill <string>}
  :x-axis {:orient <string>
           :line-axis {:show-line? <bool>
                       :stroke <string>}
           :each {:line {:x1 <num>
                         :y1 <num>
                         :x2 <num>
                         :y2 <num>
                         :stroke <string>}
                  :text {:x <num>
                         :y <num>
                         :dx <num>
                         :dy <num>
                         :text-anchor <string>
                         :content <string>}}
  :y-axis {:orient <string>
           :line-axis {:show-line? <bool>
                       :stroke <string>}
           :each {:line {:x1 <num>
                         :y1 <num>
                         :x2 <num>
                         :y2 <num>
                         :stroke <string>}
                  :text {:x <num>
                         :y <num>
                         :dx <num>
                         :dy <num>
                         :text-anchor <string>
                         :content <string>}
           :rbd <num>}
  :data <vector of map>}"
  [{:keys [svg rects x-axis y-axis retriever-ks data]} owner]
  (reify
    om/IRender
    (render [_]
      (html (let [{:keys [width height]} svg
                  {:keys [padding fill]} rects
                  {:keys [ord-ks num-ks]} retriever-ks
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
                  new-data (data/format-data data ord-ks num-ks)]
              [:svg svg
               (om/build vertical-rects
                         {:g {:pos-x (:left margin)
                              :pos-y (:top margin)}
                          :scale {:padding padding
                                  :width (:width inner-size)
                                  :height (:height inner-size)}
                          :style {:fill fill}
                          :data new-data}
                         {:react-key "vertical-rects"})
               (let [{:keys [orient line-axis each]} x-axis
                     {:keys [text line]} each]
                 (om/build axis/ordinal-x-axis
                           {:scale {:width (:width inner-size)
                                    :padding padding}
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
                           {:react-key "ordinal-x-axis"}))
               (let [{:keys [orient line-axis each rbd]} y-axis
                     {:keys [text line]} each]
                 (om/build axis/numerical-y-axis
                           {:scale {:height (:height inner-size)
                                    :rbd (if (nil? rbd) 20 rbd)}
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
