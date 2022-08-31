(ns fig.compression
  (:require [clojure.string :as str]
            [fig.chars :as chars]))

(def codepage "\n !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~")

(def compressionCodepage " !#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~")

(def compressableChars "abcdefghijklmnopqrstuvwxyz\n0123456789'()*+,-. !\"#$%&/:;<=>?@[\\]^_`{|}~")

(def dictionary (str/split-lines (slurp (clojure.java.io/resource "dict.txt"))))

(defn- divmod! "Swaps the value in the num atom to be num // i and returns num % i"
  [num i] (mod (first (swap-vals! num quot i)) i))

(defn toBijectiveBase [n base]
  (let [i (atom n)
        result (atom (transient []))]
    (while (> @i 0)
      (swap! i dec')
      (swap! result conj! (divmod! i base)))
    (reverse (persistent! (deref result)))))

(defn fromBijectiveBase [list base] (reduce (fn [acc i] (+' (*' acc base) (inc' i))) 0 list))

(defn- applyIf [cond f arg] (if cond (f arg) arg))

(defn- lastBitSet [n] (> (bit-and n 1) 0))

(def ^:private >> bit-shift-right)
(def ^:private << bit-shift-left)

(defn decompress
  ([s] (decompress s compressionCodepage compressableChars dictionary))
  ([s codepage cPage dictionary]
   (let [result (atom (transient []))
         num (atom (fromBijectiveBase (map #(str/index-of codepage %) s) (count codepage)))]
     (while (> @num 0)
       (let [tag (long (divmod! num 8))]
         (swap! result conj! (if (= (bit-and tag 2r100) 0)
                               (->> (count cPage) (divmod! num) (get cPage) (applyIf (lastBitSet tag) chars/toUpperCase))
                               (->> (count dictionary) (divmod! num) (get dictionary) (applyIf (lastBitSet tag) str/capitalize))))
         (if (> (bit-and tag 2r010) 0) (swap! result conj! \space))))
     (str/join (reverse (persistent! (deref result)))))))

(defn compress
  ([s] (compress s compressionCodepage compressableChars dictionary))
  ([^String s codepage cPage dictionary]
   (let [string (StringBuilder. s)
         num (atom 0)]
     (while (seq string)
       (let [isSpace (= (first string) \space)]
         (if isSpace (.deleteCharAt string 0))
         (let [c (chars/toLowerCase (first string))
               isUpper (chars/isUpperCase (first string))]
           (.deleteCharAt string 0)
           (.insert string 0 (chars/toLowerCase c))
           (let [word (first (filter #(str/starts-with? string %) dictionary))]
             (if word
               (do
                 (.delete string 0 (count word))
                 (reset! num (+' (*' @num (count dictionary)) (.indexOf dictionary word))))
               (do
                 (.deleteCharAt string 0)
                 (reset! num (+' (*' @num (count cPage)) (str/index-of cPage c)))))
             (reset! num (+' (*' @num 8) (<< (if word 1 0) 2) (<< (if isSpace 1 0) 1) (if isUpper 1 0)))))))
     ; Confirm compression was successful
     (let [compressed (str/join (map #(get codepage %) (toBijectiveBase (deref num) (count codepage))))
           decompressed (decompress compressed codepage compressableChars dictionary)]
       (if (not= s decompressed)
         (throw (IllegalStateException.
                  (format "Compression failed. Compressed: '%s', original: '%s', decompressed: '%s'" compressed s decompressed)))
         compressed)))))

(defn -main [] (println (compress "Hello, World!")))
