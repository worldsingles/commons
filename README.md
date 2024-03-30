# ws-commons [![Clojars Project](http://clojars.org/worldsingles/ws-commons/latest-version.svg)](http://clojars.org/worldsingles/ws-commons) [![cljdoc badge](https://cljdoc.org/badge/worldsingles/ws-commons)](https://cljdoc.org/d/worldsingles/ws-commons/CURRENT)

Common utility functions and "extensions" to Clojure.

## Usage

`deps.edn`:

``` clojure
clj -Sdeps '{:deps {worldsingles/ws-commons {:mvn/version "0.1.4"}}}'
```

Leiningen / Boot Dependency:

``` clojure
[worldsingles/ws-commons "0.1.4"]
```

* `condp->` -- an extension to `cond->` that threads the expression through the predicate(s) as well as the result(s).
* `condp->>` -- an extension to `cond->>` that threads the expression through the predicate(s) as well as the result(s).
* `condq` -- a version of `condp` that accepts a unary predicate and omits the `expr` (that `condp` uses as the second argument to the predicate).
* `dissoc-all` -- an extension to `dissoc` that `dissoc`'s a sequence of keys.
* `flip` -- a companion to `partial` that allows the first argument to be omitted (rather than the trailing arguments). Inspired by Haskell's `flip`.
* `interleave-all` -- an extension to `interleave` that uses all elements of the longer sequence argument(s).")

A local binding capturing macro:

* `local-map` -- returns a hash map containing all local symbols (as keyword keys) with their associated values; note that this can include auto-generated locals too!

In addition, some syntactic sugar for `java.util.concurrent.CompletableFuture`:

* `completable` -- like `future` but produces a `CompletableFuture` instead.
* `exceptionally` -- applied to a `CompletableFuture` and a function, will execute the function on any exception produced by the future.
* `then` -- applied to a `CompletableFuture` and a function, will execute the function on the result of the future.

## Examples

``` clojure
user=> (require '[ws.clojure.extensions :refer :all])
nil

;; threading examples:

user=> (defn f [n]
         (condp-> n
           even?   (* 3)
           (> 100) (- 100)))
#'user/f
user=> (f 22)
66 ; (* 22 3) = 66, (> 66 100) = false
user=> (f 66)
98 ; (* 66 3) = 198, (> 198 100) = true, (- 198 100)
user=> (defn g [n]
         (condp->> n
           even?   (* 3)
           (> 100) (- 100)))
#'user/g
user=> (g 22)
34 ; (* 3 22) = 66, (> 100 66) = true, (- 100 66) = 34
user=> (g 66)
198 ; (* 3 66) = 198, (> 100 198) = false

;; local-map examples:

user=> (let [a 1 b 2] (local-map)))
{:a 1, :b 2}
user=> (let [a 1 b 2] (local-map :only :a)))
{:a 1}
user=> (let [a 1 b 2] (local-map :only "a" b))
{:a 1, :b 2}
user=> (let [a 1 b 2] (local-map :without :a))
{:b 2}
user=> (defn foo [a b] ; a and b are also captured
         (let [c 1 d (+ a b)]
           {:all (local-map)
            :a-d (local-map :only :a :d)
            :b-c (local-map :without :a :d)}))
#'user/foo
user=> (foo 13 42)
{:all {:a 13, :b 42, :c 1, :d 55}
 :a-d {:a 13, :d 55}
 :b-c {:b 42, :c 1}}
user=> (let [{:keys [a b]} {:a 1, :b 2}] (local-map))
;; this will include the auto-generated :map# destructuring key as well
{:map__41744 {:a 1, :b 2}, :a 1, :b 2}

;; CompletableFuture examples:

user=> (deref (completable (Thread/sleep 5000) 42))
42 ; produces 42 after 5 seconds
user=> (deref (completable (Thread/sleep 5000) 42) 1000 13)
13 ; produces 13 after 1s
user=> (-> (completable (Thread/sleep 5000) 42)
           (then #(/ 100 %))
           (exceptionally ex-message)
           (deref))
50/21 ; produces 50/21 after 5 seconds
user=> (-> (completable (Thread/sleep 5000) 0)
           (then #(/ 100 %))
           (exceptionally ex-message)
           (deref)))
"java.lang.ArithmeticException: Divide by zero" ; produce "Divide by zero" after 5s
user=>
```

## License

Copyright © 2016-2024 [World Singles Networks llc](https://worldsinglesnetworks.com/).

Distributed under the Eclipse Public License version 1.0.
