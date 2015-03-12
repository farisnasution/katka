(ns katka.util.dev)

(defn alert
  [m & [as-cljs]]
  (.alert js/window (if (true? as-cljs)
                      m
                      (clj->js m))))

(defn log
  [m & [as-cljs]]
  (.log js/console (if (true? as-cljs)
                     m
                     (clj->js m))))

(defn p
  ([data message]
   (when-not (or (nil? message)
                 (= "" message))
     (log (str "Message: " message)))
   (log (clj->js data))
   data)
  ([data]
   (p data "")))

(defn b
  ([f message]
   (let [start (.now js/Date)
         result (f)
         duration (- (.now js/Date) start)]
     (when-not (or (nil? message)
                   (= "" message))
       (log (str "Message: " message)))
     (log (str "Duration: " duration))
     result))
  ([f]
   (b f "")))
