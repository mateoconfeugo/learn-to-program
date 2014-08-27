(ns learn-clojure.service.jira
  (:require [clojure.core.async  :refer [put! >! chan go <!! alts!]]
            [clojure.pprint :refer [pprint]]
            [clojure.edn]
            [clojure.data.json :refer [read-json json-str]]
            [com.ashafa.clutch :as clutch]
            [http.async.client :as c]
            [net.cgrand.enlive-html :as html]
            [clojure.core.match :refer [match]]
            [clojurewerkz.urly.core :refer [query-of]]))

(def jira-project-url "https://wgtdev.atlassian.net/jira/rest/api/latest/project")
(def session-url "https://wgtdev.atlassian.net/jira/rest/auth/latest/session")

(defn login
  "Login into JIRA"
  [{:keys [ session-url username password]}]
  (with-open [client (c/create-client)]
    (let [response (c/POST client session-url
                           :headers {:Content-Type "application/json"}
                           :body (json-str {:username username :password password}))]
      (c/await response)
      (c/cookies response))))

(def opts {:session-url session-url
           :username "matthewburns@gmail.com"
           :password "Pickle12"})

(def cookies  (login opts))

(defn get-all-projects
  "Return all projects from JIRA"
  [cookies]
  (with-open [client (c/create-client)]
    (let [jira-project-url ""
          response (c/GET client jira-project-url :cookies cookies)]
      (c/await response)
      (read-json (c/string response)))))

(def resp  (c/GET client jira-project-url :cookies cookies))
(c/await resp)
@resp
(get-all-projects cookies)

(defn extract-project-data [])

(defn tickets
  "Contains the control flow logic for the asynchronous aspects of the program"
 [{:keys [session-url username password] :as settings} ]
  (let [session (login session-url username password)
        ;; CHANNELS
        retreive-channel (chan)
        extract-channel (chan)
        store-channel (chan)
        channels [retreive-channel extract-channel store-channel]
        ;; LAMBDA WIRING
        retreive-fn (fn [dsn] (go (>! extract-channel [:extract (get-all-projects session)])))
        extract-fn (fn [data] (go (>! store-channel [:store (extract-project-data data)])))
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
      (map #(go (>! retreive-channel [:retreive %]))  [{}]))))


(tickets {:session-url "https://intranet.mycompany.com/jira/rest/auth/latest/session"
          :username ""
          :password})
