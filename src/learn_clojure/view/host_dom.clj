(ns learn-clojure.view.host-dom
  (:require [learn-clojure.view.templates :refer [index-page]]))

(defn render-str [t] (apply str t))

(defn render-dom
  [{:keys [pages menu] :as args}]
  (index-page {:site-name "Clojure Lunch and Learn" :pages nil :menu-data menu}))
