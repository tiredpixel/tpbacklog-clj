(ns tpbacklog.db
  (:require [taoensso.carmine :as car :refer (wcar)]
            [cheshire.core :as json]))

(def CONN {:pool {} :spec {:uri (System/getenv "REDIS_URL")}})

(def REDIS_NS (System/getenv "REDIS_NS"))

(def PATH_NS "tpbacklog")

; Connection

(defmacro wcar* [& body] `(car/wcar CONN ~@body))

; Records

(defn- pickle-rec [rec]
  (json/generate-string rec))

(defn- unpickle-rec [rec-str]
  (json/parse-string rec-str true))

; Paths

(defn- make-path [segments]
  (clojure.string/join ":" segments))

(defn- path-ns []
  (make-path (remove empty? [REDIS_NS PATH_NS])))

(defn- path-seq [subspace]
  (make-path [(path-ns) subspace "seq"]))

(defn- path-rec [subspace id]
  (make-path [(path-ns) subspace (Integer/parseInt (str id))]))

; Operations

(defn next-id [subspace]
  (wcar* (car/incr (path-seq subspace))))

(defn set-rec [subspace id rec]
  (let [path (path-rec subspace id)]
    (wcar* (car/set path (pickle-rec rec)))))

(defn get-rec [subspace id]
  (let [path (path-rec subspace id)
        rec (wcar* (car/get path))]
    (unpickle-rec rec)))

(defn del-rec [subspace id]
  (let [path (path-rec subspace id)]
    (wcar* (car/del path))))
