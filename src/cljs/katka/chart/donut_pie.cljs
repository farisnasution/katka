(ns katka.chart.donut-pie
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.util.data :as data]
            [katka.scale.ordinal :as osc]
            [katka.util.dev :as dev])
  (:use-macros [katka.macro :only [defcomponent not-nil?]]))

(defn -donut-pie-layout
  [{:keys [value sort-fn start-angle end-angle pad-angle]}]
  (cond-> (.pie (.-layout js/d3))
          (not-nil? value) (.value value)
          (not-nil? sort-fn) (.sort sort-fn)
          (not-nil? start-angle) (.startAngle start-angle)
          (not-nil? end-angle) (.endAngle end-angle)
          (not-nil? pad-angle) (.padAngle pad-angle)))

;; (def q (-donut-pie-layout {:value (fn [d]
;;                                     (last d))
;;                            :sort-fn nil}))

(defn -donut-pie-constructor
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
           (om/build shape/path (select-keys path [:stroke :stroke-width
                                                   :fill :d]))
           (when (true? (:show-text? text))
             [:g {:transform (data/translate (:g text))}
              (om/build shape/text (select-keys text [:x :y
                                                      :dx :dy
                                                      :text-anchor :content]))])]))

(defcomponent donuts-pies
  [{:keys [each g style scale data]} owner]
  (display-name [_] "donuts-pies")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [t (select-keys (:text each) [:x :y :dx :dy
                                              :text-anchor :show-text?])
                 p (select-keys (:path each) [:stroke :stroke-width])
                 {:keys [outer-r inner-r]} scale
                 {:keys [colors]} style
                 donut-pie-factory (-donut-pie-layout {:value (fn [d] (last d))
                                                       :sort-fn nil})
                 donut-pie-fn (-donut-pie-constructor {:outer-radius outer-r
                                                       :inner-radius inner-r})
                 ord-data (map first data)
                 color-fn (osc/ordinal-scale {:domain ord-data
                                              :range-scale colors})]
             (om/build-all slice-of-donut-pie
                           (map-indexed (fn [idx d]
                                          (let [ord-d (first (.-data d))]
                                            {:path (into p {:fill (color-fn ord-d)
                                                            :d (donut-pie-fn d)})
                                             :text (let [centroid (.centroid donut-pie-fn d)
                                                         computed-g {:pos-x (first centroid)
                                                                     :pos-y (last centroid)}
                                                         new-t (into {:g computed-g} t)]
                                                     (assoc new-t :content ord-d))
                                             :react-key idx}))
                                        (donut-pie-factory (apply array data)))
                           {:key :react-key}))]))

(defcomponent donut-pie-chart
  [{:keys [svg dp style retriever-ks data]} owner]
  (display-name [_] "donut-pie-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [ord-ks num-ks]} retriever-ks
                {:keys [colors inner-r]} style
                {:keys [text path]} dp
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
                radius (/ (min (:width inner-size) (:height inner-size)) 2)
                new-data (data/format-data data ord-ks num-ks)]
            [:svg {:width width
                   :height height}
             (om/build donuts-pies {:each {:text (into {:show-text? true
                                                        :text-anchor "middle"}
                                                       text)
                                           :path path}
                                    :g {:pos-x (/ width 2)
                                        :pos-y (/ height 2)}
                                    :style {:colors colors}
                                    :scale {:outer-r radius
                                            :inner-r inner-r}
                                    :data new-data})])))
