(ns learn-clojure.routes
  (:require [compojure.core :refer [defroutes GET routes]]
            [compojure.route :refer [resources not-found]]
            [shoreleave.middleware.rpc :refer [remote-ns]]
            [learn-clojure.controller.api]
            [learn-clojure.controller.site :refer [site-routes]]))

(remote-ns 'learn-clojure.controller.api :as "api")

(defroutes app-routes
;;    (resources "/app.js" {:root "../war/javascripts/main.js"})
  (resources "/design/" {:root "templates/html"})
  (resources "/literate/" {:root "src_docs"})
  (resources "/design/css/" {:root "public/css"})
  (resources "/css/" {:root "public/css"})
  (resources "/js/" {:root "public/js"})
  (not-found "404 Page not found."))

(def all-routes (routes site-routes app-routes))
