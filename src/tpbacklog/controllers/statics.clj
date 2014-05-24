(ns tpbacklog.controllers.statics
  (:use [compojure.core :only (defroutes GET)]))

(def API_VERSION 1)

(defn- r-home []
  {:body {:service "tpbacklog"
          :version API_VERSION
          :time (.getTime (java.util.Date.))
          :msg "Hello. Welcome to the tpbacklog service."}})

(defroutes routes
  (GET "/" [] (r-home)))
