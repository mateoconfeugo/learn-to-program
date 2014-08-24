(ns lectures.forms-language-elements)
(comment
;; We use the term form to refer to structurally valid code.
9
"As a string I'm a valid form"
["data" "structures" "are" "forms"]
(defn so-are-functions [you bigot] (println "can not we all just co-exist"))

;; Special Forms
;; The main feature which makes special forms "special" is that they don't always evaluate all of their operands
(comment
(if boolean-form
  then-form
  optional-else-form)

;; unlike function calls. Image if evaluated all the operands like a function

(if (locked? door)
  (unlock door)
  (lock door))
)
;; The door would always end up locked

;; conditionals

(defn buy-spandex [] (println "stretch"))
(defn shop-as-normal [] (println "paper or plastic"))
(defn slap-a-kid [] (println "smack"))
(defn go-home [] (println "varoom!"))

(def store "walmart")

(if (= store "walmart")
  (buy-spandex )
  (shop-as-normal ))

;; do lets you "wrap up" multiple forms.

(if (= store "walmart")
  (do (buy-spandex)
      (slap-a-kid))
  (do (shop-as-normal)
      (go-home)))

;;The when operator is a combination of if and do, but with no else form

(when (= store "walmart")
  (println "Horrors!")
  (buy-spandex)
  (slap-a-kid)
  (go-home))

;; Naming things with def: use def to bind a name to a value
(def ham "ham!")
;; Other langauges  assign a value to a variable; however, in clojure you bind a name to a value.

(comment Datastructures
         * nil true false truthyness, Equality
         * Numbers http://clojure.org/data_structures#Data Structures-Numbers
         * Strings
         * Maps
         * Keywords
         * Vectors
         * Lists
         * Sets
         )

;;nil and friends

(nil? 1)
(nil? 0)
(nil? nil)
(nil? ())
(= 1 1)

;; Numbers
93
1.2
1/5

;; Strings
"Fallacies do not cease to be fallacies because they become fashions."
"An inconvenience is only an adventure wrongly considered; an adventure is an inconvenience rightly considered."
"The artistic temperament is a disease that afflicts amateurs."

;; Maps and keywords
(keyword "la-salsa")

(get {:nobu 1 :sandwich-shop 2} :nobu)
({:nobu 1 :sandwich-shop 2 :ham 3 } :ham )

(:ham {:nobu 1 :sandwich-shop 2 } (+ 1 1))
(hash-map :nobu 1 :sandwich-shop 2)

;; Also sorted maps ... later

;; Vectors
(def days-off ["new years" "presidents day" "memorial day"])
(get days-off 1)
(first days-off)
(rest days-off)

;; Lists
'("merchant" "general-public" "big-box")
'(1 2 3 4)
(list 1 2 3 4)
(conj '("merchant" "general-public" "big-box") "predatory")
;; Sets


(def suits #{:hearts :diamonds :spades :clubs})
(def values #{:ace :king :queen :jack 10 9 8 7 6 5 4 3 2 1})
(take 2 suits)

;; Symbols and Names

;;Symbols are identifiers that are normally used to refer to something
(def failed-biznuses ["charlies" "malibu-inn"])

(comment
Every programming language allows association between a name with a value.
Big difference with Lisps, however, allow manipulation symbols as data.
This is used a lot in  macros.
Functions can return symbols and take them as arguments
)

;; Quoting - A symbol returns the "object" it refers to
failed-biznuses
'failed-biznuses

(failed-biznuses 0)

;; Functions
(comment
Function definitions are comprised of five main parts
defn
A name
Optional a docstring
Parameters
 None
specified
 optional
keys
 rest
 Destructuring Parameters
The basic idea behind destructuring is that it lets you concisely bind symbols to values within a collection. Let's look at a basic example
)

(defn first-biz
  "Return the first element of a collection"
  [[biz-a]] ; <- Notice that biz is within a vector
  biz-a)

(first-biz ["vees" "nobu" "kfc"])

;; Here's how you would accomplish the same thing without destructuring:
(defn other-first-biz
  [biz-collection]
  (first biz-collection))
(other-first-biz ["mcdonalds" "la salsa"])

(comment
That vector is like a huge sign held up to Clojure which says, "Hey! This function is going to receive a list or a vector or a set as an argument. Make my life easier by taking apart the argument's structure for me and associating meaningful names with different parts of the argument!")

(defn biz-chooser
  [[first-choice second-choice & unimportant-choices]]
  (println (str "Your first choice is: " first-choice))
  (println (str "Your second choice is: " second-choice))
  (println (str "We're ignoring the rest of your choices. "
                "Here they are in case remember them when you are at la salsa: "
                (clojure.string/join ", " unimportant-choices))))

(biz-chooser ["sandwich shop" "vees" "mcdonalds" "la salsa"])

(comment
  You can also destructure maps. In the same way that you tell Clojure to destructure a vector or list by providing a vector as a parameter, you destucture maps by providing a map as a parameter
  )

(defn print-biz-location
  [{lat :lat lng :lng}]
  (println (str "Biz lat: " lat))
  (println (str "Biz lng: " lng)))
(print-biz-location {:lat 28.22 :lng 81.33})

;; We often want to just take keywords and "break them out" of a map, so there's a shorter syntax for that:

;; Works the same as above.
(defn redirect-to
  [{:keys [lat lng]}]
  (println (str "Biz lat: " lat))
  (println (str "Lat lng: " lng)))

(comment
You can retain access to the original map argument by using the :as keyword. In the example below, the original map is accessed with biz-location)

;; Works the same as above.
(defn navigate-to-biz-location
  [{:keys [lat lng] :as biz-location}]
  (println (str "Biz lat: " lat))
  (println (str "Biz lng: " lng))
  (redirect-to biz-location)
  (println biz-location))

(navigate-to-biz-location {:lat 28.22 :lng 81.33 :azimuth 200})
(comment
  In general, you can think of destructuring as instructing Clojure how to associate symbols with values in a list, map, set, or vector.
  )

(comment
** Arity
Functions can also be overloaded by arity. This means that a different function body will run depending on the number of arguments passed to a function.

(defn multi-arity
  ;; 3-arity arguments and body
  ([first-arg second-arg third-arg]
     (do-things first-arg second-arg third-arg))
  ;; 2-arity arguments and body
  ([first-arg second-arg]
     (do-things first-arg second-arg))
  ;; 1-arity arguments and body
  ([first-arg]
     (do-things first-arg)))
)

(comment
** The function body
Your function body can contain any forms. Clojure automatically returns the last form evaluated)

;; ** Anonymous Functions
;; This looks a lot like defn, doesn't it?
(fn [param-list] (+ 1 1))
(def ham (fn [] (+ 1 1)))

;; Example
(def biz-ratings (map (fn [biz] {(keyword biz) (rand)}) ["walmart" "target" "sears"]))
(def biz-ratings (map #({(keyword %) (rand)}) ["walmart" "target" "sears"]))

(def break-down (map rate-them biznuzes))

(defn rate-them [biz] {(keyword biz) (rand)})
(def biznuzes ["walmart" "target" "sears"])


(def mult-by-3 (fn [x] (* x 3)))
(mult-by-3 2)


(+  1 1 1 1 3 3)
;; Another example
((fn [x] (* x 3)) 82)

#(* % 3)
(#(* % 3) 8)

;; Another example


;; Break it down

;; tangent
(first (vals (first (filter :sears break-down)))) ; yuck
;; Same but not quite so yucky
(-> (filter :sears break-down) first vals first)


(def us-biz-depts [{:name "us-accounting" :personal 1}
                   {:name "us-marketing" :personal 2}
                   {:name "us-legal" :personal 5}
                   {:name "us-engineer" :personal 5}
                   {:name "us-it" :personal 5}])

(defn has-matching-dept?
  [department]
  (re-find #"^us-" (:name department)))

(defn matching-dept
  [department]
  {:name (clojure.string/replace (:name department) #"^us-" "international-")
   :personal (:personal department)})

(defn augment-department-personal
  "Expects a seq of maps which have a :name and :personal"
  [biz-depts]
  (loop [remaining-biz-depts biz-depts
         final-biz-depts []]
    (if (empty? remaining-biz-depts) ;; common recursive pattern
      final-biz-depts
      (let [[dept & remaining] remaining-biz-depts
            final-biz-depts (conj final-biz-depts dept)]
        (if (has-matching-dept? dept)
          (recur remaining (conj final-biz-depts (matching-dept dept)))
          (recur remaining final-biz-depts))))))

(augment-department-personal us-biz-depts)

;; Let
;; bind the names on the left to the values on the right.
;; Introduces a new scope.
;; can use rest params
(let [[biz & bizs] biznuzes]
  [biz bizs])

;; let forms provide parameters and their arguments side-by-side
;; clarity in naming things
;; evaluate an expression only once and re-use the result

;; Loop and Recur
(loop [i 0]
  (println (str "i: " i))
  (if (> i 1)
    (println "Adios!")
    (recur (inc i))))

;; Same thing using functions:
(defn repeater
  ([]
     (repeater 0))
  ([i]
     (println i)
     (if (> i 3)
       (println "Adios!")
       (repeater (inc i)))))

(repeater)

;; Regexs
;; pound, open quote, close quote
;; #"regular-expression"
(doseq [biz us-biz-depts]
  (when (has-matching-dept? biz)
    (println (:personal biz))))

;; Reduce - process each element in a sequence and build a result

;; sum with reduce
(reduce + [1 2 3 4 5])

(reduce + 10 [1 2 3 4 6 7] )

(defn my-fn
  [result {:keys [name style]}]
  (update-in result [style] conj name))

(def categorized-by-style (reduce my-fn {}
        [{:name "nobu", :current-city "malibu", :style :sushi}
         {:name "la-salsa", :current-city "malibu", :style :mexican}
         {:name "sandwich-shop", :current-city "malibu", :style :deli}
         {:name "mcdonalds", :current-city "malibu", :style :burger}
         {:name "jack-in-the-box", :current-city "malibu", :style :burger}]))

(def cbs categorized-by-style)
(:burger cbs)


;; recursion
;; arity
;; building a result
;; option arguments
;; let
;; conditional
;; list/seq operations
(defn my-reduce
  ([f initial coll]
     (loop [result initial
            remaining coll]
       (let [[current & rest] remaining]
         (if (empty? remaining)
           result
           (recur (f result current) rest)))))
  ([f [head & tail]]
     (my-reduce f (f head (first tail)) (rest tail))))

(defn better-augment-biz-depts
  "Expects a seq of maps which have a :name and :size"
  [biz-depts]
  (reduce (fn [final-biz-depts dept]
            (let [final-biz-depts (conj final-biz-depts dept)]
              (if (has-matching-dept? dept)
                (conj final-biz-depts (matching-dept dept))
                final-biz-depts)))
          []
          biz-depts))

(better-augment-biz-depts us-biz-depts)

;; MBA training
(defn which-department
  [biz-depts]
  (let [all-depts (better-augment-biz-depts biz-depts)
        biz-dept-personal-sum (reduce + 0 (map :personal all-depts))
        target (inc (rand biz-dept-personal-sum))]
    (loop [[dept & rest] all-depts
           accumulated-personal (:personal dept)]
      (if (> accumulated-personal target)
        dept
        (recur rest (+ accumulated-personal (:personal dept)))))))

(defn cut [dept amount limit]
  (if (< limit (:personal dept)) (- (:personal dept) amount) (:personal dept)))

(defn masters-thesis
  [depts amount limit]
  (let [victim (which-department depts)]
    { :name (:name victim) :personal (cut victim amount limit)}))

;; go forth intrepid MBA
(masters-thesis us-biz-depts 2 2)
)
