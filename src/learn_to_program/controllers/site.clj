(ns learn-to-program.controllers.site
  (:require [cms.site :refer [new-cms-site]]
            [compojure.core]
            [compojure.route :as route :refer [not-found files resources]]
            [flourish-common.config :refer [read-config new-config]]            
            [ring.util.response :refer [file-response]]
            [learn-to-program.views.host-dom :as host :refer [render]]
            [learn-to-program.views.static-page :refer [render-static-page]]))

(def root-dir (str (System/getProperty "user.dir") "/website"))
(def cfg-dir (str (System/getProperty "user.dir") "/website/config"))
(def static-html-dir (str (System/getProperty "user.dir") "/resources/public/html/articles/"))
(def cfg (read-config (new-config {:cfg-file-path (str cfg-dir "/site-config.json")})))
(def token-type (-> cfg :website :site-cfg-dir-path))
(def token (-> cfg :website :market_vector))
(def site-tag (-> cfg :website :site-tag))
(def site-name (-> cfg :website :site-name))

(def cms (new-cms-site {:webdir root-dir :market-vector-id token :domain-name}))
;;(def file "landing-site-builder.html")
;;  (GET "/site/articles/:file" [file] (render-static-page (str static-html-dir file) cms token))
;;  (route/files "/" {:root (str root-dir "/site/articles")})
;;    (route/files "/" {:root "public"})

(defroutes site-routes
  (GET "/home" [] (learn-to-program.views.host-dom/render cms))
  (route/resources "/")
  (route/not-found "Not Found"))




