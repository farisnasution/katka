(ns katka.chart.axis
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.util.math :as math]
            [katka.util.data :as data])
  (:use-macros [katka.macro :only [defcomponent]]))

(defcomponent axis-element
  [{:keys [line text g]} owner]
  (display-name [_] "axis-element")
  (render [_]
          [:g {:transform (data/translate g)}
           (om/build shape/line line)
           (when (true? (:show-text? text))
             (om/build shape/text text))]))

(defn ordinal-g
  [scale orient]
  (fn [d]
    (let [value (+ (scale d)
                   (/ (.rangeBand scale) 2))]
      (if (or (= orient "top")
              (= orient "bottom"))
        {:pos-x value}
        {:pos-y value}))))

(defn numerical-data
  ([min-data max-data ticks]
   (let [new-ticks (if (and (number? ticks)
                            (pos? ticks))
                     ticks
                     10)
         n-bottom (-> min-data
                      math/->pos
                      (/ new-ticks)
                      math/floor
                      math/->neg)
         n-top (-> max-data (/ new-ticks) math/floor)
         middle-data (->> (+ n-top 1)
                          (range n-bottom)
                          (map #(* % new-ticks)))]
     (concat [min-data] middle-data [max-data])))
  ([min-data max-data]
   (numerical-data min-data max-data 10)))

(defn numerical-g
  [scale orient]
  (if (or (= orient "top")
          (= orient "bottom"))
    (fn [d]
      {:pos-x (scale d)})
    (fn [d]
      {:pos-y (scale d)})))

(defn construct-g
  [scale-type scale-fn orient]
  (if (= scale-type "ordinal")
    (ordinal-g scale-fn orient)
    (numerical-g scale-fn orient)))

(defn construct-line
  [line-opts orient]
  (merge (condp = orient
           "top" {:y2 "-0.5em"}
           "bottom" {:y2 "0.5em"}
           "left" {:x2 "-6"}
           "right" {:x2 "6"}
           {:x2 "-6"})
         {:stroke "black"}
         line-opts))

(defn construct-text
  [text-opts orient]
  (fn [d]
    (merge {:show-text? true
            :content d}
           (condp = orient
             "top" {:dy "-1.4em"
                    :text-anchor "middle"}
             "bottom" {:dy "1.4em"
                       :text-anchor "middle"}
             "left" {:dx "-1.4em"
                     :text-anchor "end"}
             "right" {:dx "1.4em"
                      :text-anchor "start"}
             {:dx "-1.4em"
              :text-anchor "end"})
           text-opts)))

(defn get-axis-data
  [scale-type scale-ticks {:keys [min-data max-data all-data]}]
  (if (= scale-type "ordinal")
    all-data
    (numerical-data min-data max-data scale-ticks)))

(defn construct-end-text
  [end-text-opts orient]
  (merge (condp = orient
           "top" {:x -6
                  :dx "-0.71em"
                  :text-anchor "middle"}
           "bottom" {:x 6
                     :dx "0.71em"
                     :text-anchor "middle"}
           "left"  {:transform "rotate(-90)"
                    :y 6
                    :dy "0.71em"
                    :text-anchor "end"}
           "right" {:transform "rotate(-90)"
                    :y -6
                    :dy "-0.71em"
                    :text-anchor "end"}
           {:transform "rotate(-90)"
            :y 6
            :dy "0.71em"
            :text-anchor "end"})
         {:show-text? true}
         end-text-opts))

(defn construct-outer-g
  [{:keys [size margin]} orient]
  (condp = orient
    "top" {:pos-x (:left margin)
           :pos-y (:top margin)}
    "bottom" {:pos-x (:left margin)
              :pos-y (- (:height size)
                        (:bottom margin))}
    "left" {:pos-x (:left margin)
            :pos-y (:top margin)}
    "right" {:pos-x (- (:width size)
                       (:right margin))
             :pos-y (:top margin)}
    {:pos-x (:left margin)
     :pos-y (:top margin)}))

(defn construct-line-axis
  [line-axis {:keys [size margin]} orient]
  (merge {:stroke "black"
          :show-line? true}
         (if (or (= orient "top")
                 (= orient "bottom"))
           {:x2 (- (:width size)
                   (:left margin)
                   (:right margin))}
           {:y2 (- (:height size)
                   (:top margin)
                   (:bottom margin))})
         line-axis))

(defcomponent axis
  [{:keys [outer-container each orient scale end-text line-axis data]} owner]
  (display-name [_] (str orient "-" (:scale-type scale) "-axis"))
  (render [_]
          [:g {:transform (data/translate (construct-outer-g outer-container
                                                             orient))}
           (let [{:keys [scale-type scale-fn scale-ticks]} scale
                 text-creator (construct-text (:text each) orient)
                 line-opts (construct-line (:line each) orient)
                 g-creator (construct-g scale-type scale-fn orient)]
             (om/build-all axis-element
                           (->> data
                                (get-axis-data scale-type scale-ticks)
                                (map-indexed (fn [idx d]
                                               {:line line-opts
                                                :text (text-creator d)
                                                :g (g-creator d)
                                                :react-key idx})))
                           {:key :react-key}))
           (let [end-text-opts (construct-end-text end-text orient)]
             (when (true? (:show-text? end-text-opts))
               (om/build shape/text end-text-opts)))
           (let [line-axis-opts (construct-line-axis line-axis
                                                     (:size outer-container)
                                                     orient)]
             (when (true? (:show-line? line-axis-opts))
               (om/build shape/line (construct-line-axis line-axis
                                                         outer-container
                                                         orient))))]))
