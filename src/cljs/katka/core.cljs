(ns katka.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [katka.chart.bar :as b]
            [katka.chart.line-area :as la]
            [katka.chart.bubble :as cb]
            [katka.util.dev :as dev]
            [katka.chart.donut-pie :as dp]
            [katka.util.query :as q]))

(defonce ordinal-data (map (fn [r]
                             {:name r
                              :age (+ r (rand-int 100))})
                           (range 50)))

(def vertical-bar-state (atom {:data ordinal-data
                               :svg {:width 960
                                     :height 500}
                               :rect-type "vertical"
                               :rect {:padding 0.1
                                      :fill "steelblue"}
                               :retriever-ks {:ord-ks [:name]
                                              :num-ks [:age]}
                               :x-axis {:orient "bottom"
                                        :end-text {:content "bah"}}
                               :y-axis {:orient "left"
                                        :end-text {:content "faris"}
                                        :ticks 100}}))

(om/root b/bar-chart vertical-bar-state {:target (q/get-el-by-id "vertical-bar-chart")})

(def horizontal-bar-state (atom {:data ordinal-data
                                 :svg {:width 960
                                       :height 500}
                                 :rect-type "horizontal"
                                 :rect {:padding 0.1
                                        :fill "steelblue"}
                                 :retriever-ks {:ord-ks [:name]
                                                :num-ks [:age]}
                                 :x-axis {:orient "bottom"
                                          :end-text {:content "bah"}
                                          :ticks 100}
                                 :y-axis {:orient "left"
                                          :end-text {:content "faris"}}}))

(om/root b/bar-chart
         horizontal-bar-state
         {:target (q/get-el-by-id "horizontal-bar-chart")})

(defonce numerical-data (map (fn [r]
                               {:x r
                                :y (+ r (rand-int 100))})
                             (range 50)))

(def line-state (atom {:data numerical-data
                       :svg {:width 960
                             :height 500}
                       :x-axis {:ticks 10
                                :orient "bottom"}
                       :y-axis {:ticks 100}
                       :retriever-ks {:x-ks [:x]
                                      :y-ks [[:y]]}}))

(om/root la/line-chart line-state {:target (q/get-el-by-id "line-chart")})

(defonce multi-line-data (map (fn [r]
                                {:x r
                                 :y0 (+ r (rand-int 100))
                                 :y1 (+ r (rand-int 75))
                                 :y2 (+ r (rand-int 50))})
                              (range 50)))

(def multi-line-state (atom {:data multi-line-data
                             :svg {:width 960
                                   :height 500}
                             :x-axis {:ticks 10
                                      :orient "bottom"}
                             :y-axis {:ticks 100}
                             :retriever-ks {:x-ks [:x]
                                            :y-ks [[:y0] [:y1] [:y2]]}}))

(om/root la/line-chart multi-line-state {:target (q/get-el-by-id "multi-line-chart")})

(def area-state (atom {:data numerical-data
                       :svg {:width 960
                             :height 500}
                       :area {:fill "steelblue"}
                       :x-axis {:ticks 5
                                :orient "bottom"}
                       :y-axis {:ticks 100}
                       :retriever-ks {:x-ks [:x]
                                      :y-ks [[:y]]}}))

(om/root la/area-chart area-state {:target (q/get-el-by-id "area-chart")})

(defonce bivariate-area-data (map (fn [r]
                                    {:x r
                                     :y1 (+ r (rand-int 100))
                                     :y0 (- r (rand-int 100))})
                                  (range 50)))

(def bivariate-area-state (atom {:data bivariate-area-data
                                 :svg {:width 960
                                       :height 500}
                                 :area {:fill "steelblue"}
                                 :x-axis {:ticks 5
                                          :orient "bottom"}
                                 :y-axis {:ticks 100}
                                 :retriever-ks {:x-ks [:x]
                                                :y-ks [[:y1] [:y0]]}}))

(om/root la/area-chart
         bivariate-area-state
         {:target (q/get-el-by-id "bivariate-area-chart")})

(def pie-state (atom {:data ordinal-data
                      :svg {:width 960
                            :height 500}
                      :style {:inner-r 0}
                      :retriever-ks {:ord-ks [:name]
                                     :num-ks [:age]}}))

(om/root dp/donut-pie-chart pie-state {:target (q/get-el-by-id "pie-chart")})

(def donut-state (atom {:data ordinal-data
                        :svg {:width 950
                              :height 500}
                        :style {:inner-r 100}
                        :retriever-ks {:ord-ks [:name]
                                       :num-ks [:age]}}))

(om/root dp/donut-pie-chart donut-state {:target (q/get-el-by-id "donut-chart")})

(defonce bubble-data (map (fn [r]
                            {:name r
                             :age (+ r (rand-int 100))
                             :group (rand-nth
                                     ["smart"
                                      "genius"
                                      "normal"
                                      "duh"
                                      "extraordinary"
                                      "dumb"
                                      "dumber"
                                      "stupid"
                                      "idiot"
                                      "faggot"])})
                          (range 100)))

(def bubble-state (atom {:data bubble-data
                         :svg {:width 960
                               :height 500}
                         :style {:padding 1.5
                                 :sorting-type :ascending-group
                                 :colors :c10}
                         :retriever-ks {:ord-ks [:name]
                                        :num-ks [:age]
                                        :group-ks [:group]}}))

(om/root cb/bubble-chart bubble-state {:target (q/get-el-by-id "bubble-chart")})

;; ;; ===================================================================

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

;; (def q (cb/bubble-layout {:sort-fn nil
;;                           :size [960 960]
;;                           :padding 1.5
;;                           :value #(first %)
;;                           :children #(:children %)}))

;; (def w (js-obj "children" (array (js-obj "value" 20)
;;                                  (js-obj "value" 30))))

;; (def e {:children (apply array [[50]
;;                                 [20]
;;                                 [30]
;;                                 [1]])})

;; (def r (js-obj "children" (apply array e)))

;; (dev/log (.nodes q e))
