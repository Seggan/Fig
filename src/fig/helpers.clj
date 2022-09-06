(ns fig.helpers
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn append [coll & stuff] (lazy-cat coll stuff))

(defn applyOnParts [f num] (->> (str/split (str num) #"\.")
                                (map f)
                                (map #(if (sequential? %) (str/join (flatten %)) %))
                                (str/join \.)
                                (edn/read-string)))

(defn bool [x] (cond
                 (or (coll? x) (string? x)) (boolean (seq x))
                 (number? x) (not (== x 0))
                 :else (boolean x)))

(defn collContains [coll x] (some #(if (and (number? x) (number? %)) (== x %) (= x %)) coll))

(defn cmp [a b] (cond
                  (and (number? a) (number? b)) (compare a b)
                  (and (coll? a) (coll? b)) (let [aIt (.iterator a)
                                                  bIt (.iterator b)]
                                              (loop []
                                                (if (.hasNext aIt)
                                                  (if (.hasNext bIt)
                                                    (let [cmpResult (cmp (.next aIt) (.next bIt))]
                                                      (if (= cmpResult 0)
                                                        (recur)
                                                        cmpResult))
                                                    1)
                                                  (if (.hasNext bIt) -1 0))))
                  :else (compare a b)))

(defmacro const [x] `(fn [] ~x))

(defn digits [x] (map #(- (int %) (int \0)) (filter #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} (str x))))

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

(defn printF
  ([obj] (printF obj "\n"))
  ([obj end]
   (if (sequential? obj)
     (do
       (print \[)
       (let [it (.iterator obj)]
         (while (.hasNext it)
           (printF (.next it) nil)
           (when (.hasNext it)
             (print ", "))))
       (print \]))
     (print (str obj)))
   (when (some? end) (print end))
   obj))

(defmacro matchp [x & exprs] `(condp apply [~x] ~@exprs))

(defn listify [x] (matchp x
                          coll? x
                          string? (map str x)
                          number? (digits x)
                          x))

(defn numify [x] (if (number? x) x (count x)))

(defn sortTypes
  ([t1 t2 a b] (cond
                 (and (t1 a) (t2 b)) (list a b)
                 (and (t1 b) (t2 a)) (list b a)
                 :else nil))
  ([t1 t2 t3 a b c] (cond
                      (and (t1 a) (t2 b) (t3 c)) (list a b c)
                      (and (t1 a) (t2 c) (t3 b)) (list a c b)
                      (and (t1 b) (t2 a) (t3 c)) (list b a c)
                      (and (t1 b) (t2 c) (t3 a)) (list b c a)
                      (and (t1 c) (t2 a) (t3 b)) (list c a b)
                      (and (t1 c) (t2 b) (t3 a)) (list c b a)
                      :else nil)))

(defn strmap [f s] (str/join (map f (str s))))

(defn strmapstr [f s] (str/join (map f (listify s))))

(defmacro tempAtomValue [var value code] `(let [~'oldValue (deref ~var)]
                                            (reset! ~var ~value)
                                            (let [~'result ~code]
                                              (reset! ~var ~'oldValue)
                                              ~'result)))

(defmacro vectorise
  ([this arg else] `(if (sequential? ~arg) (map ~this ~arg) ~else))
  ([this a b else] `(cond
                      (sequential? ~a) (if (sequential? ~b)
                                         (map ~this ~a ~b)
                                         (map ~this ~a (repeat ~b)))
                      (sequential? ~b) (map ~this (repeat ~a) ~b)
                      :else ~else)))
