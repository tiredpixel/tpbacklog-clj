(defproject tpbacklog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-jetty-adapter "1.3.0-RC1"]
                 [ring/ring-json "0.3.1"]
                 [cheshire "5.3.1"]
                 [com.taoensso/carmine "2.6.2"]]
  :plugins [[lein-ring "0.8.10"]]
  :main ^:skip-aot tpbacklog.core
  :ring {:handler tpbacklog.handler/app}
  :profiles {:uberjar {:aot :all}
            :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                 [ring-mock "0.1.5"]]}})
