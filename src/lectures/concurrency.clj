(ns learn-clojure.concurrency
  (:require [clojure.core.async  :refer [put! >! chan go <!! alts! <! >!! go-loop close! onto-chan into]]
            [clojure.pprint :refer [pprint]]
            [clojure.edn]
            [net.cgrand.enlive-html :as html]
            [clojure.core.match :refer [match]]
            [clojurewerkz.urly.core :refer [query-of]]))


(def ch (chan))

(go (let [x (<! ch)
          y (<! ch)]
      (println "sum: " (+ x y))))

(>!! ch 3)
(>!! ch 4)
(>!! ch 5)

(defn go-add [x y]
  (<!! (nth (iterate #(go (inc (<! %)))
                      (go x)) y)))


(time (go-add 10 10))

(defn map-chan [f from]
  (let [to (chan)]
    (go-loop []
      (when-let [x (<! from)]
        (>! to (f x))
        (close! to))
      to)))

(def ch (chan 10))
(def mapped (map-chan (partial * 2) ch))

(onto-chan ch (range 0 10))
(<!! (clojure.core.async/into [] mapped))
