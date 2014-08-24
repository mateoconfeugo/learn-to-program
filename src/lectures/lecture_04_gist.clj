(ns lectures.lecture_04_gist)


;; Data collections, structures and their abstractions

;; Distinctive characteristics
;; 1.  Used in terms of abstractions on in concrete implementations
;; 2.  They are immutable and persistent

(comment
 Each data structure has its own charateristics and idioms
 More important to internalize the above two points and what they imply about clojure,
 its data structures and how you should design your app.

 It is better to have 100 functions working on one data structure than 10 functions on 10 data structure - Alan J. Perlis

 Clojure takes it a step further and believes that its even better to have 100 functions work on 1 abastraction
 )

(def m {:a 5 :b 6})
(def v [1 2 3])
(def s #{1 3 2})

(conj v 4)
(conj s 2)
(conj m [:c 7])

(comment
  There are seven different primary abstractions in which clojure data structures participate
  * Collection
  conj
  seq
  count
  empty
  * Sequence
  Traverse  some  values from  either a collections or a computation
  ** All clojure collection types
  ** All java collections
  ** All java maps
  ** All java.lang.CharSequences including Strings
  ** java.lang.Iterable
  ** Arrays
  ** nil
  ** clojure.lang.Seqable
  first
  rest
  next
  cons
  list*
  * Associative
  assoc
  dissoc
  contains?
  * Indexed
  nth
  get
  * Stacked
  conj -> reused for push
  pop
  peek
  * Set
  disj
  clojure.set => subset? supset union intersection project and more!
  * Sorted
  rseq
  subseq
  rsubseq
  )

(comment
  Lazy Sequences
  Produce results on command when the consumer tries to use the value.  This value is realized only once.

  Use the lazy-seq macro to create a lazy seqence

  )
(defn random-ints
  [limit]
  (lazy-seq
   (println "realizing a random number")
   (cons (rand-int limit)
         (random-ints limit))
   ))

(def rands (take 10 (random-ints 50)))

(first rands)

(nth rands 3)
(nth rands 2)
(count rands)

;; list

;; map
;-----
(def playlist
  [{:title "Elephant", :artist "The White Stripes", :year 2003}
   {:title "Helioself", :artist "Papas Fritas", :year 1997}
   {:title "Stories from the City, Stories from the Sea",
    :artist "PJ Harvey", :year 2000}
   {:title "Buildings and Grounds", :artist "Papas Fritas", :year 2000}
   {:title "Zen Rodeo", :artist "Mardi Gras BB", :year 2002}])


;-----
(map :title playlist)
;= ("Elephant" "Helioself" "Stories from the City, Stories from the Sea"
;=  "Buildings and Grounds" "Zen Rodeo")


;-----
(defn summarize [{:keys [title artist year]}]
  (str title " / " artist " / " year))


;-----
(group-by #(rem % 3) (range 10))
;= {0 [0 3 6 9], 1 [1 4 7], 2 [2 5 8]}


;-----
(group-by :artist playlist)
;= {"Papas Fritas" [{:title "Helioself", :artist "Papas Fritas", :year 1997}
;=                  {:title "Buildings and Grounds", :artist "Papas Fritas"}]
;=  ...}


;-----
(into {} (for [[k v] (group-by :artist playlist
                      )]
           [k (summarize v)]))


;-----
(defn reduce-by
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
    {} coll))


;-----
(def orders
  [{:product "Clock", :customer "Wile Coyote", :qty 6, :total 300}
   {:product "Dynamite", :customer "Wile Coyote", :qty 20, :total 5000}
   {:product "Shotgun", :customer "Elmer Fudd", :qty 2, :total 800}
   {:product "Shells", :customer "Elmer Fudd", :qty 4, :total 100}
   {:product "Hole", :customer "Wile Coyote", :qty 1, :total 1000}
   {:product "Anvil", :customer "Elmer Fudd", :qty 2, :total 300}
   {:product "Anvil", :customer "Wile Coyote", :qty 6, :total 900}])

;-----
(reduce-by :customer #(+ %1 (:total %2)) 0 orders)
;= {"Elmer Fudd" 1200, "Wile Coyote" 7200}


;-----
(reduce-by :product #(conj %1 (:customer %2)) #{} orders)
;= {"Anvil" #{"Wile Coyote" "Elmer Fudd"},
;=  "Hole" #{"Wile Coyote"},
;=  "Shells" #{"Elmer Fudd"},
;=  "Shotgun" #{"Elmer Fudd"},
;=  "Dynamite" #{"Wile Coyote"},
;=  "Clock" #{"Wile Coyote"}}


;-----
(fn [order]
  [(:customer order) (:product order)])

#(vector (:customer %) (:product %))

(fn [{:keys [customer product]}]
  [customer product])

(juxt :customer :product)


;-----
(reduce-by (juxt :customer :product)
  #(+ %1 (:total %2)) 0 orders)
;= {["Wile Coyote" "Anvil"] 900,
;=  ["Elmer Fudd" "Anvil"] 300,
;=  ["Wile Coyote" "Hole"] 1000,
;=  ["Elmer Fudd" "Shells"] 100,
;=  ["Elmer Fudd" "Shotgun"] 800,
;=  ["Wile Coyote" "Dynamite"] 5000,
;=  ["Wile Coyote" "Clock"] 300}


;-----
(defn reduce-by-in
  [keys-fn f init coll]
  (reduce (fn [summaries x]
            (let [ks (keys-fn x)]
              (assoc-in summaries ks
                (f (get-in summaries ks init) x))))
    {} coll))


;-----
(reduce-by-in (juxt :customer :product)
  #(+ %1 (:total %2)) 0 orders)
;= {"Elmer Fudd" {"Anvil" 300,
;=                "Shells" 100,
;=                "Shotgun" 800},
;=  "Wile Coyote" {"Anvil" 900,
;=                 "Hole" 1000,
;=                 "Dynamite" 5000,
;=                 "Clock" 300}}


;-----
(def flat-breakup
  {["Wile Coyote" "Anvil"] 900,
   ["Elmer Fudd" "Anvil"] 300,
   ["Wile Coyote" "Hole"] 1000,
   ["Elmer Fudd" "Shells"] 100,
   ["Elmer Fudd" "Shotgun"] 800,
   ["Wile Coyote" "Dynamite"] 5000,
   ["Wile Coyote" "Clock"] 300})


;-----
(reduce #(apply assoc-in %1 %2) {} flat-breakup)
;= {"Elmer Fudd" {"Shells" 100,
;=                "Anvil" 300,
;=                "Shotgun" 800},
;=  "Wile Coyote" {"Hole" 1000,
;=                 "Dynamite" 5000,
;=                 "Clock" 300,
;=                 "Anvil" 900}}
