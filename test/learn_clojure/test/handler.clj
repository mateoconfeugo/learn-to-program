(ns learn-clojure.test.handler
  (:require [cheshire.core :refer :all]
            [clojure.test :refer [is use-fixtures deftest successful? run-tests]]
            ;;            [clj-webdriver.taxi :as scraper :refer [set-driver! to click exists? input-text submit quit page-source get-url]]
            [expectations :refer [expect]]
            [plumbing.core :refer :all]
            [plumbing.graph :as graph]
            [learn-clojure.handler :refer :all]
            [clojure.pprint :refer [pprint]]))

;; SETUP
(def test-data-graph
  {:app-uri (fnk []  "http://localhost:3000")
   :menu-data (fnk  [](:drop_down_menu (parse-stream (clojure.java.io/reader "resources/menu.json") true)))
   ;;   :index-html (fnk [menu-data] (render-dom {:pages (:pages config) :menu menu-data }))
   ;;   :nav-dom (fnk [menu-data] (nav-bar {:title "ham" :menu-data menu-data}) )
   })

(def test-fixture-fn (graph/eager-compile test-data-graph))

(pprint (:menu-data (test-fixture-fn nil)))
(pprint (:nav-dom (test-fixture-fn nil)))
(pprint (:index-html (test-fixture-fn nil)))

(defn one-time-setup [] (println "one time setup"))
(defn one-time-teardown [] (println "one time teardown"))

(defn once-fixture [f]
  (one-time-setup)
  (f)
  (one-time-teardown))

(use-fixtures :once once-fixture test-fixture-fn)

(:index-html (test-fixture-fn {}))

(deftest  site-renders-test
  (let [tf (test-fixture-fn {})]
    (:result @(future ()))))

(expect true (successful? site-renders-test))
