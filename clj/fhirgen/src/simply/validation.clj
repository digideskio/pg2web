(ns simply.validation
  (:require [clojure.string :as cs]))

(defn not-blank? [x]
  (and
    (not (nil? x))
    (and (string? x) (not (cs/blank? x)))))

(defn get-vals [m ks]
  (map
    (fn [k] (get m k))
    ks))

(defn add-error [es k e]
  (if (get es k)
    (update-in es [k] conj e)
    (assoc es k [e])))

(defn validate-key [er data attr [pred msg]]
  (if (vector? attr)
    (if (apply pred (get-vals data attr))
      er
      (add-error er attr msg))
    (if (pred (get data attr))
      er
      (add-error er attr msg))))

(defn validate [validators data]
  (let [errors (reduce
                 (fn [er [attr attr-validators]]
                   (reduce
                     (fn [er validator] (validate-key er data attr validator))
                     er attr-validators))
                 {} validators)]
    (if (empty? errors) nil errors)))

(comment
  (def myvalidations
    {:a [[not-blank? "a should be filled"]]
     [:password :password_conf] [[= "password should "]]})

  (validate myvalidations
            {:b 1 :password "ups"})

  (validate myvalidations
            {:a "ups" :password "ups" :password_conf "ups"}))
