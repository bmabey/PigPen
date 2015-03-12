;;
;;
;;  Copyright 2013 Netflix, Inc.
;;
;;     Licensed under the Apache License, Version 2.0 (the "License");
;;     you may not use this file except in compliance with the License.
;;     You may obtain a copy of the License at
;;
;;         http://www.apache.org/licenses/LICENSE-2.0
;;
;;     Unless required by applicable law or agreed to in writing, software
;;     distributed under the License is distributed on an "AS IS" BASIS,
;;     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;     See the License for the specific language governing permissions and
;;     limitations under the License.
;;
;;

(ns pigpen.rx
  (:require [pigpen.rx.core :as rx]
            [pigpen.rx.extensions :refer [multicast->observable]]
            [rx.lang.clojure.blocking :as rx-blocking]
            [pigpen.oven :as oven]))

(defn dump
  "Executes a script locally and returns the resulting values as a clojure
sequence. This command is very useful for unit tests.

  Example:

    (->>
      (pig/load-clj \"input.clj\")
      (pig/map inc)
      (pig/filter even?)
      (pig-rx/dump)
      (clojure.core/map #(* % %))
      (clojure.core/filter even?))

    (deftest test-script
      (is (= (->>
               (pig/load-clj \"input.clj\")
               (pig/map inc)
               (pig/filter even?)
               (pig-rx/dump))
             [2 4 6])))

  Note: pig/store commands return an empty set
        pig/store-many commands merge their results
"
  {:added "0.1.0"}
  [query]
  (let [graph (oven/bake :rx {} {} query)
        last-command (:id (last graph))]
    (->> graph
      (reduce rx/graph->observable+ {})
      (last-command)
      (multicast->observable)
      (rx-blocking/into [])
      (map (comp val first)))))
