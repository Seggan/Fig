(ns fig.impl
  (:require [fig.helpers :as h]))

(def lastReturnValue (atom 0))

(defn add [a b] (if (and (number? a) (number? b)) (+' a b) (str a b)))

(defn negate [arg] (h/vectorise negate arg (if (number? arg) (-' arg) (str "-" arg))))
