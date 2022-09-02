(ns fig.impl
  (:require [clojure.math.numeric-tower :as math]
            [clojure.string :as str]
            [fig.chars :as chars]
            [fig.compression :as compression])
  (:use [fig.helpers]))

(def lastReturnValue (atom 0))

(declare interpret)

; Implementations of operators

(defn add [a b] (vectorise add a b (if (and (number? a) (number? b)) (+' a b) (str a b))))

(defn all [x] (matchPreds x
                          sequential? (if (every? bool x) 1 0)
                          number? (math/abs x)
                          string? (str/upper-case x)
                          x))

(defn any [x] (matchPreds x
                          sequential? (elvis (some bool x) 0)
                          number? (range 1 (inc x))
                          string? (str/lower-case x)
                          x))

(defn binaryIf [a b] (format "'%s'" (let [condResult (interpret a)] (if (bool condResult) (interpret b) condResult))))

(defn chrOrd [x]
  (vectorise chrOrd x
             (if (number? x)
               (str (char x))
               (if (= 1 (count x))
                 (int (first x))
                 (map int x)))))

(defn compress [x] (let [compressed (compression/compress x)] (if (< (count compressed) (count x)) compressed x)))

(defn even [x] (matchPreds x
                           sequential? (map-indexed vector x)
                           number? (if (= 0 (mod x 2)) 1 0)
                           string? (map-indexed #(vector %1 (str %2)) x)
                           x))

(defn equals [a b]
  (let [[_ x] (sortTypesDyadic sequential? identity a b)]
    (if-not (sequential? x)
      (vectorise equals a b (if (equal a b) 1 0))
      (if (equal a b) 1 0))))

(defn filterF [a b]
  (let [[f coll] (sortTypesDyadic fn? identity a b)]
    (matchPreds coll
                sequential? (filter f coll)
                string? (str/join (filter f (listify coll)))
                (let [check (complement (set (listify a)))]
                  (matchPreds b
                              string? (str/join (filter check (listify b)))
                              sequential? (filter check b)
                              a)))))

(defn generate [a b]
  (let [[f i] (sortTypesDyadic fn? identity a b)]
    (if (some? f)
      (map first (iterate #(rest (append % (apply f %))) (take
                                                           (elvis (:figArity (meta f)) 1)
                                                           (cycle (if (seqable? i) i (list i))))))
      (max-key cmp a b))))

(defn loopForever [& x] (loop [] (run! interpret x) (recur)))

(defn modulo [a b]
  (cond
    (and (number? a) (number? b)) (mod b a)
    (string? a) (if (sequential? b)
                  (let [replacing (atom a)
                        coll (atom (cycle b))]
                    (while (str/includes? @replacing "%")
                      (swap! replacing str/replace-first "%" (str/re-quote-replacement (str (first @coll))))
                      (swap! coll rest))
                    (deref replacing))
                  (str/replace a "%" (str/re-quote-replacement (str b))))
    :else (vectorise modulo a b a)))

(defn multiply [a b]
  (vectorise multiply a b
             (if (and (number? a) (number? b))
               (*' a b)
               (let [[times x] (sortTypesDyadic number? string? a b)]
                 (if (some? times)
                   (.repeat x times)
                   a)))))

(defn negate [x]
  (vectorise negate x
             (if (number? x)
               (-' x)
               (strmap #(if (chars/isUpperCase %)
                          (chars/toLowerCase %)
                          (chars/toUpperCase %))
                       x))))

(defn reverseF [x] (matchPreds x
                               sequential? (reverse x)
                               string? (str/reverse x)
                               number? (applyOnParts #(str/reverse %) x)
                               x))

(defn subtract [a b]
  (vectorise subtract a b
             (cond
               (and (number? a) (number? b)) (-' b a)
               (and (string? a) (string? b)) (str/replace b a "")
               :else a)))

(defn ternaryIf [a b c] (if (bool (interpret a)) (interpret b) (interpret c)))

; Operators

(def operators {
                :ternaryIf       {:symbol "!" :arity 3 :impl ternaryIf :macro true}
                :reverse         {:symbol "$" :arity 1 :impl reverseF}
                :modulo          {:symbol "%" :arity 2 :impl modulo}
                :loopForever     {:symbol "(" :arity -1 :impl loopForever :macro true}
                :multiply        {:symbol "*" :arity 2 :impl multiply}
                :add             {:symbol "+" :arity 2 :impl add}
                :println         {:symbol "," :arity 1 :impl printF}
                :subtract        {:symbol "-" :arity 2 :impl subtract}
                :pair            {:symbol ":" :arity 1 :impl #(vector % %)}
                :print           {:symbol ";" :arity 1 :impl #(printF % nil)}
                :lessThan        {:symbol "<" :arity 2 :impl #(if (< (cmp %1 %2) 0) 1 0)}
                :equals          {:symbol "=" :arity 2 :impl equals}
                :greaterThan     {:symbol ">" :arity 2 :impl #(if (> (cmp %1 %2) 0) 1 0)}
                :binaryIf        {:symbol "?" :arity 2 :impl binaryIf :macro true}
                :all             {:symbol "A" :arity 1 :impl all}
                :chrOrd          {:symbol "C" :arity 1 :impl chrOrd}
                :compress        {:symbol "#D" :arity 1 :impl compress}
                :even            {:symbol "E" :arity 1 :impl even}
                :filter          {:symbol "F" :arity 2 :impl filterF}
                :generate        {:symbol "G" :arity 2 :impl generate}
                :negate          {:symbol "N" :arity 1 :impl negate}
                :lastReturnValue {:symbol "Q" :arity 0 :impl (const lastReturnValue)}
                :wrapOne         {:symbol "W" :arity 1 :impl vector}
                :list            {:symbol "`" :arity -1 :impl vector}
                :any             {:symbol "a" :arity 1 :impl any}
                :wrapTwo         {:symbol "w" :arity 2 :impl vector}
                :input           {:symbol "x" :arity 0}})   ; input is implemented in the interpret function itself


(defn attr [op attribute] (if (contains? operators op)
                            (get-in operators [op attribute])
                            (throw (IllegalArgumentException. "Unknown operator"))))

; Interpreter

(def ^:private input (atom nil))

(defmacro ^:private ownStackFrame [source code]
  `(let [~'oldInput (deref input)]
     (reset! input (.iterator (cycle ~source)))
     (let [~'result ~code]
       (reset! input ~'oldInput)
       ~'result)))


(defn interpret [node]
  (reset! lastReturnValue
          (let [[type args] node]
            (cond
              (= type :constant) args
              (= type :input) (.next @input)
              (= type :functionRef) (with-meta
                                      (fn figLambda [& inp] (ownStackFrame inp (interpret args)))
                                      {:figArity (count (filter #{:input} (flatten args)))})
              :else (apply (attr type :impl) (if (attr type :macro) args (map interpret args)))))))

(defn interpretProgram [ast programInput]
  (ownStackFrame programInput (elvis (last (map interpret (filter coll? ast))) 0)))
