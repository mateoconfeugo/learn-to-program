(ns learn-clojure.macros-channels
    (:require [clojure.core.async  :refer [put! >! chan go <!! alts! timeout take!]]
              [clojure.pprint :refer [pprint]]
              [clojure.core.match :refer [match]]
              [org.httpkit.client :as http]
              [plumbing.core :refer :all]
              [plumbing.graph :as graph]))

;; FUNCTIONS GRAPHS
(comment
  A Graph is a simple and declarative way to specify a structured computation, which is easy to analyze,
  change, compose, and monitor.n

  We can "compile" this Graph to produce a single function, the graph represents the steps in taking a minimum set of
  input args and producing all the data in one structure the test suite  proves assertions against.

  customization for  individual tasks is done by modifing some aspect of the graph, compiling - creating
  a new function  call that function and use that for the task.

  Setting up the default test data graph will use the helper functions.  With this we should get both
  composiblitiy and flexiblity

  These data graphs can help elminate and find duplicated code in the application itself.
  This way the functions that define the behaviour of the system as sequential transformation of types is not
  obscured by the assembly of data type operations creating the instances of the types that are passed between the
  application functions.

  I want to start with graphs and move to macros because they both manipulate/transform the code.
  The key is when.
  )

;; This just a map
(def stats-graph
  "A graph specifying the same computation as 'stats'"
  {:n  (fnk [xs]   (count xs))
   :m  (fnk [xs n] (/ (sum identity xs) n))
   :m2 (fnk [xs n] (/ (sum #(* % %) xs) n))
   :v  (fnk [m m2] (- m2 (* m m)))})

;; Compile into a function
(def stats-eager (graph/compile stats-graph))

;; call the function
(= {:n 4 :m 3 :m2 (/ 25 2) :v (/ 7 2)} (into {} (stats-eager {:xs [1 2 3 6]})))


(def extended-stats
  (graph/compile                        ;;  compile - step down the ladder
   (assoc stats-graph                   ;;  Manipulate the computation - step up the ladder
     :sd (fnk [^double v] (Math/sqrt v)))))

(= {:n 4
    :m 3
    :m2 (/ 25 2)
    :v (/ 7 2)
    :sd (Math/sqrt 3.5)}
   (into {} (extended-stats {:xs [1 2 3 6]})))

;; So neato that might be useful to help break common behavoir out of the function

;; INTRODUCTION TO MACROS

(comment  CLOJURE EVALUATION MODEL

         "CLOJURE EVALUATES DATA STRUCTURES TO VALUES!!!!"

  reader ---> data structures  ---> macroexpansion
                    |                   |
                    v                   v
                  evaluation  <---  data stucture
                    |
                    v
                  value

                    * reader macros define the rules for turning text into data structures
                    * Symbols are associated with there values using def
                    * A symbol resolves to a value or a special form
                    * A clojure program is comprised of data structures
                    * clojure can manipulate these data structures
                    * Macros are the mechanism clojure uses to manipulate the datastructures
                    that make up the program.
                    * Functions are the mechanism that manipulate and consume datastructures at runtime
                    * How macros fit into the evaluation model allows for interesting and powerful techniques
 )

;; MECHANICS
;; look at how clojure is written using its own data structures
;; lists, maps, vectors - this is why clojure is called a homoiconic language
;; Some folks get in a panic about all the parentheses, don't, rather remember
;; that when writing lisp you are just using data structures which is quite cool
;; besides any editor worth using will have some sort of parentheses matching mechanism
(defn mapcat
  "Returns the result of applying concat to the result of appying map
   to f and colls.  Thus function fu should return a collection"
  {:added "1.0"
   :static true}
  [f & colls]
  (apply concat (apply map f colls)))

;; Its this homoiconic nature of clojure that makes the macros in lisp so powerful

;; A dynamic interpreted language can do similar things by manipulating strings
(read-string "(+ 1 2 3)")
(class (read-string "(+ 1 2 3)"))
(eval (read-string "(+ 1 2 3)"))

;; Wrong .. why?
(let [expression (+ 1 2 3 4 5)]  ;; This gets evaled and you get 15 rather than a list to manipulate
  (cons
   (read-string "*")
   (rest expression)))

;; Remember in lisps code is data and functions get evalued so we got to delay some evalating
;; until after the code has been manipulated

;; quote verb
(let [expression (quote (+ 1 2 3 4 5))]
  (cons
   (read-string "*")
   (rest expression)))

;; So how do functions eval their args
(defn print-with-asterisks [printable-arg]
  (print "*****")
  (print printable-arg)
  (print "*****"))

(print-with-asterisks "hello")

(print-with-asterisks (do (println "In argument expression")
                          "hi"))

;; short hand for the quote
(let [expression '(+ 1 2 3 4 5)]
  (cons
   (read-string "*")
   (rest expression)))

;; THE MACRO LADDER

;; Lets think of about a ladder
;; |-|  moving up the ladder code becomes data
;; |-|
;; |-|
;; |-|  moving down the ladder data becomes code

;; When we encounter a macro call in code we will think of it like a function
;; but one that operates one level up the ladder on unevaluated code rather
;; than evaluated data
;; Expanding a macro takes us up a rung on the ladder.  Once expanded step down a
;; rung with an expanded expression.

;; a real macro
(defmacro when
  "Evalutes test. If logical true, evaluates body in an implicict do."
  [test & body]
  (list 'if test (cons 'do body)))

;; Difference between c/c++ scala ruby is the meta programming is very similar
;; to normal programming
(when (= 2 (+ 1 1)) (println "true"))

;; lets see what actually gets run by using macro expansion
(macroexpand-1 '(when (= 2 (+ 1 1)) (println "true")))

(defmacro cond
  [& clauses]
  (when clauses
    (list 'if (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "cond requires an even number of forms")))
          (cons 'clojure.core/cond (next (next clauses))))))

(clojure.walk/macroexpand-all '(cond false 1 true 2))
(macroexpand-1 '(cond false 1 true 2))

;; MORE ADVANCED MACRO TECHNIQUES
;; Syntax Quoting and unquoting - a more sane templated approach to creating code

;; quote is stubborn
(def a 4)
'(1 2 3 a 5)
(list 1 2 3 a 5)

;; what we want is the syntax quote ` and then uses the unquote ~ operator
`(1 2 3 ~a 5)
;; this allows us to treat this like a template

(defmacro assert [x]
  (when *assert*
    `(when-not ~x
       (throw (new AssertionError (str "Assert failed: " (pr-str '~x)))))))

(macroexpand-1 '(assert (= 1 1)))

;; What is this weird '~x
`(1 2 3 '~a 5)
;; '~ gives us a way to quote the results of an expession
;; ' is a reader macro and so is ~

(def other-numbers '(4 5 6 7 8 9))
`(1 2 3 ~other-numbers 10 )
`(1 2 3 ~@other-numbers 10)

;; namespace qualifed
'(a b c)
`(a b c)

;; caution
(defmacro squares [xs] (list 'map '#(* % %) xs))
(squares (range 1 10))
(macroexpand-1 '(squares (range 1 10)))
;;(ns foo (:refer-clojure :exclude [map]))
(def map {:a 1 :b 2})
(user/squares (range 1 10))
(user/squares :a)
(macroexpand '(user/squares (range 1 10)))

(defmacro make-adder [x] `(fn [~'y] (+ ~x ~'y)))

(def y 100)
((make-adder (+ y 3)) 5)
;; Whaaa? lets take a look at what is actually happening
(macroexpand-1 '(make-adder (+ y 3)))
;; we see we are creating a function that takes one arg named y
;; that shadows the (def y 100)

;; This mistake we have made is what is called symbol capture,
;; where the macro has internally shadowed, or captured, some symbols
;; that users of this might expect to be available when their expression is evaluted.

;; MACRO HYGIENE
;; to avoid symbolic capture we use the gensym function that produces a symbol with a unique name.
;; the names look funny because they need to be unique for the application but don't worry
;; we never have to type them

(gensym)
(gensym "xyz")
;; short cut   y#
(defmacro make-adder1 [x]
  (let [y (gensym)]
    `(fn [~y] (+ ~x ~y))))

((make-adder1 (+ y 3)) 5)
;; short cut for gensym  called auto-gensym
;; This is the idomatic way
(defmacro make-adder2 [x]
  `(fn [y#] (+ ~x y#)))

((make-adder2 (+ y 3)) 5)

;; Binding set up by special forms like let, letfn and try's catch clause have the
;; same requirement as function arguments, so typically use auto-gensym there.

;; Macro &form and &env are only available within macros
(defmacro info-about-caller []
  (clojure.pprint/pprint {:form &form :env &env})
  `(println "macro was called"))

(info-about-caller)

(let [foo "bar"] (info-about-caller))

;; &env typically used to look at the keys which are symbols and inject them into the expanded macro
(defmacro inspect-caller-locals-1 []
  (->> (keys &env)
       (map (fn [k] [`(quote ~k) k]))
       (into {})))

(defmacro inspect-caller-locals-2 []
  (->> (keys &env)
       (map (fn [k] [(list 'quote k) k]))
       (into {})))

(defmacro inspect-caller-locals []
  (->> (keys &env)
       (map (fn [k] [`'~k k]))
       (into {})))

(inspect-caller-locals)
(let [foo "bar" baz "quux"] (inspect-caller-locals))
(let [foo "bar" baz "quux"] (inspect-caller-locals-1))
(let [foo "bar" baz "quux"] (inspect-caller-locals-2))

;; &form is a special variable that contains the expression that was used to call the macro
(defmacro inspect-called-form [& arguments]
  {:form (list 'quote (cons 'inspect-called-form arguments))})

(inspect-called-form  1 2 3)

(defmacro inspect-called-form [& arguments]
  {:form (list 'quote &form)})

^{:doc "this is good stuff"} (inspect-called-form 1 2 3)
(meta (:form *1))


;; COMMUNICATING SEQUENTIAL PROCESSES
(comment
    A program using the communicating sequential processes model consists of independent,
    concurrently executing entities that communicate by sending each other messages.

    The difference is one of emphasis— instead of focusing on the entities sending the messages,
    CSP focuses on the channels over which they are sent.

    Channels are first class— instead of each process being tightly coupled to a single mailbox,
    channels can be independently created, written to, read from, and passed between processes.

    A channel is a thread-safe queue.
    Any task with a reference to a channel can add messages to one end,
    Any task with a reference to it can remove messages from the other.
    Senders don’t have to know about receivers, or vice versa.

  by default channels are synchronous or unbuffered writing to a channel blocks until something reads from it.

  The fundamental operations on channels are putting and taking values.

  Both of those operations potentially block,
  but the nature of the blocking depends on the nature of the thread of control in which the operation is performed.

  core.async supports two kinds of threads of control
  - ordinary threads and IOC (inversion of control) 'threads'. Ordinary threads can be created in any manner
  , but IOC threads are created via go blocks. Because JS does not have threads, - only go blocks
    and IOC threads are supported in ClojureScript.
    Channels can be ordinary threads using the thread macro and should be preferred over futures for channel work
    )

(def c (chan))
(chan)

;; We can attach listeners via take!
(take! c (fn [v] (println v)))

;; And we can send values via put!
(put! c 42)

;; And in reverse order
(put! c "Hello World")
(take! c (fn [v] (println "Got " v)))

;; The semantics are simple. Callbacks are one-shot. And you can have
;; many readers/writers. Each puts/gets a single value. There is no fan-out
;; or fan-in.

;; Notice the different location of the output, one
;; side or the other dispatches to a threadpool to
;; run the attached callback.

(comment
As with STM, the pervasive use of persistent data structures offers particular benefits for CSP-style channels.
In particular, it is always safe and efficient to put a Clojure data structure on a channel,
without fear of its subsequent use by either the producer or consumer.

  Go Blocks:

  Threads have both an overhead and a startup cost,
  which is why most modern programs avoid creating threads directly and use a thread pool instead
  In particular, they are problematic if the code we want to run might block.

  The Problem with Blocking Thread pools are a great way to handle CPU-intensive tasks—
  those that tie a thread up for a brief period and then return it to the pool to be reused.

  But what if we want to do something that involves communication?
  Blocking a thread ties it up indefinitely, eliminating much of the value of using a thread pool.

  Go blocks allow multiple concurrent tasks to be efficiently multiplexed across a limited pool of threads.

  go is a macro that takes its body and examines it for any channel operations.
  It will turn the body into a state machine. Upon reaching any blocking operation,
  the state machine will be 'parked' and the actual thread of control will be released

  Code within a go block is transformed into a state machine.
  Instead of blocking when it reads from or writes to a channel,
  the state machine parks, relinquishing control of the thread it’s executing on.
  When it’s next able to run, it performs a state transition and continues execution, potentially on another thread.

  The primary channel operations within go blocks are >! ('put') and <! ('take').
  The go block itself immediately returns a channel,
  on which it will eventually put the value of the last expression of the body (if non-nil),
  and then close

  IO is one area where asynchronous code comes into its own—
  instead of the traditional approach of having a thread per connection,
  asynchronous IO allows us to start a number of operations
  and receive a notification whenever one of them has data available.
  Although this is a powerful approach, it can be challenging,
  with code tending to turn into a mess of callbacks calling callbacks.

  core.async can make it much easier by solving these problems relating to  concurrency and composition/modularity

  in clojurescript we can't use a blocking thread because only one thread in the web browser
)

;;  Turn a syncrhonous function into an asynchronous
(defn http-get [url]
  (let [c (chan)]
    (http/get url (fn [r] (put! c r)))
    c))

(def key "b4cb6cd7a349b47ccfbb80e05a601a7c")

(defn request-and-process [url]
  (go
    (-> (str "http://api.themoviedb.org/3/" url "api_key=" key)
        http-get
        <!
        :body
        (cheshire/parse-string true))))

(defn latest-movies []
  (request-and-process "movies/latest?"))

(defn top-rated-movies []
  (request-and-process "movie/top_rated?"))

(<!! (top-rated-movies))

(defn movie-by-id [id]
  (request-and-process (str "movie/" id "?")))

(<!! (movie-by-id 238))

(defn movie-cast [id]
  (request-and-process (str "movie/" id "/casts?")))

(<!! (movie-cast 238))

(defn people-by-id [id]
  (request-and-process (str "person/" id "?")))

(<!! (people-by-id 3144))

(defn avg [col]
  (-> (clojure.core/reduce + 0 col)
      (/ (count col))))

(avg [1 2 3 4 5])

(defn avg-cast-popularity [id]
  (go
    (let [cast (->> (movie-cast id)
                    <!
                    :cast
                    (clojure.core/map :id)
                    (clojure.core/map people-by-id)
                    (async/map vector)
                    <!
                    (clojure.core/map :popularity)
                    avg)]
      cast)))

(<!! (avg-cast-popularity 238))


(time (do (dotimes [x 10]
            (<!! (http-get "http://www.imdb.com/xml/find?json=1&q=ben+afleck")))
          nil))

(time (do (->> (for [x (range 10)]
                 (http-get "http://www.imdb.com/xml/find?json=1&q=ben+afleck"))
               doall
               async/merge
               (async/take 10)
               (async/into [])
               <!!)
          nil))

;;;; Mult ;;;;

;; Create a mult. This allows data from one channel to be broadcast
;; to many other channels that "tap" the mult.

(def to-mult (chan 1))
(def m (mult to-mult))

(let [c (chan 1)]
  (tap m c)
  (go (loop []
        (when-let [v (<! c)]
          (println "Got! " v)
          (recur))
        (println "Exiting!"))))

(>!! to-mult 42)
(>!! to-mult 43)

(close! to-mult)


;;;; Pub/Sub ;;;

;; This is a bit like Mult + Multimethods

(def to-pub (chan 1))
(def p (pub to-pub :tag))

(def print-chan (chan 1))

(go (loop []
      (when-let [v (<! print-chan)]
        (println v)
        (recur))))

;; This guy likes updates about cats.
(let [c (chan 1)]
  (sub p :cats c)
  (go (println "I like cats:")
      (loop []
        (when-let [v (<! c)]
          (>! print-chan (pr-str "Cat guy got: " v))
          (recur))
        (println "Cat guy exiting"))))

;; This guy likes updates about dogs
(let [c (chan 1)]
  (sub p :dogs c)
  (go (println "I like dogs:")
      (loop []
        (when-let [v (<! c)]
          (>! print-chan (pr-str "Dog guy got: " v))
          (recur))
        (println "Dog guy exiting"))))

;; This guy likes updates about animals
(let [c (chan 1)]
  (sub p :dogs c)
  (sub p :cats c)
  (go (println "I like cats or dogs:")
      (loop []
        (when-let [v (<! c)]
          (>! print-chan (pr-str "Cat/Dog guy got: " v))
          (recur))
        (println "Cat/dog guy exiting"))))


(defn send-with-tags [msg]
  (doseq [tag (:tags msg)]
    (println "sending... " tag)
    (>!! to-pub {:tag tag
                 :msg (:msg msg)})))

(send-with-tags {:msg "New Cat Story"
                 :tags [:cats]})

(send-with-tags {:msg "New Dog Story"
                 :tags [:dogs]})

(send-with-tags {:msg "New Pet Story"
                 :tags [:cats :dogs]})


(close! to-pub)


;;; Limited rate updates to an atom

(def a (atom 1))
(def watch-c (chan (dropping-buffer 1)))

(add-watch a :chan-watch
           (fn [k r o n]
             (put! watch-c :ping)))

(go (while true
      (let [tout (timeout 100)]
        (when-let [x (<! watch-c)]
          (println "-----> "@a)
          (<! tout)))))

(dotimes [x 1000]
  (swap! a inc))

;;;;;; Limited Access to a Shared Resource ;;;;;


(defn function-service [f num-threads]
  (let [c (chan num-threads)]
    (dotimes [x num-threads]
      (thread
        (loop []
          (when-let [[args ret-chan] (<!! c)]
            (>!! ret-chan (apply f args))
            (recur)))))
    c))

(def slurp-service (function-service (comp read-string slurp) 2))


(defn slurp-async [& args]
  (let [c (chan 1)]
    ;; put! is tied to the take! from chan
    ;; so no unbounded-ness here.
    (put! slurp-service [args c])
    c))

(<!! (slurp-async "project.clj"))

(close! slurp-service)

(comment
 How does one use all this stuff?
 I have no idea what the correct way to use graphs, channel, and macros but here is one way I'm working on
 )

(defmacro defdispatcher [& body]
  `(fn [msg-ch# msg-tuple#]
    (let [[msg-event-token# msg-data] (take 2 msg-tuple#)]
      (do (match [msg-event-token#]
                 ~@body)))))

(defmacro defjob
  "Returns  as function graph describing the sequence of job workflow tasks a build will perform.
   A build is run by calling the function that is created by compiling the function graph with the proper
   parameters and properties.
   This build functions returns immediately and processes the job build asynchronously each to the stages
   comminicating with those further down the workflow in a with a go routine"
  [name & body]
  `{:build-name (fnk [~'build-number] (str ~name "-" ~'build-number))
    :input (fnk [~'build-name]
                (let [input# (chan)
                      dispatch# (defdispatcher ~@body)]
                  (go (while true
                        (let [[val# selected-ch#] (alts! [input#])]
                          (dispatch# selected-ch# val#))))
                  input#))})

;; see it expanded do the right weird `'~@ combo, make that combo readable -> pattern common when making macros
;; it seems then then they go through several cycles of simplicity and complexity eventually fitting to the
;; correct verbage of the domain


(defmacro build-graph [steps session]
  (let [nodes (map #(graph-node % session) steps)]
    `(into []  (reverse [~@nodes]))))

(defmacro step [step-number & body]
  `{~step-number '[~@body]})

(defmacro build [& steps]
  `(into []  (reverse [~@steps])))

(defmacro build [& steps]
  `(into [] (map (fn [step#]
                   (let [k# (first (keys step#))
                         c# (first (vals step#))]
                   '[k# (fnk [c#]
                               c#)]) ) (reverse [~@steps]))))

(def steps (build
            (step :one (>! (session monitor)  (<! (:prev session))))
            (step :two (>! (session monitor) (<! :one)))
            (step :three (pprint (format "%s" (<! :two))))))

(pprint steps)
(def test-node (first steps))
(class )

(defmacro graph-node [& node]
  `{:input (chan)
    (first (keys ~@node)) (fnk [] (first (first (vals ~@node)))) })

(defmacro graph-node1 [& node]
  `{:input (fnk [~'build-name]
                (let [k# (first (keys ~@node))
                      c# (first (first (vals ~@node)))]
                  {k# c#}))}
 )

(def node? (graph-node test-node))
(def node? (macroexpand-1 (graph-node1 test-node)             ) )
(keys node?)
  (graph/eager-compile node?)

(comment
  (let [k (first (keys node))
        code (first (first (vals node) ))
        session (atom {})]
    `{~k (fnk [session]
              (go ~@code))}))





(eval (build-graph steps {}))
(eval (-> steps first vals first first))

(eval (-> node? vals first))
(node? {})
(type node?)
(graph/eager-compile node?)

(graph/eager-compile {:ham (:3 node?)} )
(graph/eager-compile (merge  { :1 (fnk [name] (chan) )} (graph-node test-node {:monitor (chan)})))
(graph/eager-compile (graph-node test-node {:monitor (chan)}))
(graph/eager-compile (graph-node (first steps)))
(pprint test-node)
(def session {})
(def fg (graph-node (first steps)))
(graph/eager-compile fg)
(graph/eager-compile '{:1 (fnk [name]
                     (chan))})

(let [[k v] (first test-node) ]
  [k v] )

((first (first (vals (nth steps 0)))) 1)

(count steps)



;; I would use probably with a wrapper constructur type function
(defn new-ci-job [name]
  (let [spec (defjob name
               [:monitor] (#(pprint (:message % ) ) msg-data))
        build-fn (graph/eager-compile spec)]
    (build-fn {:build-number 2})))

(def test-job (new-ci-job "test-app"))

(go (>! (:input test-job)
        [:monitor {:message [{:return-channel (chan) :data {:input {:important-data "yeah baby"}}}]}]))

(go (>! (:input test-job) [:monitor  "yeah baby"]))

;; Using the dsl to describe the specifics for a job build

(require '[clojure.walk :refer [macroexpand-all]])
(pprint (macroexpand '(defjob name [:build-in] (#(pprint %) msg-data))))
(pprint (macroexpand-all '(defjob name [:build-in] (#(pprint %) msg-data))))

(time
 (def job-input-channel (new-job "test-job")))
;; "Elapsed time: 29.048 msecs to create a job "

(def num-jobs 1000)
(def jobs (map (fn [x] (new-ci-job x))  (range 0 num-jobs)))

(time (count jobs))
;; no go
(time (map
       (fn [x] (put! (:input (nth jobs x)) [:monitor {:message  x}]))
       (range 0 num-jobs)))
;; versus go
(time (map
         (fn [x] (go (>! (:input (nth jobs x)) [:monitor {:message  x}])))
         (range 0 num-jobs)))

(time
 (def jobs (map (fn [x] (new-job x))  (range 0 1000))) )
;;"Elapsed time: 0.408 msecs to create 10000 jobs"
(time (count jobs))
(go (>! (:input (nth jobs 2)) [:build-in {:message "yeah baby"}]))

 (go (>! (:input (nth jobs 99)) [:build-in {:message "yeah baby"}]))

(:input (first jobs))
(def inputs (time
             (map (fn [x]
                    (go
                      (>! (:input (nth jobs x)) [:build-in {:message "yeah baby"}])))
                  (range 0 1000))))

;;"Elapsed time: 0.269 msecs to send 100 channels a simple message"

(pprint (macroexpand-1 '(build
                         (step (fnk [x previous-step monitor]
                                    (go (>! monitor (<! previous-step)))))
                         (step (fn [x] (pprint "bad arse stuff part two")))) ) )

(def monitor (chan))
