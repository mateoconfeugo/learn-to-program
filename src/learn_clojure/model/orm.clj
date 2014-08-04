(ns learn-clojure.model.orm
  "storing and manipulating leads"
  (:require [clojure.core]
            [korma.core :refer [defentity database insert values has-many many-to-many
                                transform belongs-to has-one fields table prepare pk
                                subselect where belongs-to limit aggregate order]]
            [korma.db :refer [defdb mysql]]))

(declare user profile)

(defn mgmt-dbh
  [{:keys [db user password host port] :as args}]
  "Get a handle to the mgmt database"
  (mysql args))

(defentity user
  (pk :id)
  (table :user)
  (fields   :username  :password :id)
  (prepare (fn [{last :last :as v}]
             (if last
               (assoc v :last (clojure.string/upper-case last)) v)))
  (transform (fn [{first :first :as v}]
               (if first
                 (assoc v :first (clojure.string/capitalize first)) v)))
  (has-one profile))

(defentity profile
  (pk :id)
  (table :profile)
  (fields :id :id_user :tag_name :query_uri)
  (belongs-to user))
;;
;;  (has-one address)
;;  (has-many email)

(comment

  (declare version release supported-release user profile email address account company organization owner contact credit-card payment)

(defentity version
  (table :version)
  (database mgmt-db)
  (fields :db-version  :deployed-on)
  (has-one release)
  (has-many supported-app-release))

(defentity release
  (table :releases)
  (fields :released-on :tag))

(defentity supported-app-release
  (table :supported_release)
  (database mgmt-db)
  (fields :version-id  :deployed-on :released-on))

(defentity account
  (pk :id)
  (table :account)
  (database mgmt-db)
  (has-many user))

(defentity profile
  (pk :id)
  (table :profile)
  (database mgmt-db)
  (fields :id :id_user :tag_name :query_uri)
  (belongs-to user))

(defentity email
  (pk :id)
  (table :email)
  (database mgmt-db)
  (fields :id :id_user :address)
  (belongs-to user {:fk :id_user}))

(defentity address
  (pk :id)
  (table :address)
  (database mgmt-db)
  (belongs-to user)
  (belongs-to state {:fk :id_state}))

(defentity company
   (pk :id)
   (table :company)
   (database mgmt-db)
   (has-many address)
   (fields :name :description))

(defentity organization
  (pk :id)
  (table :organization)
  (database mgmt-db)
  (has-many address)
  (fields :name :description))

(defentity owner
  (pk :id)
  (table :owner)
  (database mgmt-db)
  (has-one user))

(defentity contact
  (pk :id)
  (table :contact)
  (database mgmt-db)
  (has-one address))

(defentity account-standing
  (pk :id)
  (table :account_standing)
  (database mgmt-db)
  (has-one account))

(defentity credit-card
  (pk :id)
  (table :credit_card)
  (database mgmt-db))

(defentity payment
  (pk :id)
  (table :payment)
  (database mgmt-db))
)
