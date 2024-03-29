(ns fig.core
  (:gen-class)
  (:require [clojure.string :as str]
            [fig.impl :as interp]
            [fig.parsing :as parsing])
  (:use [fig.compression :only [codepage]]
        [fig.helpers :only [evalString]])
  (:import (ch.obermuhlner.math.big BigDecimalMath)
           (java.awt Toolkit)
           (java.awt.datatransfer StringSelection)
           (java.math MathContext RoundingMode)
           (java.nio.charset StandardCharsets)))

(defn- evalLine [toks input doDebug]
  (let [parsed (parsing/parse toks)]
    (when doDebug (println parsed))
    (interp/interpretProgram parsed input)))

(defn -main
  [& args]
  (let [mode (first args)
        code (str/replace (slurp (second args)) "\r" "")]
    (cond
      (= "format" mode) (let [sb (StringBuilder. "# [Fig](https://github.com/Seggan/Fig), ")]
                          (if (every? #(str/includes? codepage (str %)) code)
                            (let [log (with-precision 64 (/ (BigDecimalMath/log (bigdec (count codepage)) MathContext/DECIMAL128)
                                                            (BigDecimalMath/log 256M MathContext/DECIMAL128)))]
                              (.append sb (format "\\$%d\\log_{256}(%d)\\approx\\$ " (count code) (count codepage)))
                              (.append sb (.setScale (bigdec (* (count code) log)) 3 RoundingMode/HALF_UP)))
                            (.append sb (count (.getBytes code StandardCharsets/UTF_8))))
                          (.append sb (format " bytes\n```\n%s\n```\n" code))
                          (.append sb "[See the README to see how to run this](https://github.com/Seggan/Fig/blob/master/README.md)\n\n")
                          (-> (Toolkit/getDefaultToolkit)
                              (.getSystemClipboard)
                              (.setContents (StringSelection. (.toString sb)) nil))
                          (print (str sb))
                          (println "Copied to clipboard"))
      (or (= "run" mode) (= "debugRun" mode)) (let [lexed (parsing/lex code)
                                                    input (map evalString (rest (rest args)))]
                                                (when (= "debugRun" mode) (println lexed))
                                                (reset! interp/programInput (.iterator (if (seq input) (cycle input) (repeat 0))))
                                                (let [result (reduce #(vector (evalLine %2 %1 (= "debugRun" mode))) input lexed)
                                                      last (ffirst (parsing/parse (last lexed)))]
                                                  (when-not (or (= last :print) (= last :println))
                                                    (fig.helpers/printF (first result)))))
      :else (println "Usage: fig <mode> <file> [args...]"))))
