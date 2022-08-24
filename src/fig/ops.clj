(ns fig.ops)

(def operators {
                :ternaryIf {:symbol "!" :arity 3 :macro true}
                :reverse {:symbol "$" :arity 1}
                :input {:symbol "x" :arity 0}})
