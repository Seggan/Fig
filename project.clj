(defproject fig "1.5.0"
  :description "A functional golfing language"
  :url "http://github.com/Seggan/Fig"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.10.3"] [ch.obermuhlner/big-math "2.3.0"] [org.clojure/math.numeric-tower "0.0.5"]]
  :main ^:skip-aot fig.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
