(ns learn-to-program.handler
  "Top level request handler for the web application"
  (:require [compojure.handler :as handler]
            [compojure.core :as core :refer [routes]]
            [compojure.route :as route :refer [resources files not-found]]
            [ring.adapter.jetty :as ring :refer [run-jetty]]
            [learn-to-program.controllers.site  :refer [site-routes]]))
  (:gen-class))

(def app-routes  (core/routes
                  site-routes
                  (route/resources "/")
                  (route/files "/" {:root "public"})
                  (route/not-found "Not Found")))

(def app (handler/site app-routes))

(defn start-app [port]
  (ring/run-jetty app {:port port :join? false}))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8088"))]
    (start-app port)))
