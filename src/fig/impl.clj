(ns fig.impl
  (:require [clojure.math.numeric-tower :as math]
            [clojure.string :as str]
            [fig.chars :as chars]
            [fig.compression :as compression])
  (:use [fig.helpers]))

; Stuff needed by the interpreter

(def ^:private lastReturnValue (atom 0))

(def ^:private register (atom 0))

(def ^:private input (atom nil))

(def ^:private currentFunction (atom nil))

(declare interpret)

; Implementations of operators

(defn add [a b] (vectorise add a b (if (and (number? a) (number? b)) (+' a b) (str a b))))

(defn addToEnd [a b] (cond
                       (and (sequential? a) (sequential? b)) (lazy-cat a b)
                       (sequential? a) (append a b)
                       (sequential? b) (cons a b)
                       :else (str a b)))

(defn all [x] (matchp x
                      sequential? (if (every? bool x) 1 0)
                      number? (math/abs x)
                      string? (str/upper-case x)
                      x))

(defn any [x] (matchp x
                      sequential? (elvis (some bool x) 0)
                      number? (range 1 (inc x))
                      string? (str/lower-case x)
                      x))

(defn binaryIf [a b] (format "'%s'" (let [condResult (interpret a)] (if (bool condResult) (interpret b) condResult))))

(defn chrOrd [x]
  (vectorise chrOrd x
             (matchp x
                     number? (str (char x))
                     string? (if (= 1 (count x))
                               (int (first x))
                               (map int x))
                     fn? (fn [& inp] (if (bool (apply x inp)) 0 1)))))

(defn compress [x] (let [compressed (compression/compress x)] (if (< (count compressed) (count x)) compressed x)))

(defn even [x] (matchp x
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
    (matchp coll
            sequential? (filter f coll)
            string? (str/join (filter f (listify coll)))
            (let [check (complement (set (listify a)))]
              (matchp b
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

(defn halve [x]
  (vectorise halve x
             (if (number? x)
               (/ x 2M)
               (let [s (str x)
                     len (count s)
                     half (/ len 2)]
                 (list (subs s 0 half) (subs s half len))))))

(defn interleaveF [a b]
  (cond
    (sequential? a) (interleave a (listify b))
    (sequential? b) (interleave (listify a) b)
    (and (string? a) (string? b)) (str/join (interleave a b))
    :else a))

(defn loopForever [& x] (loop [] (run! interpret x) (recur)))

(defn mapF [a b]
  (let [[f coll] (sortTypesDyadic fn? identity a b)]
    (matchp coll
            sequential? (map f coll)
            string? (strmapstr f coll)
            number? (applyOnParts #(f (digits %)) coll)
            fn? (comp a b)
            (let [replacer (fn [_] a)]
              (matchp b
                      string? (strmapstr replacer b)
                      sequential? (map replacer b)
                      number? (applyOnParts #(repeat (count %) a) b)
                      fn? (fn [& _] a)
                      a)))))

(defn modulo [a b]
  (cond
    (and (number? a) (number? b)) (mod b a)
    (string? a) (if (sequential? b)
                  (let [replacing (atom a)
                        coll (.iterator (cycle b))]
                    (while (str/includes? @replacing "%")
                      (swap! replacing str/replace-first "%" (str/re-quote-replacement (str (.next coll)))))
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

(defn odd [x] (matchp x
                      sequential? (str/join (flatten x))
                      number? (mod x 2)
                      x))

(defn reduceF [a b]
  (if (and (number? a) (number? b))
    (range a b)
    (let [[f coll] (sortTypesDyadic fn? identity a b)]
      (if (seq coll)
        (matchp coll
                sequential? (reduce f coll)
                string? (reduce f (listify coll))
                number? (reduce f (digits coll)))
        0))))

(defn reverseF [x] (matchp x
                           sequential? (reverse x)
                           string? (str/reverse x)
                           number? (applyOnParts #(str/reverse %) x)
                           x))

(defn sortIncreasing [x] (matchp x
                                 sequential? (sort x)
                                 string? (str/join (sort x))
                                 number? (applyOnParts #(str/join (sort %)) x)
                                 x))

(defn subtract [a b]
  (vectorise subtract a b
             (cond
               (and (number? a) (number? b)) (-' b a)
               (and (string? a) (string? b)) (str/replace b a "")
               :else a)))

(defn ternaryIf [a b c] (if (bool (interpret a)) (interpret b) (interpret c)))

(defn transliterate [a b c]
  (if (and (string? a) (string? b) (string? c))
    (let [m (zipmap a b)] (strmap #(if (contains? m %) (get m %) %) c))
    (let [[f indexes coll] (sortTypesTriadic fn? sequential? identity a b c)]
      (if (some? f)
        (matchp coll
                sequential? (map-indexed #(if (collContains indexes %1) (f %2) %2) coll)
                string? (str/join (map-indexed #(if (collContains indexes %1) (f %2) %2) (listify coll))))
        (let [[f index coll] (sortTypesTriadic fn? number? identity a b c)]
          (matchp coll
                  sequential? (map-indexed #(if (= index %1) (f %2) %2) coll)
                  string? (str/join (map-indexed #(if (= index %1) (f %2) %2) (listify coll)))))))))

(defn truthyIndexes [x] (matchp x
                                sequential? (keep-indexed #(if (bool %2) %1) x)
                                fn? (with-meta x {:figArity 3})
                                x))

(defn uniquify [x] (matchp x
                           sequential? (distinct x)
                           string? (str/join (distinct x))
                           number? (applyOnParts distinct x)
                           x))

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
                :halve           {:symbol "H" :arity 1 :impl halve}
                :count           {:symbol "I" :arity 1 :impl (fn [a b] (count (filter #(equal b %) (listify a))))}
                :addToEnd        {:symbol "J" :arity 2 :impl addToEnd}
                :sortIncreasing  {:symbol "K" :arity 1 :impl sortIncreasing}
                :length          {:symbol "L" :arity 1 :impl #(if (number? %) (count (str (bigint %))) (count %))}
                :map             {:symbol "M" :arity 2 :impl mapF}
                :negate          {:symbol "N" :arity 1 :impl negate}
                :odd             {:symbol "O" :arity 1 :impl odd}
                :transliterate   {:symbol "P" :arity 3 :impl transliterate}
                :lastReturnValue {:symbol "Q" :arity 0 :impl (const (deref lastReturnValue))}
                :reduce          {:symbol "R" :arity 2 :impl reduceF}
                :sum             {:symbol "S" :arity 1 :impl #(reduceF add %)}
                :truthyIndexes   {:symbol "T" :arity 1 :impl truthyIndexes}
                :uniquify        {:symbol "U" :arity 1 :impl uniquify}
                :setRegister     {:symbol "V" :arity 1 :impl #(reset! register %)}
                :wrapOne         {:symbol "W" :arity 1 :impl vector}
                :currentFunction {:symbol "X" :arity 0 :impl (const (deref currentFunction))}
                :interleave      {:symbol "Y" :arity 2 :impl interleaveF}
                :zip             {:symbol "Z" :arity 2 :impl #(map vector (listify %1) (listify %2))}
                :list            {:symbol "`" :arity -1 :impl vector}
                :any             {:symbol "a" :arity 1 :impl any}
                :wrapTwo         {:symbol "w" :arity 2 :impl vector}
                :input           {:symbol "x" :arity 0}})   ; input is implemented in the interpret function itself


(defn attr [op attribute] (if (contains? operators op)
                            (get-in operators [op attribute])
                            (throw (IllegalArgumentException. "Unknown operator"))))

; Interpreter

(defn interpret [node]
  (reset! lastReturnValue
          (let [[type args] node]
            (cond
              (= type :constant) args
              (= type :input) (.next @input)
              (= type :functionRef) (let [arity {:figArity (count (filter #{:input} (flatten args)))}]
                                      (with-meta
                                        (fn figLambda [& inp] (tempAtomValue
                                                                input
                                                                (.iterator (cycle inp))
                                                                (tempAtomValue
                                                                  currentFunction
                                                                  (with-meta figLambda arity)
                                                                  (interpret args)))) arity))
              :else (apply (attr type :impl) (if (attr type :macro) args (map interpret args)))))))

(defn interpretProgram [ast programInput] (tempAtomValue
                                            input
                                            (.iterator (cycle programInput))
                                            (tempAtomValue
                                              currentFunction
                                              (with-meta
                                                (fn [& inp] (apply interpret (cons ast inp)))
                                                {:figArity (count (filter #{:input} (flatten ast)))})
                                              (elvis (last (map interpret (filter coll? ast))) 0))))
