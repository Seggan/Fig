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
        result (transient [])]
    (while (> @i 0)
      (swap! i dec')
      (conj! result (divmod! i base)))
    (reverse (persistent! result))))

(defn fromBijectiveBase [list base] (reduce (fn [acc i] (+' (*' acc base) (inc' i))) 0 list))

(defn- applyIf [cond f arg] (if cond (f arg) arg))

(defn- lastBitSet [n] (> (bit-and n 1) 0))

(defn decompress
  ([s] (decompress s compressionCodepage compressableChars dictionary))
  ([s codepage cPage dictionary]
   (let [result (transient [])
         num (atom (fromBijectiveBase (map #(str/index-of codepage %) s) (count codepage)))]
     (while (> @num 0)
       (let [tag (long (divmod! num 8))]
         (conj! result (if (= (bit-and tag 2r100) 0)
                         (->> (count cPage) (divmod! num) (get cPage) (applyIf (lastBitSet tag) chars/toUpperCase))
                         (->> (count dictionary) (divmod! num) (get dictionary) (applyIf (lastBitSet tag) str/capitalize))))
         (if (> (bit-and tag 2r010) 0) (conj! result \space))))
     (str/join (reverse (persistent! result))))))


(defn -main [] (println (decompress "Hey" compressionCodepage compressableChars dictionary)))
