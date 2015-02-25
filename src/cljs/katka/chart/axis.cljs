(ns katka.chart.axis
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.shape :as shape]
            [katka.util.scale :as scale]
            [katka.util.math :as math]
            [katka.util.data :as data]))

(defn axis-element
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
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (om/build shape/line (select-keys line
                                               [:x1 :y1 :x2 :y2 :stroke]))
             (om/build shape/text (select-keys text
                                               [:x :y :dx :dy
                                                :text-anchor
                                                :content]))]))))

(defn ordinal-x-axis
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
           :padding <num>}
   :line-axis  {:show-line? <bool>
                :stroke <string>}
   :data <vector of map>}"
  [{:keys [each g scale line-axis data]} owner]
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [t (select-keys (:text each {}) [:x :y :dx :dy
                                                   :text-anchor :show-text?])
                   l (select-keys (:line each {}) [:x1 :y1 :x2 :y2 :stroke])
                   {:keys [width padding]} scale
                   ord-data (map first data)
                   width-fn (scale/simple-ordinal-scale ord-data width padding)]
               (om/build-all axis-element
                             (->> ord-data
                                  (map-indexed (fn [idx d]
                                                 {:line l
                                                  :text (if (-> t :show-text? true?)
                                                          (assoc t :content d)
                                                          t)
                                                  :g {:pos-x (+ (width-fn d)
                                                                (/ (.rangeBand width-fn)
                                                                   2))}
                                                  :react-key idx}))
                                  vec)
                             {:key :react-key}))
             (let [{:keys [show-line? stroke]} line-axis]
               (when (true? show-line?)
                 (om/build shape/line {:x2 (:width scale)
                                       :stroke stroke})))]))))

(defn ordinal-y-axis
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
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [t (select-keys (:text each {}) [:x :y :dx :dy
                                                   :text-anchor :show-text?])
                   l (select-keys (:line each {}) [:x1 :y1 :x2 :y2 :stroke])
                   {:keys [height padding]} scale
                   ord-data (map first data)
                   height-fn (scale/simple-ordinal-scale ord-data height padding)]
               (om/build-all axis-element
                             (->> ord-data
                                  (map-indexed (fn [idx d]
                                                 {:line l
                                                  :text (if (-> t :show-text? true?)
                                                          (assoc t :content d)
                                                          t)
                                                  :g {:pos-x (+ (height-fn d)
                                                                (/ (.rangeBand height-fn)
                                                                   2))}
                                                  :react-key idx}))
                                  vec)
                             {:key :react-key}))
             (let [{:keys [show-line? stroke]} line-axis]
               (when (true? show-line?)
                 (om/build shape/line {:x2 (:height scale)
                                       :stroke stroke})))]))))

(defn numerical-y-axis
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
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [t (select-keys (:text each) [:x :y :dx :dy
                                                   :text-anchor :show-text?])
                   l (select-keys (:line each) [:x1 :y1 :x2 :y2 :stroke])
                   {:keys [height rbd]} scale
                   num-data (map last data)
                   height-fn (scale/simple-linear-scale num-data height)
                   domain (.domain height-fn)
                   min-data (first domain)
                   max-data (last domain)
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
                             (->> new-data
                                  (map-indexed (fn [idx d]
                                                 {:line l
                                                  :text (if (-> t :show-text? true?)
                                                          (assoc t :content d)
                                                          t)
                                                  :g {:pos-y (if (neg? d)
                                                               (+ z-top
                                                                  (-> (height-fn d)
                                                                      (- z-bottom)
                                                                      math/->pos))
                                                               (- z-top
                                                                  (-> (height-fn d)
                                                                      (- z-bottom))))}
                                                  :react-key idx}))
                                  vec)
                             {:key :react-key}))
             (let [{:keys [show-line? stroke]} line-axis]
               (when (true? show-line?)
                 (om/build shape/line {:y2 (:height scale)
                                       :stroke stroke})))]))))

(defn numerical-x-axis
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
  (reify
    om/IRender
    (render [_]
      (html [:g {:transform (data/translate g)}
             (let [t (select-keys (:text each) [:x :y :dx :dy
                                                   :text-anchor :show-text?])
                   l (select-keys (:line each) [:x1 :y1 :x2 :y2 :stroke])
                   {:keys [width rbd]} scale
                   num-data (map first data)
                   width-fn (scale/simple-linear-scale num-data width)
                   domain (.domain width-fn)
                   min-data (first domain)
                   max-data (last domain)
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
                             (->> new-data
                                  (map-indexed (fn [idx d]
                                                 {:line l
                                                  :text (if (-> t :show-text? true?)
                                                          (assoc t :content d)
                                                          t)
                                                  :g {:pos-x (width-fn d)}
                                                  :react-key idx}))
                                  vec)
                             {:key :react-key}))
             (let [{:keys [show-line? stroke]} line-axis]
               (when (true? show-line?)
                 (om/build shape/line {:x2 (:width scale)
                                       :stroke stroke})))]))))
