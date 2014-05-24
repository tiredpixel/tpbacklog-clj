(ns tpbacklog.core
  (:require [ring.adapter.jetty :as jetty]
            [tpbacklog.handler :as handler])
  (:gen-class))

(defn -main [port]
  (jetty/run-jetty handler/app {:port (Integer. port) :join? false}))
