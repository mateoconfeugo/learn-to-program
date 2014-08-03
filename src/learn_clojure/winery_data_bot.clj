(ns learn-clojure.winery-data-bot
  (:require [clojure.core.async  :refer [put! >! chan go <!! alts!]]
            [clojure.pprint :refer [pprint]]
            [clojure.edn]
            [com.ashafa.clutch :as clutch]
            [net.cgrand.enlive-html :as html]
            [clojure.core.match :refer [match]]
            [clojurewerkz.urly.core :refer [query-of]]))

(def base-uri "http://pasowine.com/wineries")

(def winery-selector [:table#winerylist :td :a])

(defn fetch-uri [uri]
  (html/html-resource (java.net.URI. uri)))

(defn internal-winery-uri-extractor [winery]
  (format "%s/%s" base-uri (-> winery :attrs :href)))

(defn fetch-winery-list [uri]
  "Extract the list of wineries contain each wineries link"
  (map #(internal-winery-uri-extractor %)  (html/select (fetch-uri uri) winery-selector)))

(defn winery-profile-extractor  [dom]
  (-> (first  (html/select dom [:div#profile_content :p])) :content ))

(defn winery-uri-extractor
  "Get the third link tag contents"
  [dom]
  (-> (nth  (html/select dom [:div#profile_rightbar :p  :a]) 3) :content first))

(defn wineries-doc-db
  [region]
  (clutch/get-database (format "wine-%s" region)))

(defn extract-winery-data
  "Extract the profile as well as the url to the wineries web site "
  [{:keys [dom winery-id] :as settings}]
  (let [ p (html/texts (winery-profile-extractor dom))
        ;; First look for the italized "Website" text then look for the very next anchor tag then extract the address.
        node (html/select dom { [[ :i (html/pred #(.contains (html/text %) "Website"))]]  [[:a (html/nth-of-type 1)]]})
        u (first (:content (nth (first node) 2)))]
    {:profile p
     :uri u
     :winery-id winery-id}))

(defn mine
  "Contains the control flow logic for the asynchronous aspects of the program"
  [{:keys [uri region] :as settings} ]
  (let [doc-db (wineries-doc-db region)
        ;; CHANNELS
        retreive-channel (chan)
        extract-channel (chan)
        store-channel (chan)
        channels [retreive-channel extract-channel store-channel]
        ;; LAMBDA WIRING
        retreive-fn (fn [uri]
                      (let [id  (nth (first (re-seq #"winery=(\d+)" (query-of uri))) 1)
                            d (fetch-uri uri)
                            msg  {:dom d :winery-id id}]
                        (go (>! extract-channel [:extract msg]))))

        extract-fn (fn [data] (go (>! store-channel [:store (extract-winery-data data)])))
;;        store-fn  (fn [data]  (clutch/with-db doc-db (clutch/put-document data)))
        store-fn  (fn [data]  (pprint data))
        ;; DISTPATCH TABLE
        dispatcher (fn [ch tuple]
                     (let [[msg-token data] (take 2 tuple)]
                       (match [msg-token]
                              [:retreive] (retreive-fn data)
                              [:extract] (extract-fn  data)
                              [:store] (store-fn data))))]
    (do
      ;; PUT CHANNEL EVENTS INTO DISPATCHER
      (go (while true
            (let [[val ch] (alts! channels)]
              (dispatcher ch val))))
      ;; START PUSHING INPUT INTO SYSTEM
      (map #(go (>! retreive-channel [:retreive %]))  (fetch-winery-list uri)) )))
