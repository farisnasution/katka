(ns katka.chart.donut-pie
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.util.data :as data]
            [katka.scale.ordinal :as osc])
  (:use-macros [katka.macro :only [defcomponent not-nil?]]))

(defn donut-pie-layout
  [{:keys [value sort-fn start-angle end-angle pad-angle]}]
  (cond-> (.pie (.-layout js/d3))
          (not-nil? value) (.value value)
          (not-nil? sort-fn) (.sort sort-fn)
          (not-nil? start-angle) (.startAngle start-angle)
          (not-nil? end-angle) (.endAngle end-angle)
          (not-nil? pad-angle) (.padAngle pad-angle)))

(defn donut-pie-constructor
  [{:keys [inner-radius outer-radius
           corner-radius pad-radius
           start-angle end-angle
           pad-angle]}]
  (cond-> (.arc (.-svg js/d3))
          (not-nil? inner-radius) (.innerRadius inner-radius)
          (not-nil? outer-radius) (.outerRadius outer-radius)
          (not-nil? corner-radius) (.cornerRadius corner-radius)
          (not-nil? pad-radius) (.padRadius pad-radius)
          (not-nil? start-angle) (.startAngle start-angle)
          (not-nil? end-angle) (.endAngle end-angle)
          (not-nil? pad-angle) (.padAngle pad-angle)))

(defcomponent slice-of-donut-pie
  [{:keys [path text]} owner]
  (display-name [_] "slice-of-donut-pie")
  (render [_]
          [:g {}
           (om/build shape/path path)
           (when (true? (:show-text? text))
             [:g {:transform (data/translate (:g text))}
              (om/build shape/text text)])]))

(defn get-ordinal-data
  [d]
  (first (.-data d)))

(defn construct-path
  [path-opts color-fn donut-pie-fn]
  (fn [ord-data num-data]
    (into {:fill (color-fn ord-data)
           :d (donut-pie-fn num-data)}
          path-opts)))

(defn construct-text
  [text-opts donut-pie-fn]
  (fn [ord-data num-data]
    (let [centroid (.centroid donut-pie-fn num-data)
          g {:pos-x (first centroid)
             :pos-y (second centroid)}]
      (into {:g g
             :content ord-data
             :show-text? true
             :text-anchor "middle"}
            text-opts))))

(defcomponent donuts-pies
  [{:keys [each g style scale data]} owner]
  (display-name [_] "donuts-pies")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [{:keys [outer-r inner-r]} scale
                 {:keys [colors]} style
                 donut-pie-factory (donut-pie-layout {:value (fn [d] (second d))
                                                       :sort-fn nil})
                 donut-pie-fn (donut-pie-constructor {:outer-radius outer-r
                                                       :inner-radius inner-r})
                 ord-data (map first data)
                 color-fn (osc/ordinal-scale {:domain ord-data
                                              :range-scale colors})
                 path-constructor (construct-path (:path each)
                                                  color-fn
                                                  donut-pie-fn)
                 text-constructor (construct-text (:text each) donut-pie-fn)]
             (om/build-all slice-of-donut-pie
                           (->> (apply array data)
                                donut-pie-factory
                                (map-indexed (fn [idx data]
                                               (let [ord-d (get-ordinal-data data)]
                                                 {:path (path-constructor ord-d
                                                                          data)
                                                  :text (text-constructor ord-d
                                                                          data)
                                                  :react-key idx}))))
                           {:key :react-key}))]))

(defn construct-scale-opts
  [{:keys [width height]} inner-r]
  {:outer-r (/ (min width height) 2)
   :inner-r (if (pos? inner-r)
              inner-r
              0)})

(defn construct-g-opts
  [{:keys [width height]}]
  {:pos-x (/ width 2)
   :pos-y (/ height 2)})

(defcomponent donut-pie-chart
  [{:keys [svg text path style retriever-ks data]} owner]
  (display-name [_] (if (pos? (:inner-r style))
                      "donut-chart"
                      "pie-chart"))
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [ord-ks num-ks]} retriever-ks
                {:keys [colors inner-r]} style
                margin {:left  40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                radius (/ (min (:width container) (:height container)) 2)
                new-data (data/format-data data ord-ks num-ks)]
            [:svg {:width width
                   :height height}
             (om/build donuts-pies {:each {:text text
                                           :path path}
                                    :g (construct-g-opts container)
                                    :style {:colors colors}
                                    :scale (construct-scale-opts container
                                                                 inner-r)
                                    :data new-data})])))
