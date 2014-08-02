(ns learn-clojure.view.editor
  (:require [net.cgrand.enlive-html :refer [deftemplate defsnippet content html-content]]))

(def ^:dynamic *top-navbar-sel* [:div.navbar.navbar-inverse.navbar-fixed-top :div.navbar-inner])
(defsnippet top-navbar "templates/html/edit/top_nav_bar.html" *top-navbar-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *workspace-sel* [:#drop-zone])
(defsnippet workspace "templates/html/edit/workspace.html" *workspace-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *download-modal-sel* [:div#downloadModal :div])
(defsnippet download-modal "templates/html/edit/download_modal.html" *download-modal-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *share-modal-sel* [:div#shareModal :div])
(defsnippet share-modal "templates/html/edit/share_modal.html" *share-modal-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *feedback-modal-sel* [:div#feedbackModal :div])
(defsnippet feedback-modal "templates/html/edit/feedback_modal.html" *feedback-modal-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *grid-system*  [:ul.nav.nav-list.accordion-group])
(defsnippet grid-system "templates/html/edit/grid_system.html" *grid-system*
  [{:keys[] :as settings}])

(def ^:dynamic *css-base*  [:ul.nav.nav-list.accordion-group])
(defsnippet css-base "templates/html/edit/css_base.html" *css-base*
  [{:keys[] :as settings}])

(def ^:dynamic *components*  [:ul.nav.nav-list.accordion-group])
(defsnippet components "templates/html/edit/components.html" *components*
  [{:keys[] :as settings}])

(def ^:dynamic *javascript-sel* [:ul.nav.nav-list.accordion-group])
(defsnippet javascript-components "templates/html/edit/javascript_components.html" *javascript-sel*
  [{:keys[] :as settings}])

(def ^:dynamic *sidebar-nav-sel* [:div.sidebar-nav])
(defsnippet sidebar-nav "templates/html/edit/sidebar_nav.html" *sidebar-nav-sel*
  [{:keys[] :as settings}]
  [:div#grid-system] (content (grid-system settings))
  [:div#css-base] (content (css-base settings))
  [:div#components] (content (components settings))
  [:div#javascript-components] (content (javascript-components settings)))

(deftemplate editor "templates/html/edit/editor.html"
  [{:keys [] :as settings}]
  [:div.navbar.navbar-inverse.navbar-fixed-top] (content (top-navbar settings))
  [:div.container-fluid :div.row-fluid :div] (content (sidebar-nav settings))
  [:div.demo] (content (workspace settings))
  [:div#downloadModal] (content (download-modal settings))
  [:div#shareModal] (content (share-modal settings))
  [:div#feedbackModal] (content (feedback-modal settings)))
