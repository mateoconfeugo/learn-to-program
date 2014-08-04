(ns learn-clojure.pallet.crate.nginx
  (:require
     [pallet.api :refer [server-spec plan-fn]]
     [pallet.actions :refer [package package-manager remote-file exec-script*]]
     [pallet.crate :refer [defplan]]
     [clostache.parser :refer [render]]))

(defplan create-nginx-spec
  "install nginx package, configure and restart it"
  [{:keys [template ipsec-cfgs] :as args}]
  (let [tmpl (slurp (-> cfgs :nginx :template-path))
        cfg-contents (render tmpl ipsec-cfgs)]
    (server-spec :phases {:configure (plan-fn
                                       (package-manager :update)
                                       (package "nginx")
                                       (remote-file "/etc/nginx/nginx.conf" :content cfg-contents :owner "root")
                                       (exec-script*  "service nginx restart"))})))
