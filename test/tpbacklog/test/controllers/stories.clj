(ns tpbacklog.test.controllers.stories
  (:use clojure.test
        ring.mock.request  
        tpbacklog.handler)
  (:require [cheshire.core :as json]
            [taoensso.carmine :as car]
            [tpbacklog.db :as db]))

(use-fixtures :each
  (fn [tests]
    (db/wcar* (car/flushdb)) ; beware! redis db gets flushed
    (tests)))

(def STORY_VALID {:points 8 :priority 1 :title "Style the iPlayer icon to be more pink."})

(defn- seed-story [story]
  (let [response (app (request :post "/stories" STORY_VALID))]
    ((response :headers) "Location")))

(deftest test-r-create
  (testing "valid"
    (let [story STORY_VALID
          response (app (request :post "/stories" story))]
      (is (= (response :status) 201))
      (is (re-matches #"/stories/\d+" ((response :headers) "Location")))))
  (testing "invalid empty"
    (let [story {}
          response (app (request :post "/stories" story))]
      (is (= (response :status) 400))))
  (testing "invalid points non-number"
    (let [story (assoc STORY_VALID :points "X")
          response (app (request :post "/stories" story))]
      (is (= (response :status) 400))))
  (testing "invalid priority non-integer"
    (let [story (assoc STORY_VALID :priority 1.0)
          response (app (request :post "/stories" story))]
      (is (= (response :status) 400))))
  (testing "invalid title missing"
    (let [story (dissoc STORY_VALID :title)
          response (app (request :post "/stories" story))]
      (is (= (response :status) 400))))
  )

(deftest test-r-read
  (testing "found"
    (let [story STORY_VALID
          location (seed-story story)
          response (app (request :get location))
          body (json/parse-string (response :body) true)]
      (is (= body story))))
  (testing "not-found integer"
    (let [location "/stories/666666666"
          response (app (request :get location))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 404))))
  (testing "not-found non-integer"
    (let [location "/stories/X"
          response (app (request :get location))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 404)))))

(deftest test-r-update
  (testing "valid"
    (let [story STORY_VALID
          story2 (assoc STORY_VALID :title "Make everything really pink.")
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 204))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= body2 story2)))))
  (testing "not-found integer"
    (let [story STORY_VALID
          location "/stories/666666666"
          response (app (request :put location story))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 404))))
  (testing "not-found non-integer"
    (let [story STORY_VALID
          location "/stories/X"
          response (app (request :put location story))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 404))))
  (testing "invalid empty"
    (let [story STORY_VALID
          story2 {}
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 400))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= body2 story)))))
  (testing "invalid points non-number"
    (let [story STORY_VALID
          story2 (assoc STORY_VALID :points "X")
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 400))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= body2 story)))))
  (testing "invalid priority non-integer"
    (let [story STORY_VALID
          story2 (assoc STORY_VALID :priority 1.0)
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 400))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= body2 story)))))
  (testing "invalid title missing"
    (let [story STORY_VALID
          story2 (dissoc STORY_VALID :title)
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 400))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= body2 story)))))
  )

(deftest test-r-delete
  (testing "found"
    (let [story STORY_VALID
          location (seed-story story)
          response (app (request :delete location))]
      (is (= (response :status) 204))))
  (testing "not-found integer"
    (let [location "/stories/666666666"
          response (app (request :delete location))]
      (is (= (response :status) 404))))
  (testing "not-found non-integer"
    (let [location "/stories/X"
          response (app (request :delete location))]
      (is (= (response :status) 404))))
  )
