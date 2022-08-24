(ns fig.parsing
  (:require [clojure.string :as str]))

(def ^:private tokenMap {")"  :closer
                         "'"  :functionRef
                         "@"  :operatorRef
                         "U"  :unpack
                         "\n" :endFunction})




(defn lex [s]
  (let [input (str/replace s "\r" "")
        tokens (atom (list))
        i (atom 0)]
    (while (< @i (count input))
      (let [c (str (get input @i))]
        (swap! i inc)
        (cond
          (= c " ") nil
          (= c "\"") (let [string (atom "")]
                       (while (and (< @i (count input))
                                   (not= (get input @i) \"))
                         (when (= (get input @i) \\)
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
                                    (swap! tokens conj (list :operator (str c (get input @i))))
                                    (swap! i inc))
          (contains? tokenMap c) (swap! tokens conj (list (get tokenMap c)))
          :else (swap! tokens conj (list :operator (str c))))))
    (reverse (deref tokens))))





