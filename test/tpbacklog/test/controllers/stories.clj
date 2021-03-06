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

(def STORY_VALID {:points 8 :priority 1 :title "Style the icon to be more pink."})

(defn- seed-story [story]
  (let [response (app (request :post "/stories" story))]
    ((response :headers) "Location")))

; Test r-readall

(deftest test-r-readall-non-empty
  (let [story1 (assoc STORY_VALID :priority 1 :points 7)
        story2 (assoc STORY_VALID :priority 2 :points 11)
        story3 (assoc STORY_VALID :priority 3 :points 13)
        location3 (seed-story story3) ; order-dependent
        location1 (seed-story story1)
        location2 (seed-story story2)]
    (testing "non-empty"
      (let [response (app (request :get "/stories"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body [story1 story2 story3]))))
    (testing "non-empty points none"
      (let [response (app (request :get "/stories?points=5"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body []))))
    (testing "non-empty points one"
      (let [response (app (request :get "/stories?points=7"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body [story1]))))
    (testing "non-empty points one mid"
      (let [response (app (request :get "/stories?points=17"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body [story1]))))
    (testing "non-empty points two"
      (let [response (app (request :get "/stories?points=18"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body [story1 story2]))))
    (testing "non-empty points all high"
      (let [response (app (request :get "/stories?points=1729"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 200))
        (is (= body [story1 story2 story3]))))
    (testing "invalid points -ve"
      (let [response (app (request :get "/stories?points=-1"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 400))
        (is (empty? body))))
    (testing "invalid points 0"
      (let [response (app (request :get "/stories?points=0"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 400))
        (is (empty? body))))
    (testing "invalid points non-number"
      (let [response (app (request :get "/stories?points=X"))
            body (json/parse-string (response :body) true)]
        (is (= (response :status) 400))
        (is (empty? body))))))

(deftest test-r-readall-empty
  (testing "empty"
    (let [response (app (request :get "/stories"))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 200))
      (is (= body [])))))

; Test r-create

(defn- test-r-create-invalid [story]
  (let [response (app (request :post "/stories" story))]
    (is (= (response :status) 400))))

(deftest test-r-create
  (testing "valid"
    (let [story STORY_VALID
          response (app (request :post "/stories" story))]
      (is (= (response :status) 201))
      (is (re-matches #"/stories/\d+" ((response :headers) "Location")))))
  (testing "invalid empty"
    (test-r-create-invalid {}))
  (testing "invalid points non-number"
    (test-r-create-invalid (assoc STORY_VALID :points "X")))
  (testing "invalid priority non-integer"
    (test-r-create-invalid (assoc STORY_VALID :priority 1.0)))
  (testing "invalid priority low-range"
    (test-r-create-invalid (assoc STORY_VALID :priority 0)))
  (testing "invalid priority high-range"
    (test-r-create-invalid (assoc STORY_VALID :priority 6)))
  (testing "invalid title missing"
    (test-r-create-invalid (dissoc STORY_VALID :title))))

; Test r-read

(defn- test-r-read-not-found [location]
  (let [response (app (request :get location))
        body (json/parse-string (response :body) true)]
    (is (= (response :status) 404))))

(deftest test-r-read
  (testing "found"
    (let [story STORY_VALID
          location (seed-story story)
          response (app (request :get location))
          body (json/parse-string (response :body) true)]
      (is (= (response :status) 200))
      (is (= body story))))
  (testing "not-found integer"
    (test-r-read-not-found "/stories/666666666"))
  (testing "not-found non-integer"
    (test-r-read-not-found "/stories/X")))

; Test r-update

(defn- test-r-update-invalid [story2]
  (let [story STORY_VALID
        location (seed-story story)
        response (app (request :put location story2))]
    (is (= (response :status) 400))
    (let [response2 (app (request :get location))
          body2 (json/parse-string (response2 :body) true)]
      (is (= body2 story)))))

(defn- test-r-update-not-found [location]
  (let [story STORY_VALID
        response (app (request :put location story))
        body (json/parse-string (response :body) true)]
    (is (= (response :status) 404))))

(deftest test-r-update
  (testing "valid"
    (let [story STORY_VALID
          story2 (assoc STORY_VALID :title "Make everything really pink.")
          location (seed-story story)
          response (app (request :put location story2))]
      (is (= (response :status) 204))
      (let [response2 (app (request :get location))
            body2 (json/parse-string (response2 :body) true)]
        (is (= (response2 :status) 200))
        (is (= body2 story2)))))
  (testing "not-found integer"
    (test-r-update-not-found "/stories/666666666"))
  (testing "not-found non-integer"
    (test-r-update-not-found "/stories/X"))
  (testing "invalid empty"
    (test-r-update-invalid {}))
  (testing "invalid points non-number"
    (test-r-update-invalid (assoc STORY_VALID :points "X")))
  (testing "invalid priority non-integer"
    (test-r-update-invalid (assoc STORY_VALID :priority 1.0)))
  (testing "invalid priority high-range"
    (test-r-update-invalid (assoc STORY_VALID :priority 6)))
  (testing "invalid priority low-range"
    (test-r-update-invalid (assoc STORY_VALID :priority 0)))
  (testing "invalid title missing"
    (test-r-update-invalid (dissoc STORY_VALID :title))))

; Test r-delete

(defn- test-r-delete-not-found [location]
  (let [response (app (request :delete location))]
    (is (= (response :status) 404))))

(deftest test-r-delete
  (testing "found"
    (let [story STORY_VALID
          location (seed-story story)
          response (app (request :delete location))]
      (is (= (response :status) 204))))
  (testing "not-found integer"
    (test-r-delete-not-found "/stories/666666666"))
  (testing "not-found non-integer"
    (test-r-delete-not-found "/stories/X")))
