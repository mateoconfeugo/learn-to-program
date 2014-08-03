(ns learn-clojure.controller.api
  (:require [learn-clojure.winery-data-bot :refer [mine]]))

(def base-uri "http://pasowine.com/wineries")

(defn mine-wine-data-from-paso
  "gets most of the relevant information about the various wines in paso robles"
  []
  (do
    (mine {:uri base-uri :region "paso-robles"})
      {:results []
       :message "blah"}))
