(defproject misoi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [seesaw "1.4.4"]
                 [incanter/incanter-core "1.5.5"]
                 [incanter/incanter-charts "1.5.5"]
                 [incanter/incanter-processing "1.3.0"]]
  :main ^:skip-aot misoi.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
