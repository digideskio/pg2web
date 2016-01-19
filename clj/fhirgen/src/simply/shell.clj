(ns simply.shell
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell :as cjs]
    [clojure.string :as cs]))

;; to-shell
(declare to-shell)

(defn opts  [x]
  (cs/join " " (map (fn [[k v]] (str (to-shell k) " " (to-shell v) " ")) x)))

(def ops {:and " && " :or " && "})

(defn cmd  [x]
  (cond
    (contains? ops  (first x))  (cs/join  (get ops  (first x))  (map to-shell  (rest x)))
    :else  (cs/join " "  (map to-shell x))))

(defn to-shell  [x]
  (cond
    (string? x) x
    (keyword? x)  (name x)
    (map? x)  (opts x)
    (vector? x)  (cmd x)))

(defn shell [x]
  (let [cmd (to-shell x)]
    (println "SH:" cmd)
    (cjs/sh "bash" "-c" (to-shell x))))

(comment
  (shell
    [:and
     [:ls :-lah]
     [:df :-h]]))
