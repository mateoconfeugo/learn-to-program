(defproject learn-clojure "0.1.0"
  :description "Web site application for learning clojure"
  :url "http://interestingsoftwarestuff.com/learn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "learn-clojure-0.1.0-standalone.jar"
  :main learn-clojure.server
  :ring {:handler learn-clojure.handler/request-handler :auto-reload? true :auto-refresh true}
  :dependencies [[cheshire "5.3.1"] ; json
                 [compojure "1.1.5"] ; Web routing https://github.com/weavejester/compojure
                 [com.taoensso/timbre "2.2.0"] ; Logging https://github.com/ptaoussanis/timbre
                 [org.clojure/clojurescript "0.0-2280"] ;; clojure lisp on web browser
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/core.match "0.2.1"]
                 [de.ubercode.clostache/clostache "1.4.0"] ;; text templating
                 [com.ashafa/clutch "0.4.0-RC1"] ; CouchDB client https://github.com/clojure-clutch/clutch
                 [org.clojars.bsima/stockings "1.1.2.1-SNAPSHOT"]
                 [overtone/at-at "1.2.0"]
                 [org.clojars.vshender/clojure-contrib "1.2.0-SNAPSHOT"]
                 [clojurewerkz/urly "1.0.0"]
                 [crypto-random "1.1.0"]
                 [enlive "1.1.1"] ; DOM manipulating
                 [jayq "2.5.0"] ; jquery
                 [korma "0.3.0-RC5"] ; ORM
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [mysql/mysql-connector-java "5.1.27"]
                 [metis "0.3.3"] ; Form validation
                 [me.raynes/fs "1.4.0"]  ; File manipulation tools
                 [org.clojure/clojure "1.6.0"]  ; Lisp on the JVM
                 [prismatic/plumbing "0.3.3"] ;; function graphs
                 [amalloy/ring-gzip-middleware "0.1.3" :exclusions [org.clojure/clojure]]
                 [com.palletops/pallet "0.8.0-RC.9"] ;; dev ops framework
                 [com.palletops/pallet-jclouds "1.7.3"] ;; cloud provider abstraction layer pallet uses
                 [com.palletops/pallet-docker "0.1.0"]
                 [com.palletops/pallet-aws "0.2.3"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [org.apache.jclouds/jclouds-allblobstore "1.7.2"]
                 [org.apache.jclouds/jclouds-allcompute "1.7.2"]
                 [org.apache.jclouds.driver/jclouds-slf4j "1.7.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.apache.jclouds.driver/jclouds-sshj "1.7.2"] ;; ssh client functionality
                 [ring "1.2.0"] ; Webserver framework
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring.middleware.logger "0.4.0"]
                 [ring-anti-forgery "0.3.0"] ;
                 [ch.qos.logback/logback-classic "1.0.9"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]] ; web server logging middleware
  :plugins [[lein-ring "0.8.6"] ;; run ring server via lein
            [lein-ancient "0.5.4"]
            [s3-wagon-private "1.1.2"] ;; uses AWS s3 bucket as a private repo for jars
            [lein-expectations "0.0.7"] ;; run expections via lein
            [lein-marginalia "0.7.1"]
            [lein-cljsbuild "1.0.3"] ;;  make ClojureScript development easy
            [com.keminglabs/cljx "0.4.0"] ;; s-expression preprocessor
            [lein-autoexpect "0.2.5"]] ;; run continuous expections tests
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}]}
  :cljsbuild { :builds [{:source-paths ["src-cljs"]
                         :compiler {
                                    :output-to "resources/public/js/app.js"  ; default: target/cljsbuild-main.js
                                    :optimizations :whitespace
                                    :pretty-print true}}]}
  :repositories [["private" {:url "s3p://marketwithgusto.repo/releases/"
                             :username :env
                             :passphrase :env}]]
  :profiles  {:dev {:dependencies [[ring-mock "0.1.3"]
                                   [ring/ring-devel "1.1.8"]
                                   [com.palletops/pallet "0.8.0-RC.9" :classifier "tests"]
                                   [expectations "1.4.49"]]
                    :plugins [[com.palletops/pallet-lein "0.8.0-alpha.1"]]}
              :leiningen/reply
              {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.2"]]
               :exclusions [commons-logging]}})
