;; copyright (c) 2017-2020 world singles networks llc

(ns ws.clojure.extensions-test
  "Tests for our 'Clojure language extension' functions."
  (:require [expectations.clojure.test
             :refer [defexpect expect from-each in more-of]]
            [ws.clojure.extensions
             :refer [condp-> condp->> interleave-all local-map]]))

(set! *warn-on-reflection* true)

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

(defn- local-map-with-args [a b]
  (let [c 1 d (+ a b)]
    {:all (local-map)
     :a-d (local-map :only :a :d)
     :b-c (local-map :without :a :d)}))

(defexpect local-map-tests

  ;; note: using `local-map` inside an expect will pick up an :e#
  ;; that is added by Expectations so we use expect/in for cases
  ;; that do not narrow keys to a specific subset:

  (expect {:a 1, :b 2}
          (in (let [a 1 b 2] (local-map))))

  (expect {:a 1}
          (let [a 1 b 2] (local-map :only :a)))

  (expect {:a 1, :b 2}
          (let [a 1 b 2] (local-map :only "a" b)))

  (expect {:b 2}
          (in (let [a 1 b 2] (local-map :without :a))))

  (expect {:all {:a 13, :b 42, :c 1, :d 55}
           :a-d {:a 13, :d 55}
           :b-c {:b 42, :c 1}}
          (local-map-with-args 13 42)))
