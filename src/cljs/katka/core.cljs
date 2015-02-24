(ns katka.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.chart.bar :as b]
            [katka.util.query :as q]
            [katka.util.scale :as s]
            [katka.util.dev :as d]
            [katka.chart.shape :as shape]))

;; (defonce data (map (fn [r]
;;                      {:name (str "bah " r)
;;                       :age (rand 100)}) (range 10)))

(def data [{:name "faris"
            :age 37}
           {:name "farras"
            :age 30}
           {:name "lukman"
            :age 25}
           {:name "mirhady"
            :age 10}
           {:name "herdito"
            :age 40}
           {:name "kewer"
            :age -50}
           {:name "dana"
            :age 23}
           {:name "ican"
            :age 50}
           {:name "adiva"
            :age 24}
           {:name "foo"
            :age 34}
           {:name "bar"
            :age 97}
           {:name "baz"
            :age 54}])

(def state (atom {:data data
                  :svg {:width 960
                        :height 500}
                  :rects {:padding 0.1
                          :fill "steelblue"}
                  :x-axis {:orient "top"}
                  :y-axis {:orient "right"}}))

;; (defn bah
;;   [data owner]
;;   (reify
;;     om/IRender
;;     (render [_]
;;       (html [:svg {:width 960
;;                    :height 500}
;;              (om/build shape/line {:x1 10
;;                                    :y1 10
;;                                    :x2 100
;;                                    :y2 50
;;                                    :stroke "steelblue"})
;;              ]))))

;; (om/root bah state {:target (q/get-el-by-id "for-navbar")})

(om/root b/vertical-bar-chart state {:target (q/get-el-by-id "for-navbar")})

;; ===================================================================

;; (def state (atom {:scale-attrs {:padding 0.1
;;                                 :container-width 800
;;                                 :container-height 400}
;;                   :retriever-ks {:ord-ks [:name]
;;                                  :num-ks [:score]}
;;                   :data (map (fn [k]
;;                                {:name (str "bah" k)
;;                                 :score (rand 1000)}) (range 799))}))

;; (defn chart
;;   [{:keys [scale-attrs retriever-ks style-attrs data]} owner]
;;   (reify
;;     om/IRender
;;     (render [_]
;;       (html [:svg {:height (:container-height scale-attrs)
;;                    :width (:container-width scale-attrs)}
;;              (om/build b/vertical-rects {:scale-attrs scale-attrs
;;                                          :retriever-ks retriever-ks
;;                                          :style-attrs style-attrs
;;                                          :data data})]))))

;; (om/root chart state {:target (q/get-el-by-id "for-navbar")})

;; (util/log (q "b"))

;; ===================================================================

;; ini buat pr keamanan informasi

;; (defn write-file
;;   [file-name content]
;;   (loop [x (reduce (fn [p n]
;;                      (str p n "\n")) "" content)]
;;     (spit file-name x)))

;; (defn allmx
;;   [file-name]
;;   (->> (split (slurp file-name) #"\n")
;;        (map (fn [x]
;;               (split x #"\s")))
;;        (map #(last %))
;;        (set)))

;; (def allmxdetik
;;   (allmx "allmxdetik.txt"))

;; (write-file "processed/allmxdetik.txt" (reduce (fn [p n]
;;                                                  (str p n "\n")) "" allmxdetik))

;; (def allmxitb
;;   (allmx "allmxitb.txt"))

;; (def allnsdetik
;;   (allmx "allnsdetik.txt"))

;; (def allnsitb
;;   (allmx "allnsitb.txt"))

;; (defn fourthlevelsubdomain
;;   [file-name]
;;   (->> (split (slurp file-name) #"\n")
;;        (map #(split % #"\s"))
;;        (map #(first %))
;;        (filter (fn [x]
;;                  (let [splitted (split x #"\.")
;;                        result (count splitted)]
;;                    (= result 4))))
;;        (set)))

;; (def allsubdomainitb
;;   (fourthlevelsubdomain "allsubdomainitb.txt"))
