(ns katka.chart.axis
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.scale.ordinal :as osc]
            [katka.scale.linear :as lsc]
            [katka.util.math :as math]
            [katka.util.data :as data])
  (:use-macros [katka.macro :only [defcomponent]]))

(defcomponent axis-element
  "Creates a React axis element.

   Accept:

   {:line {:x1 <num>
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
    :g {:pos-x <num>
        :pos-y <num>}}"
  [{:keys [line text g]} owner]
  (display-name [_] "axis-element")
  (render [_]
          [:g {:transform (data/translate g)}
             (om/build shape/line (select-keys line
                                               [:x1 :y1 :x2 :y2 :stroke]))
           (when (true? (:show-text? text))
             (om/build shape/text (select-keys text
                                               [:x :y
                                                :dx :dy
                                                :text-anchor :content])))]))

(defcomponent ordinal-x-axis
  "Creates a React x-axis Resetelement.

   Accept:

   {:each {:line {:x1 <num>
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
   :g {:pos-x <num>
       :pos-y <num>}
   :scale {:width <num>
           :padding <num>}
   :line-axis  {:show-line? <bool>
                :stroke <string>}
   :data <vector of map>}"
  [{:keys [each g scale line-axis data]} owner]
  (display-name [_] "ordinal-x-axis")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [t (select-keys (:text each) [:x :y :dx :dy
                                              :text-anchor :show-text?])
                 l (select-keys (:line each) [:x1 :y1 :x2 :y2 :stroke])
                 {:keys [width padding]} scale
                 ord-data (map first data)
                 width-fn (osc/ordinal-scale {:domain ord-data
                                              :range-scale [0 width]
                                              :padding padding})]
             (om/build-all axis-element
                           (map-indexed (fn [idx d]
                                          {:line l
                                           :text (assoc t :content d)
                                           :g {:pos-x (+ (width-fn d)
                                                         (/ (.rangeBand width-fn)
                                                            2))}
                                           :react-key idx})
                                        ord-data)
                           {:key :react-key}))
           (let [{:keys [show-line? stroke]} line-axis]
             (when (true? show-line?)
               (om/build shape/line {:x2 (:width scale)
                                     :stroke stroke})))]))

(defcomponent ordinal-y-axis
  "Creates a React x-axis element.

   Accept:

   {:each {:line {:x1 <num>
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
   :g {:pos-x <num>
       :pos-y <num>}
   :scale {:height <num>
           :padding <num>}
   :line-axis  {:show-line? <bool>
                :stroke <string>}
   :data <vector of map>}"
  [{:keys [each g scale line-axis data]} owner]
  (display-name [_] "ordinal-y-axis")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [t (select-keys (:text each {}) [:x :y :dx :dy
                                                 :text-anchor :show-text?])
                 l (select-keys (:line each {}) [:x1 :y1 :x2 :y2 :stroke])
                 {:keys [height padding]} scale
                 ord-data (map first data)
                 height-fn (osc/ordinal-scale {:domain ord-data
                                               :range-scale [0 height]
                                               :padding padding})]
             (om/build-all axis-element
                           (map-indexed (fn [idx d]
                                          {:line l
                                           :text (assoc t :content d)
                                           :g {:pos-x (+ (height-fn d)
                                                         (/ (.rangeBand height-fn)
                                                            2))}
                                           :react-key idx})
                                        ord-data)
                           {:key :react-key}))
           (let [{:keys [show-line? stroke]} line-axis]
             (when (true? show-line?)
               (om/build shape/line {:x2 (:height scale)
                                     :stroke stroke})))]))

(defcomponent numerical-y-axis
  "Creates a React y-axis element.

   Accept:

   {:each {:line {:x1 <num>
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
    :g {:pos-x <num>
        :pos-y <num>}
    :scale {:height <num>
            :rbd <num>}
    :line-axis  {:show-line? <bool>
                 :stroke <string>}
    :data <vector of map>}"
  [{:keys [each g scale line-axis data]} owner]
  (display-name [_] "numerical-y-axis")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [t (select-keys (:text each) [:x :y :dx :dy
                                              :text-anchor :show-text?])
                 l (select-keys (:line each) [:x1 :y1 :x2 :y2 :stroke])
                 {:keys [height rbd]} scale
                 num-data (map last data)
                 min-data (let [d (apply min num-data)]
                            (if (neg? d) d 0))
                 max-data (apply max num-data)
                 height-fn (lsc/linear-scale {:domain [min-data max-data]
                                              :range-scale [0 height]})
                 z-bottom (height-fn 0)
                 z-top (- height z-bottom)
                 n-top (-> max-data (/ rbd) math/floor)
                 n-bottom (-> min-data
                              math/->pos
                              (/ rbd)
                              math/floor
                              math/->neg)
                 middle-data (->> (+ n-top 1)
                                  (range n-bottom)
                                  (map #(* % rbd))
                                  vec)
                 new-data (concat [min-data] middle-data [max-data])]
             (om/build-all axis-element
                           (map-indexed (fn [idx d]
                                          {:line l
                                           :text (assoc t :content d)
                                           :g {:pos-y (if (neg? d)
                                                        (+ z-top
                                                           (-> (height-fn d)
                                                               (- z-bottom)
                                                               math/->pos))
                                                        (- z-top
                                                           (-> (height-fn d)
                                                               (- z-bottom))))}
                                           :react-key idx})
                                        new-data)
                           {:key :react-key}))
           (let [{:keys [show-line? stroke]} line-axis]
             (when (true? show-line?)
               (om/build shape/line {:y2 (:height scale)
                                     :stroke stroke})))]))

(defcomponent numerical-x-axis
  "Creates a React x-axis element.

   Accept:

   {:each {:line {:x1 <num>
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
    :g {:pos-x <num>
        :pos-y <num>}
    :scale {:width <num>
            :rbd <num>}
    :line-axis  {:show-line? <bool>
                 :stroke <string>}
    :data <vector of map>}"
  [{:keys [each g scale line-axis data]} owner]
  (display-name [_] "numerical-x-axis")
  (render [_]
          [:g {:transform (data/translate g)}
           (let [t (select-keys (:text each) [:x :y :dx :dy
                                              :text-anchor :show-text?])
                 l (select-keys (:line each) [:x1 :y1 :x2 :y2 :stroke])
                 {:keys [width rbd]} scale
                 num-data (map first data)
                 min-data (let [d (apply min num-data)]
                            (if (neg? d) d 0))
                 max-data (apply max num-data)
                 width-fn (lsc/linear-scale {:domain [min-data max-data]
                                             :range-scale [0 width]})
                 n-top (-> max-data (/ rbd) math/floor)
                 n-bottom (-> min-data
                              math/->pos
                              (/ rbd)
                              math/floor
                              math/->neg)
                 middle-data (->> (+ n-top 1)
                                  (range n-bottom)
                                  (map #(* % rbd))
                                  vec)
                 new-data (concat [min-data] middle-data [max-data])]
             (om/build-all axis-element
                           (map-indexed (fn [idx d]
                                          {:line l
                                           :text (assoc t :content d)
                                           :g {:pos-x (width-fn d)}
                                           :react-key idx})
                                        new-data)
                           {:key :react-key}))
           (let [{:keys [show-line? stroke]} line-axis]
             (when (true? show-line?)
               (om/build shape/line {:x2 (:width scale)
                                     :stroke stroke})))]))
