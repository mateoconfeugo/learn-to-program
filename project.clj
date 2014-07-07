(defproject learn-clojure "0.1.0"
  :description "Web site application for learning clojure"
  :url "http://interestingsoftwarestuff.com/learn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "learn-clojure-0.1.0-standalone.jar"
  :main learn-clojure.handler
  :ring {:handler learn-clojure.handler/app}
  :dependencies [[cheshire "5.3.1"] ; json
                 [compojure "1.1.5"] ; Web routing https://github.com/weavejester/compojure
                 [com.taoensso/timbre "2.2.0"] ; Logging https://github.com/ptaoussanis/timbre
                 [enlive "1.1.1"] ; DOM manipulating
                 [me.raynes/fs "1.4.0"]  ; File manipulation tools
                 [org.clojure/clojure "1.6.0"]  ; Lisp on the JVM
                 [prismatic/plumbing "0.3.3"] ;; function graphs
                 [ring "1.2.0"] ; Webserver framework
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring.middleware.logger "0.4.0"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]] ; web server logging middleware
  :plugins [[lein-ring "0.8.6"] ;; run ring server via lein
            [s3-wagon-private "1.1.2"] ;; uses AWS s3 bucket as a private repo for jars
            [lein-expectations "0.0.7"] ;; run expections via lein
            [lein-autoexpect "0.2.5"]] ;; run continuous expections tests
  :repositories [["private" {:url "s3p://marketwithgusto.repo/releases/"
                             :username :env
                             :passphrase :env}]]
  :profiles  {:dev {:dependencies [[ring-mock "0.1.3"]
                                   [ring/ring-devel "1.1.8"]
                                   [expectations "1.4.49"]]}})