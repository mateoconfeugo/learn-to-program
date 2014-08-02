(ns learn-clojure.handler
  (:require [learn-clojure.config :refer [config]]
            [compojure.handler :as handler :refer [site]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]
            [learn-clojure.routes :refer [all-routes]])
  (:gen-class))

(defn init []  (println "The baseline app is starting"))
(defn destroy []  (println "The baseline app has been shut down"))

(defn wrap-add-anti-forgery-cookie
  "Mimics code in Shoreleave-baseline's customized ring-anti-forgery middleware."
  [handler & [opts]]
  (fn [request]
     (let [response (handler request)]
       (if-let [token (-> request :session (get "__anti-forgery-token"))]
         (assoc-in response [:cookies "__anti-forgery-token"] token)
         response))))

(defn get-handler
  "wrap app with ring extensions"
  [app]
  (-> app
      wrap-rpc
      wrap-add-anti-forgery-cookie
      wrap-anti-forgery
      wrap-gzip
      (handler/site {:session {:cookie-name (config :cookie-name)
                               :store (cookie-store {:key (config :session-secret)})
                               :cookie-attrs {:max-age (config :session-max-age-seconds)
                                              :http-only true}}})
      wrap-file-info))

(def app (handler/site all-routes))
(def request-handler (get-handler app))
