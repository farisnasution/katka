(ns katka.util.dev)

(defn alert
  [m]
  (.alert js/window m))

(defn log
  [m]
  (.log js/console m))

(defn p
  ([data message]
   (when-not (or (nil? message)
                 (= "" message))
     (log (str "Message: " message)))
   (log data)
   data
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
