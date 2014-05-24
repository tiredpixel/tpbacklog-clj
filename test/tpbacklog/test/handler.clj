(ns tpbacklog.test.handler
  (:use clojure.test
        ring.mock.request  
        tpbacklog.handler)
  (:require [cheshire.core :as json]))

(deftest test-app
  (testing "not-found"
    (let [response (app (request :get "/invalid"))]
      (is (= (response :status) 404)))))
