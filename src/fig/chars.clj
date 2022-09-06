(ns fig.chars)

; A bunch of Clojure wrappers around Java's character utilities

(defn isUpperCase [c] (Character/isUpperCase (char c)))

(defn isLowerCase [c] (Character/isLowerCase (char c)))

(defn isDigit [c] (Character/isDigit (char c)))

(defn isWhitespace [c] (Character/isWhitespace (char c)))

(defn isLetter [c] (Character/isLetter (char c)))

(defn isAlphanumeric [c] (or (isLetter c) (isDigit c)))

(defn isAlphabetic [c] (Character/isAlphabetic (char c)))

(defn toUpperCase [c] (Character/toUpperCase (char c)))

(defn toLowerCase [c] (Character/toLowerCase (char c)))


