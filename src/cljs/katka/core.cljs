(ns katka.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.chart.bar :as b]
            [katka.chart.line :as l]
            [katka.chart.area :as a]
            [katka.chart.donut-pie :as dp]
            [katka.util.query :as q]))

(defonce ordinal-data (map (fn [r]
                             {:name r
                              :age (rand-int r)}) (range 50)))

(def bar-state (atom {:data ordinal-data
                      :svg {:width 960
                            :height 500}
                      :rects {:padding 0.1
                              :fill "steelblue"}
                      :retriever-ks {:ord-ks [:name]
                                     :num-ks [:age]}
                      :x-axis {:orient "bottom"}
                      :y-axis {:orient "left"
                               :rbd 10}}))

(om/root b/vertical-bar-chart bar-state {:target (q/get-el-by-id "bar-chart")})

(defonce numerical-data (map (fn [r]
                               {:x r
                                :y (rand-int r)}) (range 50)))

(def line-state (atom {:data numerical-data
                       :svg {:width 960
                             :height 500}
                       :x-axis {:rbd 10}
                       :y-axis {:rbd 10}
                       :retriever-ks {:x-ks [:x]
                                      :y-ks [:y]}}))

(om/root l/line-chart line-state {:target (q/get-el-by-id "line-chart")})

(def area-state (atom {:data numerical-data
                       :svg {:width 960
                             :height 500}
                       :area {:fill "steelblue"}
                       :x-axis {:rbd 10}
                       :y-axis {:rbd 10}
                       :retriever-ks {:x-ks [:x]
                                      :y-ks [:y]}}))

(om/root a/area-chart area-state {:target (q/get-el-by-id "area-chart")})

(def pie-state (atom {:data ordinal-data
                      :svg {:width 950
                            :height 500}
                      :style {:inner-r 0
                              :colors ["steelblue"
                                       "green"
                                       "yellow"
                                       "orange"
                                       "pink"
                                       "brown"]}
                      :retriever-ks {:ord-ks [:name]
                                     :num-ks [:age]}}))

(om/root dp/donut-pie-chart pie-state {:target (q/get-el-by-id "pie-chart")})

(def donut-state (atom {:data ordinal-data
                        :svg {:width 950
                              :height 500}
                        :style {:inner-r 100
                                :colors ["steelblue"
                                         "green"
                                         "yellow"
                                         "orange"
                                         "pink"
                                         "brown"]}
                        :retriever-ks {:ord-ks [:name]
                                       :num-ks [:age]}}))

(om/root dp/donut-pie-chart donut-state {:target (q/get-el-by-id "donut-chart")})

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
