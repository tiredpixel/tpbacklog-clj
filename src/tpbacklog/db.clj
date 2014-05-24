(ns tpbacklog.db
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def CONN {:pool {} :spec {:uri (System/getenv "REDIS_URL")}})

(defmacro wcar* [& body] `(car/wcar CONN ~@body))
