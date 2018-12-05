# env-repl [![Clojars Project](http://clojars.org/worldsingles/ws-commons/latest-version.svg)](http://clojars.org/worldsingles/ws-commons)

Common utility functions and "extensions" to Clojure.

## Usage

`deps.edn`:

``` clojure
clj -Sdeps '{:deps {worldsingles/ws-commons {:mvn/version "0.1.0"}}}'
```

Leiningen / Boot Dependency:

``` clojure
[worldsingles/ws-commons "0.1.0"]
```

* `condp->` -- an extension to `cond->` that threads the expression through the predicate(s) as well as the result(s).
* `condp->>` -- an extension to `cond->>` that threads the expression through the predicate(s) as well as the result(s).
* `condq` -- a version of `condp` that accepts a unary predicate and omits the `expr` (that `condp` uses as the second argument to the predicate).
* `dissoc-all` -- an extension to `dissoc` that `dissoc`'s a sequence of keys.
* `flip` -- a companion to `partial` that allows the first argument to be omitted (rather than the trailing arguments). Inspired by Haskell's `flip`.
* `interleave-all` -- an extension to `interleave` that uses all elements of the longer sequence argument(s).")

``` clojure
user=> (require '[ws.clojure.extensions
                  :refer [condp-> condp->> condq dissoc-all flip interleave-all]])
nil
user=> (defn example [n]
         (condp-> n
           even?   (* 3)
           (> 100) (- 100)))
#'user/example
user=> (example 22)
66
user=> (example 66)
98
user=> (example 100)
200
user=>
```

## TODO

Add more examples and more tests!

## License

Copyright © 2016-2018 [World Singles Networks llc](https://worldsinglesnetworks.com/).

Distributed under the Eclipse Public License version 1.0.
