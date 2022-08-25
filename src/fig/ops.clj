(ns fig.ops
  (:require [fig.impl :as impl]))

(def operators {
                :ternaryIf       {:symbol "!" :arity 3 :macro true}
                :reverse         {:symbol "$" :arity 1}
                :add             {:symbol "+" :arity 2 :impl impl/add}
                :negate          {:symbol "N" :arity 1 :impl impl/negate}
                :lastReturnValue {:symbol "Q" :arity 0 :impl (fn [] impl/lastReturnValue)}
                :list            {:symbol "`" :arity -1 :impl vector}
                :input           {:symbol "x" :arity 0}}) ; input is implemented in the interpret function itself


(defn attr [op attribute] (get-in operators [op attribute]))
