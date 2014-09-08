(ns learn-clojure.config
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io :refer [resource]]
            [shoreleave.server-helpers :refer [safe-read]])
  (:gen-class))

;; Configuration
(defn read-config
  "Read a config file and return it as Clojure Data.  Usually, this is a hashmap"
  ([]
     (read-config (.getFile (io/resource "config.edn"))))
  ([config-loc]
     (safe-read (slurp config-loc))))

(def config (read-config))
