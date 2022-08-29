(ns fig.helpers
  (:require [clojure.test :as test]))

(defn bool [x] (cond
                 (or (coll? x) (string? x)) (boolean (seq x))
                 (number? x) (not (== x 0))
                 :else (boolean x)))

(defmacro const [x] `(fn [] ~x))

(defn elvis [x default] (if (nil? x) default x))

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

(defn putFunctionFirst [a b] (if (test/function? a) (list a b) (list b a)))

(defn strmap [f s] (clojure.string/join (map f (str s))))

(defmacro vectorise
  ([this arg else] `(if (sequential? ~arg) (map ~this ~arg) ~else))
  ([this a b else] `(cond
                      (sequential? ~a) (if (sequential? ~b)
                                         (map ~this ~a ~b)
                                         (map ~this ~a (repeat ~b)))
                      (sequential? ~b) (map ~this (repeat ~a) ~b)
                      :else ~else)))
