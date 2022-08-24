(ns fig.core
  (:gen-class)
  (:require [fig.parsing :as parsing]))

(defn -main
  [& args]
  (let [code (slurp (first args))]
    (println (parsing/lex code))))
