(ns learn-clojure.driver
  (:require-macros [jayq.macros :refer [ready]])
  (:require [learn-clojure.editor :as edit :refer [new-async-app]]))

(defn main [& args]
  "Application driver attaches event handlers to elements and starts the application going"
  (do (edit/new-async-app :body)
      (js/initialize_legacy_editor)
      (.log js/console "client app starting")))

(ready (main))
