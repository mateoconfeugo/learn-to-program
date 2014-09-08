(ns learn-clojure.service.winery-data-bot
  ^ {:author "Matthew Burns"
     :doc "Contains the control flow logic for the asynchronous aspects of the program.
   The control logic of the program is decoupled and isolated by using the core.async
   go channels.
   The various logical piecies of the processing system have channels that serve
   as connection to each piece.
   A convention of sending a vector container a keyword token message  pair is used
   when inserting data into the channel system.
   This pair of information is used by the the dispatcher to route the correct data to
   correct handling function." }
  (:require [clojure.core.async  :refer [put! >! chan go <!! alts!]]
            [clojure.pprint :refer [pprint]]
            [clojure.edn]
            [com.ashafa.clutch :as clutch :refer [get-database with-db put-document]]
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
  (get-database (format "wine-%s" region)))

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

;;        store-fn  (fn [data]  (pprint data))

;; What are the advantages of using core async go channels?
;; Why do I need to have access to the channels in the dispatch tables (one of the reasons for the closure) couldn't there be
;; another channel that serves to route all the channels isn't that what alt! is already doing

(defn wire-up
  "Control logic of a screen scraper get raw data -> filter/transform data -> store data"
  [{:keys [uri region] :as settings} ]
  (let [doc-db (wineries-doc-db region)
        retreive-channel (chan)
        extract-channel (chan)
        store-channel (chan)]
    {:channels [retreive-channel extract-channel store-channel]
     :dispatcher (fn [ch tuple]
                   (let [[msg-token data] (take 2 tuple)]
                     ;; notice functions created and called all at once.
                         (match [msg-token]
                                [:retreive] ((fn [uri]
                                               (let [winery-id  (nth (first (re-seq #"winery=(\d+)" (query-of uri))) 1)
                                                     html (fetch-uri uri)
                                                     msg  {:dom html :winery-id winery-id}]
                                                 (go (>! extract-channel [:extract msg])))) data)

                                [:extract] ((fn [data] (go (>! store-channel [:store (extract-winery-data data)]))) data)

                                [:store] ((fn [data] (with-db doc-db (put-document data))) data))))}))

(defn mine
  "Retreive the desired infromation and save it by:
   1) starting handling messages flowing out of the channels with the dispatcher
   2) input data into the system for processing"
  [{:keys [uri region] :as settings} ]
  (let [{:keys [dispatcher channels]} (wire-up settings)]
    (do (go (while true
              (let [[val ch] (alts! channels)]
                (dispatcher ch val))))
        (map #(go (>! retreive-channel [:retreive %])) (fetch-winery-list uri)))))

;; It'd be cool to show some of the higher level things we can do with channels

(comment
  (fetch-winery-list base-uri)
  (mine {:uri base-uri :region "paso" } )
)
