(ns fig.helpers
  (:require [clojure.math.numeric-tower :as math]
            [clojure.string :as str]))

(def numberRegex #"^-?\d+(\.\d+)?$")

(declare readNumber)

(defn append [coll & stuff] (lazy-cat coll stuff))

(defn applyOnParts [f num] (->> (str/split (str num) #"\.")
                                (map f)
                                (map #(if (sequential? %) (str/join (flatten %)) %))
                                (str/join \.)
                                (readNumber)))

(defn bool [x] (cond
                 (or (coll? x) (string? x)) (boolean (seq x))
                 (number? x) (not (== x 0))
                 :else (boolean x)))

(defn collContains [coll x] (some #(if (and (number? x) (number? %)) (== x %) (= x %)) coll))

(defn cmp [a b] (cond
                  (and (number? a) (number? b)) (compare a b)
                  (and (string? a) (re-matches numberRegex a) (number? b)) (cmp (readNumber a) b)
                  (and (number? a) (string? b) (re-matches numberRegex b)) (cmp a (readNumber b))
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

(defn evalString [s]
  (let [s (str/trim s)]
    (cond
      (re-matches numberRegex s) (readNumber s)
      (and (str/starts-with? s "[") (str/ends-with? s "]"))
      (let [stripped (subs s 1 (dec (count s)))]
        (if (str/blank? stripped)
          []
          (loop [string ""
                 consuming (rest stripped)
                 quoting false
                 result (vector)
                 brackets 0
                 c (first stripped)]
            (cond
              (empty? consuming) (let [full (str string c)] (if (str/blank? full) result (conj result (evalString full))))
              (= c \") (recur (str string c) (rest consuming) (not quoting) brackets result (first consuming))
              (not quoting) (cond
                              (= c \[) (recur (str string c) (rest consuming) quoting result (inc brackets) (first consuming))
                              (= c \]) (recur (str string c) (rest consuming) quoting result (dec brackets) (first consuming))
                              (and (= c \,) (zero? brackets)) (recur "" (rest consuming) quoting (conj result (evalString string)) 0 (first consuming))
                              :else (recur (str string c) (rest consuming) quoting result brackets (first consuming)))
              :else (recur (str string c) (rest consuming) quoting result brackets (first consuming))))))
      (and (str/starts-with? s "\"") (str/ends-with? s "\"")) (subs s 1 (dec (count s)))
      :else s)))

(defn fromBase [n base] (let [res (reduce #(+' (*' %1 base) %2) (cons (math/abs (first n)) (rest n)))]
                          (if (neg? (first n)) (-' res) res)))

(defn isPrime [x]
  (cond
    (< x 2) 0
    (seq (filter (partial equal x) [2 3 5])) 1
    (some zero? (map #(mod x %) [2 3 5])) 0
    :else (let [limit (math/ceil (math/sqrt x))]
            (loop [i 7]
              (if (> i limit) 1 (if (zero? (mod x i)) 0 (recur (+' i 2))))))))

(defn printF
  ([obj] (printF obj "\n"))
  ([obj end] (printF obj end false))
  ([obj end quote]
   (cond
     (sequential? obj) (do
                         (print \[)
                         (loop [coll (seq obj)]
                           (when coll
                             (printF (first coll) nil true)
                             (when (next coll)
                               (print ", ")
                               (recur (next coll)))))
                         (print \]))
     (string? obj) ((if quote pr print) obj)
     (decimal? obj) (print (.toPlainString (.stripTrailingZeros obj)))
     :else (print (str obj)))
   (when (some? end) (print end))
   (flush)
   obj))

(defmacro matchp [x & exprs] `(condp apply [~x] ~@exprs))

(defn listify [x] (matchp x
                          coll? x
                          string? (map str x)
                          number? (digits x)
                          x))

(defn numify [x] (if (number? x) x (count x)))

(defn readNumber [x] (if (re-matches numberRegex x) (bigdec x) x))

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

(defn toBase [n base]
  (loop [ret (list)
         i (math/abs n)]
    (if (zero? i)
      (if (and (neg? n) (seq ret)) (cons (-' (first ret)) (rest ret)) ret)
      (recur (cons (mod i base) ret) (quot i base)))))

(defmacro vectorise
  ([this arg else] `(if (sequential? ~arg) (map ~this ~arg) ~else))
  ([this a b else] `(cond
                      (sequential? ~a) (if (sequential? ~b)
                                         (map ~this ~a ~b)
                                         (map ~this ~a (repeat ~b)))
                      (sequential? ~b) (map ~this (repeat ~a) ~b)
                      :else ~else)))

(defn vectoriseFn [f] (fn v [a] (vectorise f a (f a))))
