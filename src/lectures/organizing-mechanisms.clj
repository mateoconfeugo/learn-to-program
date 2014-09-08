(ns learn-clojure.organizing-mechanisms
  (:require
   [overtone.at-at :as at]
   [clojure-contrib :refer [pprint]]
   [stockings.core :refer [get-quote]]))

(use 'stockings.core)
()

;; Would like to demostrate

;; Data structures
(def data-list (ref []))

;; stocks and options

(defn initialize-pool [] (defonce my-pool (at/mk-pool)))

(defn schedule-task
  "Scedule a task to be performed. Options (and defaults) are {:msec 1000 :sec 60 :min 1}"
  [options task-fn]
  (let [opts (merge {:msec 1000 :sec 60 :min 1} options)
        msec (:msec opts)
        sec (:sec opts)
        min (:min opts)]
    (at/every
     (* min sec msec)
     task-fn
     my-pool)))


(defn handler-fn [evt] (dosync (alter data-list conj evt)))

(defn subscribe-to-market [fn])

(subscribe-to-market handler-fn)

(def connect-result (market/connect-to-market))

(def request-historical-data [])

(request-historical-data (:esocket connect-result) 0 "IBM" "1 D" "1 secs" "TRADES")


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

  Go Blocks:

  Threads have both an overhead and a startup cost,
  which is why most modern programs avoid creating threads directly and use a thread pool instead
  In particular, they are problematic if the code we want to run might block.

  The Problem with Blocking Thread pools are a great way to handle CPU-intensive tasks—
  those that tie a thread up for a brief period and then return it to the pool to be reused.

  But what if we want to do something that involves communication?
  Blocking a thread ties it up indefinitely, eliminating much of the value of using a thread pool.

  Go blocks allow multiple concurrent tasks to be efficiently multiplexed across a limited pool of threads.

  Code within a go block is transformed into a state machine.
  Instead of blocking when it reads from or writes to a channel,
  the state machine parks, relinquishing control of the thread it’s executing on.
  When it’s next able to run, it performs a state transition and continues execution, potentially on another thread.

  IO is one area where asynchronous code comes into its own—
  instead of the traditional approach of having a thread per connection,
  asynchronous IO allows us to start a number of operations
  and receive a notification whenever one of them has data available.
  Although this is a powerful approach, it can be challenging,
  with code tending to turn into a mess of callbacks calling callbacks.

  core.async can make it much easier by solving these problems relating to  concurrency and composition/modularity
  )

;; core.async
"There comes a time in all good programs when components or subsystems must stop communicating directly with one another."
(comment
The objectives of core.async are:

To provide facilities for independent threads of activity, communicating via queue-like channels
To support both real threads and shared use of thread pools (in any combination), as well as ClojureScript on JS engines
To build upon the work done on CSP and its derivatives


by default channels are synchronous or unbuffered writing to a channel blocks until something reads from it.

go is a macro that takes its body and examines it for any channel operations.
It will turn the body into a state machine. Upon reaching any blocking operation,
the state machine will be 'parked' and the actual thread of control will be released

The fundamental operations on channels are putting and taking values.

Both of those operations potentially block,
but the nature of the blocking depends on the nature of the thread of control in which the operation is performed.

core.async supports two kinds of threads of control
- ordinary threads and IOC (inversion of control) 'threads'. Ordinary threads can be created in any manner
, but IOC threads are created via go blocks. Because JS does not have threads,
  - only go blocks and IOC threads are supported in ClojureScript.
  Channels can be ordinary threads using the thread macro and should be preferred over future for channel work


The primary channel operations within go blocks are >! ('put') and <! ('take').
The go block itself immediately returns a channel,
on which it will eventually put the value of the last expression of the body (if non-nil),
and then close

As with STM, the pervasive use of persistent data structures offers particular benefits for CSP-style channels.
In particular, it is always safe and efficient to put a Clojure data structure on a channel,
without fear of its subsequent use by either the producer or consumer.
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

;; merge and mix
;; merge is fine when you want to just grab a bunch of channels and treat them as one
(def merged (core.async/merge [in-channel-one in-channel-two in-channel-three]))

;; mix is more complicated
;; It introduces an intermediary component - the mixer
;; It is configurable, you can add and remove input channels
;; Channels can be muted, paused and solo'ed on demand

;; manually declare our output channel
(def output-channel (chan))
;; create a mixer linked to the output channel
(def mixer (mix output-channel))

(go (loop []
      (println (<! output-channel))
            (recur)))

;; Unlike merge we still haven't declared what input channels should be associated with the mixer and ultimately output channel. We can do this using admix,

(admix mixer in-channel-one)
(admix mixer in-channel-two)
(admix mixer in-channel-three)

;; toggle allows you to control how the mixer responds to each input channel.
;; You pass it a state map of channels and associated mixer properties.
(comment
:mute - keep taking from the input channel but discard any taken values
:pause - stop taking from the input channel
:solo - listen only to this (and other :soloed channels). Whether or not the non-soloed channels are muted or paused can be controlled via the solo-mode method.
)

;; mute channel one
(toggle mixer {  in-channel-one { :mute true}})


;; operations over channels

;; wine cheese wine

;; macros

;; storm
;; trident using marciline

;; lambda architecture
