(ns learn-to-program.views.host-dom
  (:require [net.cgrand.enlive-html :refer [deftemplate clone-for content add-class set-attr do->
                                            html-content first-child nth-of-type]]
            [learn-to-program.views.snippets :refer [nav-bar]]        
            [cms.site :refer [new-cms-site get-site-menu get-site-contents]]
            [flourish-common.web-page-utils :refer [render-to-response]]))

(comment
  (def-landing-site "templates/index.html")
  ;; TODO: Make this generic so we can have all sorts of customizable landing pages
(defn map-elements-into-dom
  [{:keys [:template :settings :mappings]}]
  "maps the user configured landing site components into the dom declared by the template"))

(deftemplate index-with-webapp-pages "templates/index.html"
  [{:keys [site-name pages menu-data] :as settings}]
;;  (map-elements-into-dom {:template "templates/index.html" :settings settings}
  [:div#navbar] (content (nav-bar {:title site-name :menu-data menu-data}))
;;  [: (nth-of-type 1)]  (html/content (nav-bar {:title site-name :menu-data menu-data}))
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
  [[:.tab-pane first-child]] (add-class "active"))

(defn render
  [cms]
  "Take the sequence of pages in insert them into an unordered list"
  (let [pages (get-site-contents cms)
        menu (:drop_down_menu (first (get-site-menu cms)))        
        num_pages (count pages)
        page_num (range 0 num_pages)
        pages (reverse (map #(assoc %1 :order %2)  pages page_num))]
    (render-to-response (index-with-webapp-pages {:site-name "MarketWithGusto.com" :pages pages :menu-data menu}))))
