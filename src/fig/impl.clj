(ns fig.impl
  (:require [clojure.string :as str]
            [fig.chars :as chars])
  (:use [fig.helpers]))

(def lastReturnValue (atom 0))

(declare interpret)

; Implementations of operators

(defn add [a b] (vectorise add a b (if (and (number? a) (number? b)) (+' a b) (str a b))))

(defn any [x] (condp apply [x]
                sequential? (elvis (some bool x) 0)
                number? (range 1 (inc x))
                string? (str/lower-case x)
                x))

(defn negate [x] (vectorise negate x (if (number? x) (-' x) (strmap #(if (chars/isUpperCase %)
                                                                       (chars/toLowerCase %)
                                                                       (chars/toUpperCase %))
                                                                    x))))

; Operators

(def operators {
                :ternaryIf       {:symbol "!" :arity 3 :macro true}
                :reverse         {:symbol "$" :arity 1}
                :add             {:symbol "+" :arity 2 :impl add}
                :println         {:symbol "," :arity 1 :impl (fn [x] (figPrint x) x)}
                :negate          {:symbol "N" :arity 1 :impl negate}
                :lastReturnValue {:symbol "Q" :arity 0 :impl (const lastReturnValue)}
                :list            {:symbol "`" :arity -1 :impl vector}
                :any             {:symbol "a" :arity 1 :impl any}
                :input           {:symbol "x" :arity 0}})   ; input is implemented in the interpret function itself


(defn attr [op attribute] (if (contains? operators op)
                            (get-in operators [op attribute])
                            (throw (IllegalArgumentException. "Unknown operator"))))

; Interpreter

(defn- nextInput [source]
  (let [it (first @source)]
    (if (.hasNext it)
      (.next it)
      (let [new (.iterator (second @source))]
        (if (.hasNext new)
          (do
            (swap! source assoc 0 new)
            (.next new))
          0)))))

(def ^:private inputStack (atom (list)))

(defn- pushInput [source] (swap! inputStack conj (atom (list (.iterator source) source))))

(defn- popInput [] (swap! inputStack pop))

(defn interpret [node]
  (reset! lastReturnValue
          (let [type (first node)]
            (cond
              (= type :constant) (second node)
              (= type :input) (nextInput (peek @inputStack))
              (= type :functionRef) (fn figLambda [input] (pushInput input)
                                      (let [result (interpret (second node))]
                                        (popInput)
                                        result))
              :else (apply (attr type :impl) (map interpret (second node)))))))


(defn interpretProgram [ast input]
  (reset! inputStack (list))
  (pushInput input)
  (last (map interpret ast)))
