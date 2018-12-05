;; copyright (c) 2017-2018 world singles networks llc

(ns ws.clojure.extensions-test
  "Tests for our 'Clojure language extension' functions."
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [ws.clojure.extensions :refer [interleave-all]]))

(defexpect interleave-all-tests

  (expect [1 :a \A 2 :b \B :c \C :d]
          (interleave-all [1 2] [:a :b :c :d] [\A \B\C]))

  (expect [1 2 3] (interleave-all [1 2 3] []))

  (expect [] (interleave-all))

  (expect [1 2 3] (interleave-all [1 2 3])))
