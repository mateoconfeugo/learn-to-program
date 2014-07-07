(ns learn-clojure.handler
  (:require [net.cgrand.enlive-html :refer [deftemplate clone-for content add-class set-attr do->
                                            html-content first-child nth-of-type defsnippet]]
            [cheshire.core :refer :all]
            [compojure.handler :as handler]
            [compojure.core :as core :refer [routes defroutes GET]]
            [compojure.route :as c-route :refer [resources files not-found]]
            [ring.adapter.jetty :as ring :refer [run-jetty]]
            [ring.util.response :refer [file-response response]]
            [shoreleave.server-helpers :refer [safe-read]])
  (:gen-class))

;; Configuration
(defn read-config
  "Read a config file and return it as Clojure Data.  Usually, this is a hashmap"
  ([]
     (read-config (str (System/getProperty "user.dir") "/resources/config.edn")))
  ([config-loc]
     (safe-read (slurp config-loc))))

(def config (read-config))

;; HTML View Pieces
(defsnippet site-nav-header "templates/html/site-nav-header.html" [:nav.container-fluid]
  [site-name]
  [:a.brand] (content site-name))

(defsnippet menu-item-model "templates/html/site-nav-header.html" [[:ul.dropdown-menu (nth-of-type 1)] :> first-child]
  [item]
  [:a] (do->
        (content (:menu_item_text item))
        (set-attr :href (:menu_item_url item))))

(defsnippet menu-model "templates/html/site-nav-header.html" [:li.dropdown]
  [{:keys [drop_down_menu_name  menu_item]} model]
  [:.menu-header]   (content drop_down_menu_name)
  [:ul.dropdown-menu] (content (map model  menu_item)))

(defsnippet nav-bar  "templates/html/site-nav-header.html" [:nav.container-fluid]
  [{:keys [title menu-data]}]
  [:a.brand] (content title)
  [:ul#nav-bar-dropdown-menu] (content (map #(menu-model % menu-item-model)  menu-data)))

  (comment
  [:div#navbar] (content (nav-bar {:title site-name :menu-data menu-data}))
  [:ul.pages :li] (clone-for [p pages]
                             [:a] (do->
                                   (add-class "btn")
                                   (add-class "btn-success")
                                   (set-attr :href (str "#tab" (:order p)))
                                   (set-attr :data-toggle "tab")
                                   (content (:header p))))
  [:div.tab-content :section.tab-pane] (clone-for [p pages]
                                                  (do->
                                               (set-attr :id (str "tab" (:order p)))
                                               (html-content (:contents p))))
  [[:ul.pages (nth-of-type 1)] :> first-child] (add-class "active")
  [[:.tab-pane first-child]] (add-class "active")
  )




;; HTML DOM
(deftemplate index-page "templates/html/index.html"
  [{:keys [site-name pages menu-data] :as settings}]
  [:div#navbar] (content (nav-bar {:title site-name :menu-data menu-data})))

;; View
(defn render-str [t] (apply str t))
;;(def render-to-response (comp response render-str))

  ;;  (render-to-response (index-page {:site-name "MySiteNow!.com" :pages pages :menu-data menu}))

(defn render-dom
  [{:keys [pages menu] :as args}]
  (index-page {:site-name "MySiteNow!.com" :pages nil :menu-data menu}))

;; Controller/Router
(defroutes app-routes
  (GET "/" [] (render-dom {:pages (:pages config) :menu (:drop_down_menu (parse-stream (clojure.java.io/reader "resources/menu.json") true)) }))
  ;;  (GET "/" [] (render-dom ))
  (c-route/resources "/design/" {:root "templates/html"})
  (c-route/resources "/design/css/" {:root "public/css"})
  (c-route/resources "/css/" {:root "public/css"})
  (c-route/resources "/js/" {:root "public/js"})
  (c-route/not-found "404 Page not found."))

;; Ring Application
(def app (handler/site app-routes))

;; Jetty Server
(defn start-app [port]
  (ring/run-jetty app {:port port :join? false}))

;; Driver entry point
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "6088"))]
    (start-app port)))
