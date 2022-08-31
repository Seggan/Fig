(ns fig.helpers
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test]))

(defn applyIf [pred f x] (if (pred x) (f x) x))

(defn applyOnParts [f num] (edn/read-string (str/join \. (map f (str/split (str num) #"\.")))))

(defn bool [x] (cond
                 (or (coll? x) (string? x)) (boolean (seq x))
                 (number? x) (not (== x 0))
                 :else (boolean x)))

(defmacro const [x] `(fn [] ~x))

(defn elvis [x default] (if (nil? x) default x))

(defn equal [a b]
  (cond
    (or (and (string? a) (number? b)) (and (number? a) (string? b))) (= (str a) (str b))
    (and (number? a) (number? b)) (== a b)
    (and (coll? a) (coll? b)) (let [aIt (.iterator a)
                                    bIt (.iterator b)]
                                (loop []
                                  (if (= (.hasNext aIt) (.hasNext bIt))
                                    (if (.hasNext aIt)
                                      (if (equal (.next aIt) (.next bIt))
                                        (recur)
                                        false)
                                      true)
                                    false)))
    :else (= a b)))

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

(defmacro matchPreds [x & exprs] `(condp apply [~x] ~@exprs))

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
