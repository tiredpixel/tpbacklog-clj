(ns tpbacklog.test.handler
  (:use clojure.test
        ring.mock.request  
        tpbacklog.handler)
  (:require [cheshire.core :as json]))

(deftest test-app
  (testing "statics"
    (testing "home"
      (let [response (app (request :get "/"))
            body (json/parse-string (response :body) true)]
        (println body)
        (is (= (response :status) 200))
        (is (= (body :service) "tpbacklog"))
        (is (integer? (body :version)))
        (is (java.util.Date. (body :time)))
        (is (string? (body :msg))))))
  
  (testing "not-found"
    (let [response (app (request :get "/invalid"))]
      (is (= (response :status) 404)))))
