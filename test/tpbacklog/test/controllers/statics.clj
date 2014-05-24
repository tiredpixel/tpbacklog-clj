(ns tpbacklog.test.controllers.statics
  (:use clojure.test
        ring.mock.request  
        tpbacklog.handler)
  (:require [cheshire.core :as json]))

(deftest test-home
  (testing "valid"
    (let [response (app (request :get "/"))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 200))
      (is (= (body :service) "tpbacklog"))
      (is (integer? (body :version)))
      (is (java.util.Date. (body :time)))
      (is (string? (body :msg))))))
