;; copyright (c) 2017-2018 world singles networks llc

(ns ws.clojure.extensions-test
  "Tests for our 'Clojure language extension' functions."
  (:require [expectations.clojure.test
             :refer [defexpect expect from-each more-of]]
            [ws.clojure.extensions
             :refer [condp-> condp->> interleave-all]]))

(defexpect cond-tests

  (expect (more-of [x y]
                   true (= x y))
          (from-each [[n res] [[22 66] [66 98]]]
                     [(condp-> n
                               even?   (* 3)
                               (> 100) (- 100))
                      res]))

  (expect (more-of [x y]
                   true (= x y))
          (from-each [[n res] [[22 34] [66 198]]]
                     [(condp->> n
                                even?   (* 3)
                                (> 100) (- 100))
                      res])))

(defexpect interleave-all-tests

  (expect [1 :a \A 2 :b \B :c \C :d]
          (interleave-all [1 2] [:a :b :c :d] [\A \B\C]))

  (expect [1 2 3] (interleave-all [1 2 3] []))

  (expect [] (interleave-all))

  (expect [1 2 3] (interleave-all [1 2 3])))
