(ns learn-to-program.controllers.site
  (:require [cms.site :refer :all]
            [cheshire.core :refer [parse-string parse-stream]]            
            [compojure.core :refer [defroutes GET routes]]
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
(def domain-name "foo")

(def test-cms (let [market-vector-id token
      dir (or root-dir (str root-dir "/" domain-name "/site"))
      ls-id (get-site-id root-dir  token)
      site-json (landing-site-json root-dir ls-id)]
  (reify CMS-Site
      (get-conversion-scripts [this] (:conversion_script site-json))
      (get-site-title [this] (:site_title site-json))
      (get-site-banner [this] (:landingsite_name site-json))
      (get-side-form [this] (cms-side-form dir ls-id))
      (get-modal-form [this] (cms-modal-form dir ls-id))
      (get-fonts [this] (cms-fonts dir ls-id))
      (get-header-image [this] (cms-header-image dir ls-id))
      (get-css [this] (cms-css dir ls-id))
      (get-site-contents [this] (populate-contents dir (assemble-site-files dir ls-id) market-vector-id))
      (get-site-menu [this] (:site_nav_header (first (:single_page_webapp (get-site-data dir  market-vector-id))))))))


;;(def cms (new-cms-site {:webdir root-dir :market-vector-id token :base-dir root-dir :domain-name "foo"}))
;;(def file "landing-site-builder.html")
;;  (GET "/site/articles/:file" [file] (render-static-page (str static-html-dir file) cms token))
;;  (route/files "/" {:root (str root-dir "/site/articles")})
;;    (route/files "/" {:root "public"})

(defroutes site-routes
  (GET "/home" [] (learn-to-program.views.host-dom/render test-cms)))





