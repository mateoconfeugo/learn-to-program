(ns lectures.dsl)

(defn annotate [node]
  node)

(defmacro transformation
  ([] `identity)
  ([form] form)
   ([form & forms] `(fn [node#] (at node# ~form ~@forms))))

(defn- bodies [forms]
  (if (vector? (first forms))
    (list forms)
    forms))

(defmacro build-step [nodes & body]
  (let [nodesym (gensym "nodes")]
    `(let [~nodesym (map annotate ~nodes)]
       (fn ~@(for [[args & forms] (bodies body)]
              `(~args
                (doall (flatmap (transformation ~@forms) ~nodesym))))))))

(defmacro defbuild-step
  [specs]
  (let [build-sym (gensym "build-step")]
          ~@(for [[args & forms] specs]
              `(def ~name (snippet ~build-sym ~selector ~args ~@forms)))))


(build-step [:foo] (pprint "what"))

(macroexpand-1 '(build-step :foo (pprint "what")))
(def ham (build-step :foo (pprint "what")))

;; devops engineer writes something like using my dsl
(def ci-spec (defjob
               (defbuild
                 (defstep
                   (>! monitor "compiling app")
                   (pprint "lein compile"))
                 (defstep (pprint "lein test"))
                 (defstep (pprint "lein uberjar"))
                 (defstep (pprint "lein deploy private"))
                 (defstep (pprint "lien pallet up")))
               (defpublisher
                 (publish (email "a@b.com"))
                 (publish (report
                             (let [results (<! build-results)]
                               create-report))))))
;; without the idomatic def to indicate a macro
;; maybe for DSL its correct to that because its
;; self-evident these are macros as they are being
;; used in part and context of the DSL
(def ci-spec (job
              (parameters :commit :release)
              (scm :git (:commit parameters))
              (build
               (step
                   (>! monitor "compiling app")
                   (pprint "lein compile"))
                 (step (pprint "lein test"))
                 (step (pprint "lein uberjar"))
                 (step (pprint "lein deploy private"))
                 (step (pprint "lien pallet up")))
               (publishers
                 (publish (email "a@b.com"))
                 (publish (report
                             (let [results (<! build-results)]
                               create-report))))))

;; upon calling
(def ci-job (ci-spec {:commit 123}))

;; what should this expand to

(def desired-expandition {:riemann (fnk [ip]
                                        (tcp-client {:host ip}))

                          :monitor (fnk [riemann]
                                        (let [mc (chan)]
                                          (go  (while true
                                                 (when-let [x (<! mc)]
                                                   (send-event riemann x))))
                                          mc))
                          :build-results (fnk [input] (chan))
                          :step2xx (fnk [input monitor]
                                        (let [output (chan)]
                                          (when-let [input-data (go (<! input))]
                                            (do (go (>! monitor input-data)
                                                    (pprint "lein compile"))
                                                (go (>! output [:build-done {:message "completed build"}])))
                                            output)))
                          :step1xx (fnk [input monitor step2xx]
                                        (let [output (chan)]
                                          (go (while true
                                                (when-let [[val ch] (alts! input)]
                                                  (do (>! monitor val)
                                                      (pprint "lein compile")
                                                      (>! step2xx [:step2xx {:message "transition to step 2xx"}])))))
                                          output))
                          :publisher2xx (fnk [build-results monitor]
                                             (let [output (chan)]
                                               (when-let [input-data (go (<! input))]
                                                 (do (go (>! monitor input-data)
                                                         (pprint "lein compile"))
                                                     (go (>! output [:build-done {:message "completed build"}])))
                                                 output)))
                          :publish1xx (fnk [build-results monitor publisher2xx]
                                           (let [output (chan)]
                                             (go (while true
                                                   (when-let [[val ch] (alts! input)]
                                                     (do (>! monitor val)
                                                         (pprint "lein compile")
                                                         (>! step2xx [:publisher2xx {:message "transition to step 2xx"}])))))))
                          })


(comment

(use 'riemann.client)


(def test-monitored-job) (merge {:riemann (fnk [ip]
                                               (tcp-client {:host ip}))
                                 :monit (fnk [riemann]
                                             (let [mc (chan)]
                                               (go  (while true
                                                      (when-let [x (<! mc)]
                                                        (send-event riemann x))))
                                               mc))}
                                (defjob name
                                  [:monitor] (#(pprint %) msg-data)
                                  [:build-in] (#(pprint %) msg-data)))
;; (merge {:monit (fnk [ip]) (tcp-client {:host ip})}

;;               [:build-in] (#(pprint %) msg-data))
(defn new-job [name]
  (let [spec (defjob name
               [:monitor] (#(pprint %) msg-data)
               (build-step :build-in (pprint msg-data)))
        build-fn (graph/eager-compile spec)]
  (build-fn {:build-number 2})))


  {
  :step3xx (fnk [input monitor]
               (let [output (chan)]
                 (when-let [input-data (go (<! input))]
                   (pprint "lein uberjar"))
                 output))
:step4xx (fnk [input monitor]
               (let [output (chan)]
                 (when-let [input-data (go (<! input))]
                   (pprint "lein deploy private"))
                 output))
:step5xx (fnk [input monitor]
               (let [output (chan)]
                 (when-let [input-data (go (<! input))]
                   (pprint "lein pallet up"))
                 output))
 :channels (fnk [step1xx step2xx])
 :build-workflow (fnk [channels]
                      (go (while true
                            (when-let [[val step-ch] (alts! channels)]
                              (let [[msg-event-token msg-data] (take 2 val)]
                                (do (match [msg-event-token]
                                           [:steplxx] (#(>! step2xx [:step2xx {:message "1 -> 2"}]) msg-data)
                                           [:step2xx] (#(>! output [:output {:message "2 -> done"}] msg-data))))))))) })
