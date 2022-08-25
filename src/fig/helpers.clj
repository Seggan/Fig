(ns fig.helpers)

(defn figPrint
  ([obj] (figPrint obj "\n"))
  ([obj end]
   (if (sequential? obj)
     (do
       (print \[)
       (let [it (.iterator obj)]
         (while (.hasNext it)
           (figPrint (.next it) nil)
           (when (.hasNext it)
             (print ", "))))
       (print \]))
     (print (str obj)))
   (when (some? end) (print end))))

(defmacro vectorise
  [this arg else] `(let [~'a ~arg] (if (sequential? ~'a) (map ~this ~'a) ~else)))
