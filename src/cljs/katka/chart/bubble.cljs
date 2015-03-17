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
  (into {:dy "0.3em"
         :text-anchor "middle"
         :show-text? true}
        text-opts))

(defn construct-diameter
  [container]
  (let [width (:width container)
        height (:height container)]
    (min width height)))

(defn construct-acceptable-value
  [data]
  {:children (apply array data)})

(defcomponent bubbles
  [{:keys [diameter style text data]} owner]
  (display-name [_] "bubbles")
  (render [_]
          [:g {}
           (let [{:keys [colors padding]} style
                 ord-data (map first data)
                 color-fn (if (nil? colors)
                            (osc/ordinal-20c)
                            (osc/ordinal-scale {:domain ord-data
                                                :range-scale colors}))
                 bubble-factory (bubble-layout {:sort-fn nil
                                                :size [diameter diameter]
                                                :padding padding
                                                :value #(second %)
                                                :children #(:children %)})
                 text-opts (construct-text text)]
             (om/build-all bubble-element
                           (->> data
                                construct-acceptable-value
                                bubble-factory
                                (map-indexed (fn [idx d]
                                               {:circle {:r (.-r d)
                                                         :fill (color-fn (last d))}
                                                :text (assoc text-opts
                                                             :content
                                                             (first d))
                                                :g {:pos-x (.-x d)
                                                    :pos-y (.-y d)}
                                                :react-key idx})))
                           {:key :react-key}))]))

(defcomponent bubble-chart
  [{:keys [svg style text retriever-ks data]} owner]
  (display-name [_] "bubble-chart")
  (render [_]
          (let [{:keys [width height]} svg
                {:keys [ord-ks num-ks color-ks children-ks]} retriever-ks
                margin {:left 40
                        :right 40
                        :top 30
                        :bottom 30}
                container (data/inner-container width height margin)
                new-data (data/format-data ord-ks num-ks color-ks)]
            (om/build bubbles
                      {:diameter (construct-diameter container)
                       :style style
                       :text text
                       :data new-data}))))
