(ns katka.shape
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(defn rect
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
  (reify
    om/IRender
    (render [_]
      (html [:rect (select-keys config [:fill :width :height :x :y :rx :ry])]))))

(defn text
  "Creates a basic React text element.

   Accept:

   {:x <num>
    :y <num>
    :dx <num>
    :dy <num>
    :text-anchor <string>
    :transform <string>}"
  [{:keys [content] :as config} owner]
  (reify
    om/IRender
    (render [_]
      (html [:text (select-keys config [:x :y :dx :dy :text-anchor :transform])
             content]))))

(defn line
  "Creates a basic React line element.

   Accept:

   {:x1 <num>
    :y1 <num>
    :x2 <num>
    :y2 <num>}
    :stroke <string>
    :stroke-width <num>"
  [config owner]
  (reify
    om/IRender
    (render [_]
      (html [:line (select-keys config [:x1 :y1 :x2 :y2
                                        :stroke :stroke-width])]))))

(defn path
  "Creates a basic React path element.

   Accept:

   {:d <i dont know>
    :stroke <string>
    :stroke-width <num>
    :fill <string>}"
  [config owner]
  (reify
    om/IRender
    (render [_]
      (html [:path (select-keys config [:d :stroke :stroke-width :fill])]))))
