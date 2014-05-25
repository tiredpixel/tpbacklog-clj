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

(defn- path-idx [subspace]
  "Makes a storage index path."
  (make-path [(path-ns) subspace "idx"]))

(defn- path-rec [subspace id]
  "Makes a storage record path."
  {:pre [(integer? id)]}
  (make-path [(path-ns) subspace id]))

; Operations

(defn next-id [subspace]
  "Allocates and returns a new unique id within a subspace."
  (wcar* (car/incr (path-seq subspace))))

(defn set-rec [subspace id rec]
  "Sets a record by id within a subspace."
  {:pre [(integer? id)]}
  (let [path-r (path-rec subspace id)
        path-i (path-idx subspace)]
    (wcar*
      (car/set path-r (pickle-rec rec))
      (car/zadd path-i (rec :priority) id))))

(defn get-rec [subspace id]
  "Gets a record by id within a subspace."
    {:pre [(integer? id)]}
    (let [path-r (path-rec subspace id)
          rec (wcar* (car/get path-r))]
      (unpickle-rec rec)))

(defn get-recs [subspace & {:keys [max-points]}]
  "Gets records within a subspace."
  (let [path-i (path-idx subspace)
        rec-ids (wcar* (car/zrange path-i 0 -1))]
    (loop [[rec-id & rec-ids-t] rec-ids
            points 0
            recs []]
      (if (empty? rec-id)
        recs
        (let [rec (get-rec subspace (Integer/parseInt rec-id))
              points2 (+ (rec :points) points)]
          (if (and max-points (> points2 max-points))
            recs
            (recur rec-ids-t points2 (conj recs rec))))))))

(defn del-rec [subspace id]
  "Deletes a record by id within a subspace."
  {:pre [(integer? id)]}
  (let [path-r (path-rec subspace id)
        path-i (path-idx subspace)]
    (wcar*
      (car/del path-r)
      (car/zrem path-i id))))
