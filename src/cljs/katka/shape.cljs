(ns katka.shape
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]])
  (:use-macros [katka.macro :only [defcomponent]]))

(defcomponent rect
  "Creates a basic React rect element.

   Accept:

   {:fill <string>
    :width <num>
    :height <num>
    :x <num>
    :y <num>
    :rx <num>
    :ry <num>}"
  [config owner]
  (display-name [_] "rect")
  (render [_]
          [:rect (select-keys config
                              [:fill :width :height :stroke
                               :stroke-width :x :y :rx :ry :transform])]))

(defcomponent text
  "Creates a basic React text element.

   Accept:

   {:x <num>
    :y <num>
    :dx <num>
    :dy <num>
    :text-anchor <string>
    :transform <string>}"
  [config owner]
  (display-name [_] "text")
  (render [_]
          [:text (select-keys config
                              [:x :y :dx :dy
                               :text-anchor :transform])
           (:content config)]))

(defcomponent line
  "Creates a basic React line element.

   Accept:

   {:x1 <num>
    :y1 <num>
    :x2 <num>
    :y2 <num>}
    :stroke <string>
    :stroke-width <num>"
  [config owner]
  (display-name [_] "line")
  (render [_]
          [:line (select-keys config
                              [:x1 :y1 :x2 :y2
                               :stroke :stroke-width :transform])]))

(defcomponent path
  "Creates a basic React path element.

   Accept:

   {:d <i dont know>
    :stroke <string>
    :stroke-width <num>
    :fill <string>}"
  [config owner]
  (display-name [_] "path")
  (render [_]
          [:path (select-keys config
                              [:d :stroke :stroke-width :fill :transform])]))

(defcomponent circle
  "Creates a basic React path element.

   Accept:

   {:cx <num>
    :cy <num>
    :r <num>}"
  [config owner]
  (display-name [_] "circle")
  (render [_]
          [:circle (select-keys config [:cx :cy :r :fill :transform])]))
