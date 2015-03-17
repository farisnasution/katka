(ns katka.chart.bubble
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.scale.ordinal :as osc]
            [katka.util.data :as data])
  (:use-macros [katka.macro :only [defcomponent not-nil?]]))

(defn -invoke-sort
  [scale value]
  (.sort scale (if (false? value) nil value)))

(defn -invoke-size
  [scale value]
  (.size scale (apply array value)))

(defn bubble-layout
  [{:keys [value sort-fn size radius padding children]}]
  (cond-> (.pack (.-layout js/d3))
          (not-nil? value) (.value value)
          (not-nil? sort-fn) (-invoke-sort sort-fn)
          (not-nil? size) (-invoke-size size)
          (not-nil? radius) (.radius radius)
          (not-nil? padding) (.padding padding)
          (not-nil? children) (.children children)))

(defcomponent bubble-element
  [{:keys [circle text g]} owner]
  (display-name [_] "bubble-element")
  (render [_]
          [:g {:transform (data/translate g)}
           (om/build shape/circle circle)
           (when (true? (:show-text? text))
             (om/build shape/text text))]))

(defn construct-text
  [text-opts]
  (fn [idx d]
    (into {:dy "0.3em"
           :text-anchor "middle"
           :show-text? (not (zero? idx))
           :content (when-not (zero? idx) (first d))}
          text-opts)))

(defn construct-diameter
  [{:keys [width height]}]
  (min width height))

(defn construct-sorting
  [sorting-type]
  (condp = sorting-type
    :none false
    :ascending-name (fn [a b]
                      (compare (first a) (first b)))
    :descending-name (fn [a b]
                       (compare (first b) (first a)))
    :ascending-value (fn [a b]
                       (compare (second a) (second b)))
    :descending-value (fn [a b]
                        (compare (second b) (second a)))
    :ascending-group (fn [a b]
                       (compare (last a) (last b)))
    :descending-group (fn [a b]
                        (compare (last b) (last a)))
    false))

(defn construct-color
  [data colors]
  (cond
   (coll? colors) (osc/ordinal-scale {:domain (map first data)
                                      :range-scale colors})
   (keyword? colors) (condp = colors
                       :c10 (osc/ordinal-10)
                       :c20 (osc/ordinal-20)
                       :c20b (osc/ordinal-20b)
                       :c20c (osc/ordinal-20c))
   :else (osc/ordinal-10)))

(defn construct-fill
  [color-fn]
  (fn [idx d]
    (if (zero? idx)
      "white"
      (color-fn (last d)))))

(defn construct-acceptable-value
  [data]
  (js-obj "children" (apply array data)))

(defcomponent bubbles
  [{:keys [g diameter style text data]} owner]
  (display-name [_] "bubbles")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [{:keys [colors padding sorting-type]} style
                 color-fn (construct-color data colors)
                 fill-creator (construct-fill color-fn)
                 bubble-factory (bubble-layout {:sort-fn (construct-sorting
                                                          sorting-type)
                                                :size [diameter diameter]
                                                :padding padding
                                                :value second})
                 text-creator (construct-text text)]
             (om/build-all bubble-element
                           (->> data
                                construct-acceptable-value
                                (.nodes bubble-factory)
                                (map-indexed (fn [idx d]
                                               {:circle {:r (.-r d)
                                                         :fill (fill-creator idx d)}
                                                :text (text-creator idx d)
                                                :g {:pos-x (.-x d)
                                                    :pos-y (.-y d)}
                                                :react-key idx})))
                           {:key :react-key}))]))

(defcomponent bubble-chart
  [{:keys [svg style text retriever-ks data]} owner]
  (display-name [_] "bubble-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [ord-ks num-ks group-ks]} retriever-ks
                margin {:left 40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                new-data (data/format-data data ord-ks num-ks group-ks)]
            [:svg {:width width
                   :height height}
             (om/build bubbles
                       {:g {:pos-x (:left margin)
                            :pos-y (:top margin)}
                        :diameter (construct-diameter container)
                        :style style
                        :text text
                        :data new-data})])))
