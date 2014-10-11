;; INTRODUCTION TO MACROS

(comment  CLOJURE EVALUATION MODEL

         "CLOJURE EVALUATES DATA STRUCTURES TO VALUES!!!!"

  reader ---> data structures  ---> macroexpansion
                    |                   |
                    v                   v

                  evaluation  <------  data stucture

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
