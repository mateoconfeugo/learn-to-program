(defproject learn-to-program "0.1.0"
  :description "Web site application for learning to program"
  :url "http://interestingsoftwarestuff.com/learn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[compojure "1.1.5"] ; Web routing https://github.com/weavejester/compojure
                 [com.taoensso/timbre "2.2.0"] ; Logging https://github.com/ptaoussanis/timbre
                 [enlive "1.1.1"] ; DOM manipulating                                   
                 [liberator "0.9.0"] ; WebMachine(REST state machine) port to clojure
                 [me.raynes/fs "1.4.0"]  ; File manipulation tools                                 
                 [org.clojure/clojure "1.5.1"]  ; Lisp on the JVM
                 [ring "1.2.0"]
                 [ring.middleware.logger "0.4.0"]]
  :plugins [[lein-ring "0.8.6"]
            [lein-localrepo "0.4.1"]            
            [s3-wagon-private "1.1.2"]            
            [lein-expectations "0.0.7"]
            [lein-autoexpect "0.2.5"]]
  :repositories [["private" {:url "s3p://marketwithgusto.repo/releases/"
                             :username :env
                             :passphrase :env}]]
  :main learn-to-program.handler
  :ring {:handler learn-to-program.handler/app}
  :profiles  {:dev {:dependencies [[ring-mock "0.1.3"]
                                   [ring/ring-devel "1.1.8"]
                                   [expectations "1.4.49"]]}})
