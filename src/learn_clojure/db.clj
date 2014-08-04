(ns learn-clojure.db
  "Functions related to manipulating the persistance store for the
   site builders authoring artifacts/resources"
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [com.ashafa.clutch :as clutch]
            [learn-clojure.config :refer [config]]
            [clojure.java.jdbc :as sql :refer [with-connection create-table]])
  (:import (java.net URI)))

(def tables ["user profile"])

(defn get-default-config [] config)

(defn drop-database
  [db-spec name]
  (sql/with-connection db-spec
    (with-open [s (.createStatement (sql/connection))]
      (.addBatch s (str "DROP DATABASE IF EXISTS " name))
      (seq (.executeBatch s)))))

(defn create-database
  [db-spec name]
  (sql/with-connection db-spec
    (with-open [s (.createStatement (sql/connection))]
      (.addBatch s (str "DROP DATABASE IF EXISTS " name))
      (.addBatch s (str "create database " name))
      (seq (.executeBatch s)))))

(defn drop-table [table dsn]
  (sql/with-connection dsn
    (try
      (sql/drop-table (keyword table))
      (catch Exception _))))

(defn drop-tables
  "remove the database the has all the feed partner data"
  [dsn tables] (doseq [t tables] (drop-table t dsn)))

(defn create-user-authoring-database
  [prefix username]
  "Create the logical persistance store for the user authored artifacts"
  (clutch/create! (clutch/couch (str prefix "-" username ))))

(defn get-user-database
  [prefix username]
  "Retreve the logical persistance store for the user authored artifacts"
  (clutch/create! (clutch/couch  (str prefix"-" username))))

(defn map-from-db
  "Turn the CouchDB map into the CMS map"
  [db-map]
  (if-let [data (:data db-map)]
    (assoc (dissoc data :type) :id (:_id db-map))))

(defn initialize-management-database
  [db-spec]
  "Build schema for the management database"
  (sql/with-connection db-spec
    (sql/create-table  :user
                       [:id :integer "INTEGER(10) PRIMARY KEY" "AUTO_INCREMENT"]
                       [:username "VARCHAR(50) NOT NULL"]
                       [:password "VARCHAR(225) NOT NULL"])
    (sql/create-table  :profile
                       [:id :integer "INTEGER(10) PRIMARY KEY" "AUTO_INCREMENT"]
                       [:user_id "INTEGER(10) NOT NULL"]
                       [:tag "VARCHAR(225) NOT NULL"]
                       [:query_uri "TEXT NOT NULL"])))

(defn get-editor-db-host
  [cfg]
  (let [cfg (if-let [cfg not-empty] cfg (get-default-config))]
    (or (System/getenv "EDITOR_DATABASE_HOST") (:db-host cfg))))

(defn get-editor-db-port
  [cfg]
  (let [cfg (if-let [cfg not-empty] cfg (get-default-config))]
    (or (System/getenv "EDITOR_DATABASE_PORT") (:db-port cfg))))

(defn get-editor-db-username-prefix
  [cfg]
  (let [cfg (if-let [cfg not-empty] cfg (get-default-config))]
    (or (System/getenv "EDITOR_DATABASE_NAME_PREFIX") (:db-name-prefix cfg))))

(defn get-editor-db-api-key
  [cfg]
  (let [cfg (if-let [cfg not-empty] cfg (get-default-config))]
    (or (System/getenv "EDITOR_DATABASE_API_KEY") (:api-key cfg))))

(defn get-editor-db-api-password
  [cfg]
  (let [cfg (if-let [cfg not-empty] cfg (get-default-config))]
    (or (System/getenv "EDITOR_DATABASE_API_PASSWORD") (:api-password cfg))))

(defn editor-resource
  [prefix username sb-db-host db-api-key db-api-password]
  "Assemble the database dsn path for an editor user
   authoring artifacts and resources"
  (assoc (cemerick.url/url sb-db-host (format "%s-%s" prefix username))
    :username db-api-key :password db-api-password))

(defn get-docs
  [db prefix username]
  "Get a list of all the users landing site"
  (->> (clutch/get-view db (format "%s-%s" prefix username) :all)
       (map (juxt :key :value))
       (into {})
       keywordize-keys))

(defn editor-db
  [prefix user]
  "Get the users authoring database for editor artifacts and resources"
  (clutch/get-database (format "%s-%s" prefix user)))
