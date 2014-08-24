(ns learn-clojure.concurrency)


;-----
(def d (delay (println "Running...")
              :done!))
;= #'user/d
(deref d)
; Running...
;= :done!


;-----
(def a-fn (fn []
            (println "Running...")
            :done!))
;= #'user/a-fn
(a-fn)
; Running...
;= :done!


;-----
@d
;= :done!


;-----
(defn get-document
  [id]
  ; ... do some work to retrieve the identified document's metadata ...
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (delay (slurp "http://www.mozilla.org/about/manifesto.en.html"))})
;= #'user/get-document
(def d (get-document "some-id"))
;= #'user/d
d
;= {:url "http://www.mozilla.org/about/manifesto.en.html",
;=  :title "The Mozilla Manifesto",
;=  :mime "text/html",
                                        ;=  :content #<Delay@2efb541d: :pending>}

(realized? (:content d))
;= false
@(:content d)
;= "<!DOCTYPE html><html>..."
(realized? (:content d))
;= true


;-----
(def long-calculation (future (apply + (range 1e8))))
;= #'user/long-calculation


;-----
@long-calculation
;= 4999999950000000


;-----
@(future (Thread/sleep 5000) :done!)
;= :done!


;-----
(deref (future (Thread/sleep 5000) :done!)
       1000
       :impatient!)
;= :impatient!


;-----
(defn get-document
  [id]
  ; ... do some work to retrieve the identified document's metadata ...
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (future (slurp "http://www.mozilla.org/about/manifesto.en.html"))})


;-----
(def p (promise))
;= #'user/p


;-----
(realized? p)
;= false
(deliver p 42)
;= #<core$promise$reify__1707@3f0ba812: 42>
(realized? p)
;= true
@p
;= 42


;-----
(def a (promise))
(def b (promise))
(def c (promise))


;-----
(future
  (deliver c (+ @a @b))
  (println "Delivery complete!"))


;-----
(deliver a 15)
;= #<core$promise$reify__5727@56278e83: 15>
(deliver b 16)
; Delivery complete!
;= #<core$promise$reify__5727@47ef7de4: 16>
@c
;= 31


;-----
(def a (promise))
(def b (promise))
(future (deliver a @b))
(future (deliver b @a))
(realized? a)
;= false
(realized? b)
;= false
(deliver a 42)
;= #<core$promise$reify__5727@6156f1b0: 42>
@a
;= 42
@b
;= 42


;-----
(defn call-service
  [arg1 arg2 callback-fn]
  ; ...perform service call, eventually invoking callback-fn with results...
  (future (callback-fn (+ arg1 arg2) (- arg1 arg2))))


;-----
(defn sync-fn
  [async-fn]
  (fn [& args]
    (let [result (promise)]
      (apply async-fn (conj (vec args) #(deliver result %&)))
      @result)))

((sync-fn call-service) 8 7)
;= (15 1)

                                        ;-----
(comment
class Person {
    public String name;
    public int age;
    public boolean wearsGlasses;

    public Person (String name, int age, boolean wearsGlasses) {
      this.name = name;
      this.age = age;
      this.wearsGlasses = wearsGlasses;
    }
}

Person sarah = new Person("Sarah", 25, false);

)

;-----
(def sarah {:name "Sarah" :age 25 :wears-glasses? false})
;= #'user/sarah


;-----
@(atom 12)
;= 12
@(agent {:c 42})
;= {:c 42}
(map deref [(agent {:c 42}) (atom 12) (ref "http://clojure.org") (var +)])
;= ({:c 42} 12 "http://clojure.org" #<core$_PLUS_ clojure.core$_PLUS_@65297549>)


;-----
(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))


;-----
(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))


;-----
(def sarah (atom {:name "Sarah" :age 25 :wears-glasses? false}))
;= #'user/sarah
(swap! sarah update-in [:age] + 3)
;= {:age 28, :wears-glasses? false, :name "Sarah"}


;-----
(swap! sarah (comp #(update-in % [:age] inc)
                   #(assoc % :wears-glasses? true)))
;= {:age 29, :wears-glasses? true, :name "Sarah"}


;-----
(def xs (atom #{1 2 3}))
;= #'user/xs
(wait-futures 1 (swap! xs (fn [v]
                            (Thread/sleep 250)
                            (println "trying 4")
                            (conj v 4)))
                (swap! xs (fn [v]
                            (Thread/sleep 500)
                            (println "trying 5")
                            (conj  5))))
;= nil
; trying 4
; trying 5
; trying 5
@xs
;= #{1 2 3 4 5}


;-----
(def x (atom 2000))
;= #'user/x
(swap! x #(Thread/sleep %))
;= nil


;-----
(compare-and-set! xs :wrong "new value")
;= false
(compare-and-set! xs @xs "new value")
;= true
@xs
;= "new value"


;-----
(def xs (atom #{1 2}))
;= #'user/xs
(compare-and-set! xs #{1 2} "new value")
;= false


;-----
(reset! xs :y)
;= :y
@xs
;= :y


;-----
(defn echo-watch
  [key identity old new]
  (println key old "=>" new))
;= #'user/echo-watch
(def sarah (atom {:name "Sarah" :age 25}))
;= #'user/sarah
(add-watch sarah :echo echo-watch)
;= #<Atom@418bbf55: {:name "Sarah", :age 25}>
(swap! sarah update-in [:age] inc)
; :echo {:name Sarah, :age 25} => {:name Sarah, :age 26}
;= {:name "Sarah", :age 26}
(add-watch sarah :echo2 echo-watch)
;= #<Atom@418bbf55: {:name "Sarah", :age 26}>
(swap! sarah update-in [:age] inc)
; :echo {:name Sarah, :age 26} => {:name Sarah, :age 27}
; :echo2 {:name Sarah, :age 26} => {:name Sarah, :age 27}
;= {:name "Sarah", :age 27}


;-----
(remove-watch sarah :echo2)
;= #<Atom@418bbf55: {:name "Sarah", :age 27}>
(swap! sarah update-in [:age] inc)
; :echo {:name Sarah, :age 27} => {:name Sarah, :age 28}
;= {:name "Sarah", :age 28}


;-----
(reset! sarah @sarah)
; :echo {:name Sarah, :age 28} => {:name Sarah, :age 28}
;= {:name "Sarah", :age 28}


;-----
(def history (atom ()))

(defn log->list
  [dest-atom key source old new]
  (when (not= old new)
    (swap! dest-atom conj new)))

(def sarah (atom {:name "Sarah", :age 25}))
;= #'user/sarah
(add-watch sarah :record (partial log->list history))
;= #<Atom@5143f787: {:age 25, :name "Sarah"}>
(swap! sarah update-in [:age] inc)
;= {:age 26, :name "Sarah"}
(swap! sarah update-in [:age] inc)
;= {:age 27, :name "Sarah"}
(swap! sarah identity)
;= {:age 27, :name "Sarah"}
(swap! sarah assoc :wears-glasses? true)
;= {:age 27, :wears-glasses? true, :name "Sarah"}
(swap! sarah update-in [:age] inc)
                                        ;= {:age 28, :wears-glasses? true, :name "Sarah"}
(require 'clojure.pprint)
(clojure.pprint/pprint @history)
;= ;= nil
;= ; ({:age 28, :wears-glasses? true, :name "Sarah"}
;= ;  {:age 27, :wears-glasses? true, :name "Sarah"}
;= ;  {:age 27, :name "Sarah"}
;= ;  {:age 26, :name "Sarah"})


(comment
;-----
(defn log->db
  [db-id identity old new]
  (when (not= old new)
    (let [db-connection (get-connection db-id)]
      ...)))

(add-watch sarah "jdbc:postgresql://hostname/some_database" log->db)
)

;-----
(def n (atom 1 :validator pos?))
;= #'user/n
(swap! n + 500)
;= 501
(swap! n - 1000)
;= #<IllegalStateException java.lang.IllegalStateException: Invalid reference state>


;-----
(def sarah (atom {:name "Sarah" :age 25}))
;= #'user/sarah
(set-validator! sarah :age)
;= nil
(swap! sarah dissoc :age)
;= #<IllegalStateException java.lang.IllegalStateException: Invalid reference state>


;-----
(set-validator! sarah #(or (:age %)
                         (throw (IllegalStateException. "People must have `:age`s!"))))
;= nil
(swap! sarah dissoc :age)
                                        ;= #<IllegalStateException java.lang.IllegalStateException: People must have `:age`s!>


;-----
(defn character
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500}
              opts)))


;-----
(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))


;-----
(defn loot
  [from to]
  (dosync
    (when-let [item (first (:items @from))]
      (alter to update-in [:items] conj item)
      (alter from update-in [:items] disj item))))

(map (comp count :items deref) [smaug])

;-----
(wait-futures 1
              (while (loot smaug bilbo))
              (while (loot smaug gandalf)))
;= nil
@smaug
;= {:name "Smaug", :items #{}, :health 500}
@bilbo
;= {:name "Bilbo", :items #{0 44 36 13 ... 16}, :health 500}
@gandalf
;= {:name "Gandalf", :items #{32 4 26 ... 15}, :health 500}


;-----
(map (comp count :items deref) [bilbo gandalf])
;= (21 29)

;-----
(defn attack
  [aggressor target]
  (dosync
    (let [damage (* (rand 0.1) (:strength @aggressor))]
      (commute target update-in [:health] #(max 0 (- % damage))))))

(defn heal
  [healer target]
  (dosync
    (let [aid (* (rand 0.1) (:mana @healer))]
      (when (pos? aid)
        (commute healer update-in [:mana] - (max 5 (/ aid 5)))
        (commute target update-in [:health] + aid)))))


;-----
(def alive? (comp pos? :health))

(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))


;-----
(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo))
;= nil
(map (comp :health deref) [smaug bilbo])
;= (488.80755445030337 -12.0394908759935)


;-----
(dosync
  (alter smaug assoc :health 500)
  (alter bilbo assoc :health 100))

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))
;= nil
(map (comp #(select-keys % [:name :health :mana]) deref) [smaug bilbo gandalf])
;= ({:health 0, :name "Smaug"}
;=  {:health 853.6622368542827, :name "Bilbo"}
;=  {:mana -2.575955687302212, :health 75, :name "Gandalf"})


;-----
(defn- enforce-max-health
  ;;  [{:keys [name health]}]
    [name health]
  (fn [character-data]
    (or (<= (:health character-data) health)
      (throw (IllegalStateException. (str name " is already at max health!"))))))

(defn character
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500}
                     opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health name (:health cdata))
                          (:validators cdata))]
    (ref (dissoc cdata :validators)
      :validator #(every? (fn [v] (v %)) validators))))


;-----
(def bilbo (character "Bilbo" :health 100 :strength 100))
;= #'user/bilbo
(heal gandalf bilbo)
;= #<IllegalStateException java.lang.IllegalStateException: Bilbo is already at max health!>


;-----
(dosync (alter bilbo assoc-in [:health] 95))
;= {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 95, :xp 0}
(heal gandalf bilbo)
;= #<IllegalStateException java.lang.IllegalStateException: Bilbo is already at max health!>


;-----
(defn heal
  [healer target]
  (dosync
    (let [aid (min (* (rand 0.1) (:mana @healer))
                   (- (:max-health @target) (:health @target)))]
      (when (pos? aid)
        (commute healer update-in [:mana] - (max 5 (/ aid 5)))
        (alter target update-in [:health] + aid)))))


;-----
(dosync (alter bilbo assoc-in [:health] 95))
;= {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 95}
(heal gandalf bilbo)
;= {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100}
(heal gandalf bilbo)
                                        ;= nil

(def ^:dynamic *max-value* 255)
;= #'user/*max-value*
(defn valid-value?
  [v]
  (<= v *max-value*))
;= #'user/valid-value?
(binding [*max-value* 500]
  (valid-value? 299))
;= true


;-----
(binding [*max-value* 500]
  (println (valid-value? 299))
  (doto (Thread. #(println "in other thread:" (valid-value? 299)))
    .start
    .join))

;-----
(def ^:dynamic *var* :root)
;= #'user/*var*
(defn get-*var* [] *var*)
;= #'user/get-*var*
(binding [*var* :a]
  (binding [*var* :b]
    (binding [*var* :c]
      (get-*var*))))
;= :c


;-----
(binding [*var* :a]
  (binding [*var* :b]
    (binding [*var* :c]
      (binding [*var* :d]
        (get-*var*)))))
;= :d


;-----
(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (if (== 404 response-code)
      [response-code]
      [response-code (-> conn .getInputStream slurp)])))

(http-get "http://google.com/bad-url")
;= [404]
(http-get "http://google.com/")
;= [200 "<!doctype html><html><head>..."]


;-----
(def ^:dynamic *response-code* nil)

(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (when (thread-bound? #'*response-code*)
      (set! *response-code* response-code))
    (when (not= 404 response-code) (-> conn .getInputStream slurp))))

(http-get "http://google.com")
;= "<!doctype html><html><head>..."
*response-code*
;= nil
(binding [*response-code* nil]
  (let [content (http-get "http://google.com/bad-url")]
    (println "Response code was:" *response-code*)
    ; ... do something with `content` if it is not nil ...
    ))
;= Response code was: 404
;= nil


;-----
(binding [*max-value* 500]
  (println (valid-value? 299))
  @(future (valid-value? 299)))
; true
;= true


;-----
(binding [*max-value* 500]
  (map valid-value? [299]))
;= (false)


;-----
(map #(binding [*max-value* 500]
        (valid-value? %))
     [299])
                                        ;= (true)


(def a (agent 500))
;= #'user/a
(send a range 1000)
;= #<Agent@53d2f8be: 500>
@a
;= (500 501 502 503 504 ... 999)


;-----
(def a (agent 0))
;= #'user/a
(send a inc)
;= #<Agent@65f7bb1f: 1>


;-----
(def a (agent 5000))
(def b (agent 10000))

(send-off a #(Thread/sleep %))
;= #<Agent@da7d7b5: 5000>
(send-off b #(Thread/sleep %))
;= #<Agent@c0cd75b: 10000>
@a
;= 5000
(await a b)
;= nil
@a
;= nil


;-----
(def a (agent nil))
;= #'user/a
(send a (fn [_] (throw (Exception. "something is wrong"))))
;= #<Agent@3cf71b00: nil>
a
;= #<Agent@3cf71b00 FAILED: nil>
(send a identity)
;= #<Exception java.lang.Exception: something is wrong>



;-----
(set-error-handler! a (fn [the-agent exception]
                        (when (= "FATAL" (.getMessage exception))
                          (set-error-mode! the-agent :fail))))
;= nil
(send a (fn [_] (throw (Exception. "FATAL"))))
;= #<Agent@6fe546fd: nil>
(send a identity)
;= #<Exception java.lang.Exception: FATAL>


;-----
(require '[clojure.java.io :as io])

(def console (agent *out*))
(def character-log (agent (io/writer "character-states.log" :append true)))


;-----
(defn write
  [^java.io.Writer w & content]
  (doseq [x (interpose " " content)]
    (.write w (str x)))
  (doto w
    (.write "\n")
    .flush))


;-----
(defn log-reference
  [reference & writer-agents]
  (add-watch reference :log
             (fn [_ reference old new]
               (doseq [writer-agent writer-agents]
                 (send-off writer-agent write new)))))


;-----
(def smaug (character "Smaug" :health 500 :strength 400))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 1000))

(log-reference bilbo console character-log)
(log-reference smaug console character-log)

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))

; {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 490.0526187036565}
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 61.501239189435296}
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100.0}
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 67.34251519995752}
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100.0}
; {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 480.9901413567355}
; ...


;-----
(defn attack
  [aggressor target]
  (dosync
    (let [damage (* (rand 0.1) (:strength @aggressor) (ensure daylight))]
      (send-off console write
        (:name @aggressor) "hits" (:name @target) "for" damage)
      (commute target update-in [:health] #(max 0 (- % damage))))))

(defn heal
  [healer target]
  (dosync
    (let [aid (min (* (rand 0.1) (:mana @healer))
                   (- (:max-health @target) (:health @target)))]
      (when (pos? aid)
        (send-off console write
          (:name @healer) "heals" (:name @target) "for" aid)
        (commute healer update-in [:mana] - (max 5 (/ aid 5)))
        (alter target update-in [:health] + aid)))))

(dosync
  (alter smaug assoc :health 500)
  (alter bilbo assoc :health 100))
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100}
; {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 500}

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))
; {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 497.41458153660614}
; Bilbo hits Smaug for 2.585418463393845
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 66.62625211852506}
; Smaug hits Bilbo for 33.373747881474934
; {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 494.6674778679298}
; Bilbo hits Smaug for 2.747103668676348
; {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100.0}
; Gandalf heals Bilbo for 33.37374788147494
; ...


;;(:require '[clojure.core.async  :refer [put! >! chan go <!! alts! <! >!! go-loop close! onto-chan into]])
;;(:require '[clojure.pprint :refer [pprint]])
;;(:require '[clojure.edn])
;;(:require '[net.cgrand.enlive-html :as html])
;;(:require '[clojure.core.match :refer [match]])
;;(:require '[clojurewerkz.urly.core :refer [query-of]])
