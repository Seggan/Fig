(ns fig.parsing
  (:require [clojure.string :as str]
            [fig.ops :as ops]))

(def ^:private tokenMap {")"  :closer
                         "'"  :functionRef
                         "@"  :operatorRef
                         "U"  :unpack
                         "\n" :endFunction})

(defn ^:private whereValue [pred map] (-> #(pred (second %)) (filter map) (first) (first)))

(defn lex [s]
  (let [input (str/replace s "\r" "")
        tokens (atom (vector))
        i (atom 0)]
    (while (< @i (count input)) ; I went for a definitely non-functional while loop
      (let [c (str (get input @i))]
        (swap! i inc)
        (cond
          (= c " ") nil
          (= c "\"") (let [string (atom "")]
                       (while (and (< @i (count input))
                                   (not= (get input @i) \"))
                         (when (= (get input @i) \\) ; if backslash, consume next character
                           (swap! string str \\)
                           (swap! i inc))
                         (swap! string str (get input @i))
                         (swap! i inc))
                       (swap! tokens conj (list :string @string))
                       (swap! i inc))
          (str/includes? "123456789" c) (let [n (atom (str c))]
                                          (while (and (str/includes? "0123456789." (str (get input @i)))
                                                      (< @i (count input)))
                                            (swap! n str (get input @i))
                                            (swap! i inc))
                                          (swap! tokens conj (list :number (bigdec (str @n)))))
          (= c "0") (swap! tokens conj (list :number 0))
          (and (= c "#") (= (get input @i) \space)) (do
                                                      (while (not= (get input @i) \newline) (swap! i inc))
                                                      (swap! i inc))
          (str/includes? "cm#" c) (do
                                    (swap! tokens conj (list :operator (whereValue
                                                                         #(= (:symbol %) (str c (get input @i)))
                                                                         ops/operators)))
                                    (swap! i inc))
          (contains? tokenMap c) (swap! tokens conj (list (get tokenMap c)))
          :else (swap! tokens conj (list :operator (whereValue #(= (:symbol %) (str c)) ops/operators))))))
    (deref tokens)))

(def ^:private isEnding (atom false))

(declare ^:private parseOperator)
(declare ^:private parse)

(defn ^:private consume [list] (-> (swap-vals! list rest) (first) (first)))

(defn ^:private parseToken [token tokens]
  (let [tokenType (first token)]
    (cond
      (or (= tokenType :string) (= tokenType :number)) (list :constant (second token))
      (= tokenType :operator) (parseOperator token tokens)
      :else '(:nop))))

(defn ^:private parseOperator [token tokens]
  (let [op (second token)
        arity (get-in ops/operators [op :arity])]
    (list op (if (= arity -1)
               (let [cond #(and (not= % :closer) (not= % :endFunction))
                     args (first (parse (take-while cond @tokens)))]
                 (reset! tokens (rest (drop-while cond @tokens)))
                 args)
               (let [args (atom (vector))]
                 (loop [i 0]
                   (if (< i arity)
                     (if (or (empty? @tokens) @isEnding)
                       (swap! args concat (repeat (- arity i) '(:input)))
                       (let [next (consume tokens)
                             nextType (first next)]
                         (if (= nextType :unpack)
                           (swap! args conj (list :unpack (parseToken (consume tokens) tokens)))
                           (if (= nextType :endFunction)
                              (do
                                (reset! isEnding true)
                                (recur i))
                              (do
                                (swap! args conj (parseToken next tokens))
                                (recur (inc i)))))))
                     (deref args))))))))

(defn parse [tokens]
  (let [toks (atom tokens)
        parsed (atom (vector))]
     (while (seq @toks)
       (reset! isEnding false)
       (swap! parsed conj (parseToken (consume toks) toks)))
     (deref parsed)))



