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
  "Pickles a record for to storage."
  (json/generate-string rec))

(defn- unpickle-rec [rec-str]
  "Unpickles a record for from storage."
  (json/parse-string rec-str true))

; Paths

(defn- make-path [segments]
  "Makes a storage path from segments."
  (clojure.string/join ":" segments))

(defn- path-ns []
  "Makes a storage namespace path."
  (make-path (remove empty? [REDIS_NS PATH_NS])))

(defn- path-seq [subspace]
  "Makes a storage sequence path."
  (make-path [(path-ns) subspace "seq"]))

(defn- path-rec [subspace id]
  "Makes a storage record path."
  {:pre [(integer? id)]}
  (make-path [(path-ns) subspace id]))

; Operations

(defn next-id [subspace]
  "Allocates and returns a new unique id within a subspace."
  {:post [(integer? %)]}
  (wcar* (car/incr (path-seq subspace))))

(defn set-rec [subspace id rec]
  "Sets a record by id within a subspace."
  {:pre [(integer? id)]}
  (let [path (path-rec subspace id)]
    (wcar* (car/set path (pickle-rec rec)))))

(defn get-rec [subspace id]
  "Gets a record by id within a subspace."
  {:pre [(integer? id)]}
  (let [path (path-rec subspace id)
        rec (wcar* (car/get path))]
    (unpickle-rec rec)))

(defn del-rec [subspace id]
  "Deletes a record by id within a subspace."
  {:pre [(integer? id)]}
  (let [path (path-rec subspace id)]
    (wcar* (car/del path))))
