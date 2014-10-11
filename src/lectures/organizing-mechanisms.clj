(ns learn-clojure.organizing-mechanisms
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan put! take! timeout] :as async]
            [clojure.pprint :refer [pprint]]))

;; Mechanisms to organize the code

;; Data structures

;; map reduce

;; namespaces

;; future

;; promise

;; delay

;; atoms

;; agents
;; screen scrapers

;; watches
;; watch behind log

;; valididators

;; stm
;; review topics of state identity persistant data structure

;; dynamic scope var
;; config system

;; java interop

;; records
;; tuples

;; protocols
;; building objects

;; multimethods
;; fill methods for jenkins jobs

;; hierarchies

;; COMMUNICATING SEQUENTIAL PROCESSES
(comment
The objectives of core.async are:

To provide facilities for independent threads of activity, communicating via queue-like channels
To support both real threads and shared use of thread pools (in any combination), as well as ClojureScript on JS engines
To build upon the work done on CSP and its derivatives

by default channels are synchronous or unbuffered writing to a channel blocks until something reads from it.

The fundamental operations on channels are putting and taking values.

Both of those operations potentially block,
but the nature of the blocking depends on the nature of the thread of control in which the operation is performed.

core.async supports two kinds of threads of control
- ordinary threads and IOC (inversion of control) 'threads'. Ordinary threads can be created in any manner
, but IOC threads are created via go blocks. Because JS does not have threads,
  - only go blocks and IOC threads are supported in ClojureScript.
  Channels can be ordinary threads using the thread macro and should be preferred over future for channel work

  A program using the communicating sequential processes model similarly consists of independent,
  concurrently executing entities that communicate by sending each other messages.

  The difference is one of emphasis— instead of focusing on the entities sending the messages,
  CSP focuses on the channels over which they are sent.

  Channels are first class— instead of each process being tightly coupled to a single mailbox,
  channels can be independently created, written to, read from, and passed between processes.

  A channel is a thread-safe queue.
  Any task with a reference to a channel can add messages to one end,
  Any task with a reference to it can remove messages from the other.
  Senders don’t have to know about receivers, or vice versa.
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

(comment
  lowest level primitive is an unbuffered channel you can think of it as a communication point for two logical to transfer a piece of information acts like a synchronization point

  callback used once

  many to many channel

  attach callback to put! as well as take

  operations take   <!!   not transaction safe and blocking
  put   >!!   not transaction safe and blocking

  thread returns a channel

  really lightweight logical threads are created by go blocks

  <! parking take says take the rest the of the body of the go turn it into a callback function and attach it to a channel

  any code in go back can be regular clojure code - use channel from try catch finally

  buffered channel parks when buffer full and continues when channel empty

  this can be used to throttle a system

  buffers have options
  - dropping buffers
  - sliding window buffer

  when you get nil from a channel its a signal the channel is closed

  alts!! gives the value and the channel from a set of channels

  timeout channel - we give it a time and after that time it closes using alt! with this can return nill when operation doesn't bring back a value

  alt!! has randomness built in so to avoid thread starvation

  What can I build with this

  problems with multi-threading

  tree of channels

  mult
  pub-sub

  )

(require '[core.async])
;; declare the channels
(def in-channel-one   (chan))
(def in-channel-two   (chan))
(def in-channel-three (chan))

(alts)
;; for use in go blocks
(alts!)
(alts!!)

(timeout )

(ns clojure-conj-talk.core
  (:refer-clojure :exclude [map reduce into partition partition-by take merge])
  (:require [clojure.core.async :refer :all :as async]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :as cheshire]))

;; The most basic primitive in Core.Async is the channel
;; We can wait until the put is finished by passing a callback
(put! c "Hello World" (fn [] (println "Done putting")))
(take! c (fn [v] (println "Got " v)))

;; Try the above again in reverse order to show dispatching
;;;;;;; Introducing <!! and >!! ;;;;;;

;; Well that's nice, but it's a pain to write code in that form,
;; so let's use something that uses promises:

(defn takep [c]
  (let [p (promise)]
    (take! c (fn [v] (deliver p v)))
    @p))

;; Now we can block the current thread waiting on the promise.
(future (println "Got!" (takep c)))

(put! c 42)


;; And we can do the reverse with put!

(defn putp [c val]
  (let [p (promise)]
    (put! c val (fn [] (deliver p nil)))
    @p))


(future (println "Done" (putp c 42)))

(future (println "Got!" (takep c)))


;; Well, that's exactly what clojure.core.async/<!! and >!! do

(future (println "Done" (>!! c 42)))

(future (println "Got! " (<!! c)))

;; But future doesn't really fit here, as it returns a promise, why not make
;; it return a channel? This is what core.async's thread macro does:

(thread 42)

(<!! (thread 42))

(thread (println "It works!" (<!! (thread 42))))

;;;;;;;; The Go Block ;;;;;;;;

;; That's all well and good, but who wants to tie up a thread
;; when we could use simple callbacks. This is what the go macro
;; does. It lets you write code that looks like the above code,
;; but it re-writes all your code to use callbacks.

(go 42)

(<!! (go 42))

(go (println "It works!" (<! (go 42))))

;; Wait....why did we use <!! Well <!! is simply a function that uses
;; blocking promises to wait for channel values. That would mess up the fixed
;; size thread pool that go blocks dispatch in. So we must define two sets
;; of operations

;; for thread, use <!! and >!! (blocking)

;; for go, use <! and >! (parking)



;; How do go blocks work? Let's take a look.

(pprint (macroexpand '(go 42)))

;; The code is rewritten as a state machine. Each block ends with a flow
;; control command to the next block. From this we can start/stop the state
;; machine as desired.

(pprint (macroexpand '(go (>! c (inc (<! c))))))


;; You really don't have to worry about most of this. Just accept that it works
;; and look up clojure core.async state machines on the internet if you want
;; more information.


;;;;; Buffer Types ;;;;;
;; Fixed length buffer
(def fbc (chan 1))

(go (>! fbc 1)
    (println "done 1"))
(go (>! fbc 2)
    (println "done 2"))

(<!! fbc)
(<!! fbc)

;; Dropping buffer (aka. drop newest)

(def dbc (chan (dropping-buffer 1)))

(go (>! dbc 1)
    (println "done 1"))

(go (>! dbc 2)
    (println "done 2"))

(<!! dbc) ;; returns 1

;; Sliding buffer (aka. drop oldest)

(def sbc (chan (sliding-buffer 1)))

(go (>! sbc 1)
    (println "done 1"))

(go (>! sbc 2)
    (println "done 2"))

(<!! sbc) ;; returns 2

;;; Closing a channel

(def c (chan))

(close! c)

(<!! c)

;;;; Alt & Timeout ;;;;

;; Sometimes we want to take the first available item from a bunch
;; of channels. For this we use alt! and alt!!

(def a (chan))
(def b (chan))

(put! a 42)

(alts!! [a b]) ;; returns [value chan]

;; Timeout is a channel that closes after X ms
;; (close! (chan 42))

(<!! (timeout 2000))

;; Often used with alt

(alts!! [a (timeout 1000)])

;; Alts can be used with writes

(alts!! [[a 42]
         (timeout 1000)])

;; We can also provide defaults for alts

(alts!! [a]
        :default :nothing-found)

;; By default, alts are tried in random order

(put! a :a) ;; Do this a few times
(put! b :b) ;; And this

(alts!! [a b]) ;; Notice the order


;; And again with :priority true

(put! a :a)
(put! b :b)

(alts!! [a b]
        :priority true)


;;;;; Logging Handler ;;;;;

(def log-chan (chan))

(thread
  (loop []
    (when-let [v (<!! log-chan)]
      (println v)
      (recur)))
  (println "Log Closed"))


(close! log-chan)

(defn log [msg]
  (>!! log-chan msg))

(log "foo")

;;;; Thread Pool Service


(defn thread-pool-service [ch f max-threads timeout-ms]
  (let [thread-count (atom 0)
        buffer-status (atom 0)
        buffer-chan (chan)
        thread-fn (fn []
                    (swap! thread-count inc)
                    (loop []
                      (when-let [v (first (alts!! [buffer-chan (timeout timeout-ms)]))]
                        (f v)
                        (recur)))
                    (swap! thread-count dec)
                    (println "Exiting..."))]
    (go (loop []
          (when-let [v (<! ch)]
            (if-not (alt! [[buffer-chan v]] true
                          :default false)
              (loop []
                (if (< @thread-count max-threads)
                  (do (put! buffer-chan v)
                      (thread (thread-fn)))
                  (when-not (alt! [[buffer-chan v]] true
                                  [(timeout 1000)] ([_] false))
                    (recur)))))
            (recur)))
        (close! buffer-chan))))

(def exec-chan (chan))
(def thread-pool (thread-pool-service exec-chan (fn [x]
                                                  (println x)
                                                  (Thread/sleep 5000)) 3 3000))



(>!! exec-chan "Hello World")


;;;;; HTTP Async ;;;;;;


(require '[org.httpkit.client :as http])

(defn http-get [url]
  (let [c (chan)]
    (println url)
    (http/get url
              (fn [r] (put! c r)))
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
