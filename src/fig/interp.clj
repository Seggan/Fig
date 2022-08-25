(ns fig.interp
  (:require [fig.impl :as impl]
            [fig.ops :as ops]))

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
  (reset! impl/lastReturnValue
          (let [type (first node)]
            (cond
              (= type :constant) (second node)
              (= type :input) (nextInput (peek @inputStack))
              (= type :functionRef) (fn [input] (pushInput input)
                                      (let [result (interpret (second node))]
                                        (popInput)
                                        result))
              :else (apply (ops/attr type :impl) (map interpret (second node)))))))


(defn interpretProgram [ast input]
  (reset! inputStack (list))
  (pushInput input)
  (last (map interpret ast)))



