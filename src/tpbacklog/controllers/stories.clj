(ns tpbacklog.controllers.stories
  (:use [compojure.core :only (defroutes POST GET PUT DELETE)])
  (:require [tpbacklog.db :as db]))

(def DB_SUBSPACE "stories")

(defn- to-int [n]
  "Utility function to ensure string is converted to integer. This helps with
  handling requests containing params in the URL vs request body, as these could
  arrive as strings or integers respectively."
  (if (integer? n) n (Integer/parseInt n)))

(defn- r-create [points priority title]
  {:pre [(integer? points)
         (integer? priority)
         (>= priority 1)
         (<= priority 5)
         (not (nil? title))]
   :post [(integer? %)]}
  (let [id (db/next-id DB_SUBSPACE)
        story {:points points :priority priority :title title}]
    (db/set-rec DB_SUBSPACE id story)
    id))

(defn- r-read [id]
  {:pre [(integer? id)]
   :post [(not (nil? %))]}
  (db/get-rec DB_SUBSPACE id))

(defn- r-update [id points priority title]
  {:pre [(integer? id)
         (integer? points)
         (integer? priority)
         (>= priority 1)
         (<= priority 5)
         (not (nil? title))]}
  (let [story {:points points :priority priority :title title}]
    (db/set-rec DB_SUBSPACE id story)))

(defn- r-delete [id]
  {:pre [(integer? id)]}
  (db/del-rec DB_SUBSPACE id))

(defroutes routes
  (POST "/stories" [points priority title]
    (try
      (let [points (to-int points)
            priority (to-int priority)
            id (r-create points priority title)]
        {:status 201
         :headers {
           "Location" (str "/stories/" id)}})
      (catch java.lang.NumberFormatException e {:status 400}) ; story invalid
      (catch java.lang.AssertionError e {:status 400}))) ; story invalid
  (GET "/stories/:id" [id]
    (try
      (let [id (to-int id)
            story (r-read id)]
        {:status 200 :body story})
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404}))) ; id invalid
  (PUT "/stories/:id" [id points priority title]
    (try
      (let [id (to-int id)]
        (r-read id) ; check exists
        (try
          (let [points (to-int points)
                priority (to-int priority)]
            (r-update id points priority title)
            {:status 204})
          (catch java.lang.NumberFormatException e {:status 400}) ; story invalid
          (catch java.lang.AssertionError e {:status 400}))) ; story invalid
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404}))) ; id invalid
  (DELETE "/stories/:id" [id]
    (try
      (let [id (to-int id)]
        (r-read id) ; check exists
        (r-delete id)
        {:status 204})
      (catch java.lang.NumberFormatException e {:status 404}) ; id invalid
      (catch java.lang.AssertionError e {:status 404})))) ; id invalid
