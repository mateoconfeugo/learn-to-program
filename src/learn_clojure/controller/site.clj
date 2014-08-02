(ns learn-clojure.controller.site
  (:require [compojure.core :refer [defroutes GET]]
            [cheshire.core :refer [parse-stream]]
            [learn-clojure.config :refer [config]]
            [learn-clojure.view.host-dom :refer [render-dom]]))

(def site-data {:pages (:pages config)
                :menu (parse-stream (clojure.java.io/reader "resources/menu.json") true)})

(defroutes site-routes
  (GET "/" [] (render-dom site-data)))
