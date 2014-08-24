;;; Pallet project configuration file
(require '[learn-clojure.pallet.groups.website :refer [create-website-group-spec]])
(require ' [learn-clojure.config :refer [read-config]])
(require '[pallet.api :refer [node-spec group-spec server-spec plan-fn lift converge]])

;;   :image {:image-id "us-east-1/ami-090e0160" }
;;   :location {:location-id "us-east-1"}
;;                        :network-interface {:associate-public-ip-address true}
;;                     :network {:subnet-id "subnet-0d2d394b" :inbound-ports [22]}


(defproject learn-clojure
  :provider {:aws {:node-spec {:image {:os-family :centos }
                               :hardware {:min-cores 1}}}
             :rs  {:node-spec {:image {:os-family :centos }
                               :hardware {:min-cores 1}}}
             :docker {:node-spec {:image {:os-family :centos }
                          :hardware {:min-cores 1}}}}
  :groups [(create-website-group-spec (read-config))])
