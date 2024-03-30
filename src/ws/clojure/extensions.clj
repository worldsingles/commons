;; copyright (c) 2017-2024 world singles networks llc

(ns ws.clojure.extensions
  "A small library of useful 'language extensions' -- things are 'like'
  existing clojure.core functionality but with a small, useful extension.

  condp-> -- an extension to cond-> that threads the expression
    through the predicate(s) as well as the result(s).
  condp->> -- an extension to cond->> that threads the expression
    through the predicate(s) as well as the result(s).
  condq -- a version of condp that accepts a unary predicate and omits the
    expr (that condp uses as the second argument to the predicate).
  dissoc-all -- an extension to dissoc that dissoc's a sequence of keys.
  flip -- a companion to partial that allows the first argument to be
    omitted (rather than the trailing arguments). Inspired by Haskell's flip.
  interleave-all -- an extension to interleave that uses all elements
    of the longer sequence argument(s).
  completable, then, exceptionally -- macros providing syntactic sugar over
    Java's CompletableFuture.
  local-map -- produce a hash map from all, or a subset of, local bindings.")

(set! *warn-on-reflection* true)

(defmacro condp->
  "Takes an expression and a set of predicate/form pairs. Threads expr (via ->)
  through each form for which the corresponding predicate is true of expr.
  Note that, unlike cond branching, condp-> threading does not short circuit
  after the first true test expression."
  [expr & clauses]
  (assert (even? (count clauses)))
  (let [g (gensym)
        pstep (fn [[pred step]] `(if (-> ~g ~pred) (-> ~g ~step) ~g))]
    `(let [~g ~expr
           ~@(interleave (repeat g) (map pstep (partition 2 clauses)))]
       ~g)))

(defmacro condp->>
  "Takes an expression and a set of predicate/form pairs. Threads expr (via ->>)
  through each form for which the corresponding predicate is true of expr.
  Note that, unlike cond branching, condp->> threading does not short circuit
  after the first true test expression."
  [expr & clauses]
  (assert (even? (count clauses)))
  (let [g (gensym)
        pstep (fn [[pred step]] `(if (->> ~g ~pred) (->> ~g ~step) ~g))]
    `(let [~g ~expr
           ~@(interleave (repeat g) (map pstep (partition 2 clauses)))]
       ~g)))

(defmacro condq
  "Takes a unary predicate, and a set of clauses.
  Each clause can take the form of either:

  test-expr result-expr

  test-expr :>> result-fn

  Note :>> is an ordinary keyword.

  For each clause, (pred test-expr) is evaluated. If it returns
  logical true, the clause is a match. If a binary clause matches, the
  result-expr is returned, if a ternary clause matches, its result-fn,
  which must be a unary function, is called with the result of the
  predicate as its argument, the result of that call being the return
  value of condq. A single default expression can follow the clauses,
  and its value will be returned if no clause matches. If no default
  expression is provided and no clause matches, an
  IllegalArgumentException is thrown."
  {:copyright "Rich Hickey, since this is a modified version of condp"}
  [pred & clauses]
  (let [gpred (gensym "pred__")
        emit (fn emit [pred args]
               (let [[[a b c :as clause] more]
                     (split-at (if (= :>> (second args)) 3 2) args)
                     n (count clause)]
                 (cond
                  (= 0 n) `(throw (IllegalArgumentException. (str "No matching clause: " ~pred)))
                  (= 1 n) a
                  (= 2 n) `(if (~pred ~a)
                             ~b
                             ~(emit pred more))
                  :else `(if-let [p# (~pred ~a)]
                           (~c p#)
                           ~(emit pred more)))))]
    `(let [~gpred ~pred]
       ~(emit gpred clauses))))

(defn dissoc-all
  "Given a map and a sequence of keys, dissoc them all."
  [m ks]
  (apply dissoc m ks))

(defn flip
  "Like partial except you supply everything but the first argument.
  Also like Haskell's flip for single arity call."
  ([f] (fn [b a] (f a b)))
  ([f b] (fn [a] (f a b)))
  ([f b c] (fn [a] (f a b c)))
  ([f b c d & more]
   (fn [a] (apply f a b c d more))))

(defn interleave-all
  "Like interleave, but stops when the longest seq is done, instead of
   the shortest."
  {:copyright "Rich Hickey, since this is a modified version of interleave"}
  ([] ())
  ([c1] (lazy-seq c1))
  ([c1 c2]
   (lazy-seq
    (let [s1 (seq c1) s2 (seq c2)]
      (cond
       (and s1 s2) ; there are elements left in both
       (cons (first s1) (cons (first s2)
                              (interleave-all (rest s1) (rest s2))))
       s1 ; s2 is done
       s1
       s2 ; s1 is done
       s2))))
  ([c1 c2 & colls]
   (lazy-seq
    (let [ss (filter identity (map seq (conj colls c2 c1)))]
      (concat (map first ss) (apply interleave-all (map rest ss)))))))

(defmacro completable
  "Return a CompletableFuture that will evaluate the body asynchronously.

  Can be deref'd, future-cancel'd, etc. Call then (below) on this to provide
  a function to call when the future completes."
  [& body]
  `(java.util.concurrent.CompletableFuture/supplyAsync
    (reify java.util.function.Supplier
      (~'get [_#] ~@body))))

(defn then
  "Given a CompletableFuture and a function, when the future completes,
  invoke the function on its result."
  [^java.util.concurrent.CompletableFuture cf f]
  (.thenApply cf
              (reify java.util.function.Function
                (apply [_ v] (f v)))))

(defn exceptionally
  "Given a CompletableFuture and a function, if the future completes
  with an exception, invoke the function on that exception."
  [^java.util.concurrent.CompletableFuture cf f]
  (.exceptionally cf
                  (reify java.util.function.Function
                    (apply [_ v] (f v)))))

(defmacro local-map
  "Turn all (or a subset of) locals into a hash map with keys named for
  their symbols (and their values).

  For the subset case, `op` can be `:with` (or `:only`) or `:without`.
  The latter will use all the local bindings except the named ones; the
  former will use just the specified locals. `ks` can be bare symbols or
  keywords or strings.

  Inspired by an example from @noisesmith on Slack."
  ([]
   (into {}
         (map (juxt (comp keyword name) identity))
         (keys &env)))
  ([op & ks]
   (if (= :without op)
     (into {}
           (comp (remove (set (map (comp symbol name) ks)))
                 (map (juxt (comp keyword name) identity)))
           (keys &env))
     (into {}
           (map (juxt (comp keyword name) (comp symbol name)))
           ks))))

(comment
  (deref (completable (Thread/sleep 5000) 42)) ; produces 42 after 5 seconds
  (deref (completable (Thread/sleep 5000) 42) 1000 13) ; produces 13 after 1s
  (-> (completable (Thread/sleep 5000) 42) ; produces 50/21 after 5 seconds
      (then #(/ 100 %))
      (exceptionally ex-message)
      (deref))
  (-> (completable (Thread/sleep 5000) 0) ; produce "Divide by zero" after 5s
      (then #(/ 100 %))
      (exceptionally ex-message)
      (deref)))
