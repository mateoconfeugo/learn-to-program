(ns learn-clojure.server
  (:require [learn-clojure.handler :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start-app [port] (run-jetty app {:port port :join? false}))
(defn -main  [] (start-app (Integer/parseInt (or (System/getenv "PORT") "6088"))))

(comment
  (def app (-main))
  (.stop app)
  )
