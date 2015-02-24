(ns katka.util.query)

(defn get-el-by-id
  [id]
  (.getElementById js/document id))

(defn get-els-by-class
  [cls]
  (.getElementsByClassName js/document cls))
