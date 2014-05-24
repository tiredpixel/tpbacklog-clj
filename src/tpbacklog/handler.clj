(ns tpbacklog.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware-json]
            [tpbacklog.controllers.statics :as statics]
            [tpbacklog.controllers.stories :as stories]))

(defroutes app-routes
  statics/routes
  stories/routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware-json/wrap-json-body)
      (middleware-json/wrap-json-params)
      (middleware-json/wrap-json-response)))
