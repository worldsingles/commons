# ws-commons [![Clojars Project](http://clojars.org/worldsingles/ws-commons/latest-version.svg)](http://clojars.org/worldsingles/ws-commons) [![cljdoc badge](https://cljdoc.org/badge/worldsingles/ws-commons)](https://cljdoc.org/d/worldsingles/ws-commons/CURRENT)

Common utility functions and "extensions" to Clojure.

## Usage

`deps.edn`:

``` clojure
clj -Sdeps '{:deps {worldsingles/ws-commons {:mvn/version "0.1.2"}}}'
```

Leiningen / Boot Dependency:

``` clojure
[worldsingles/ws-commons "0.1.2"]
```

* `condp->` -- an extension to `cond->` that threads the expression through the predicate(s) as well as the result(s).
* `condp->>` -- an extension to `cond->>` that threads the expression through the predicate(s) as well as the result(s).
* `condq` -- a version of `condp` that accepts a unary predicate and omits the `expr` (that `condp` uses as the second argument to the predicate).
* `dissoc-all` -- an extension to `dissoc` that `dissoc`'s a sequence of keys.
* `flip` -- a companion to `partial` that allows the first argument to be omitted (rather than the trailing arguments). Inspired by Haskell's `flip`.
* `interleave-all` -- an extension to `interleave` that uses all elements of the longer sequence argument(s).")

In addition, some syntactic sugar for `java.util.concurrent.CompletableFuture`:

* `completable` -- like `future` but produces a `CompletableFuture` instead.
* `exceptionally` -- applied to a `CompletableFuture` and a function, will execute the function on any exception produced by the future.
* `then` -- applied to a `CompletableFuture` and a function, will execute the function on the result of the future.


``` clojure
user=> (require '[ws.clojure.extensions :refer :all])
nil
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

## Releases

0.1.2 -- Jul 24, 2019 -- adds `completable`, `then`, `exceptionally`.
0.1.1 -- Dec 05, 2018 -- first public release.

## License

Copyright © 2016-2019 [World Singles Networks llc](https://worldsinglesnetworks.com/).

Distributed under the Eclipse Public License version 1.0.
