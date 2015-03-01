(ns katka.macro)

(defn method->interface
  [method]
  (->> (clojure.string/split (str (first method)) #"-")
       (map clojure.string/capitalize)
       (apply str "I")
       (symbol "om.core")))

(defn wrap-html
  [method]
  (let [[fname args & body] method]
    (if (contains? #{'render-state 'render} fname)
      `(~fname ~args
	       (do
		 ~@(butlast body))
	       (sablono.core/html
		~(last body)))
      method)))

(defmacro not-nil?
  [v]
  `(not (nil? ~v)))

(defmacro defcomponent
  [name & args]
  (let [[docstring args] (if (-> args first string?)
                           [(first args) (next args)]
                           [nil args])
        [args impls] [(first args) (next args)]
        pairs (mapcat (comp (juxt method->interface identity)
			    wrap-html)
		      impls)]
    `(do
       (defn ~name [~@args]
         (reify
           ~@pairs))
       (alter-meta! (var ~name)
                    assoc
                    :doc ~docstring))))
