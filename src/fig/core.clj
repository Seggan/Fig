(ns fig.core
  (:gen-class)
  (:require [fig.impl :as interp]
            [fig.parsing :as parsing]))

(defn -main
  [& args]
  (let [code (slurp (first args))
        lexed (parsing/lex code)]
    (println lexed)
    (let [parsed (parsing/parse lexed)]
      (println parsed)
      (fig.helpers/figPrint (interp/interpretProgram parsed (rest args))))))