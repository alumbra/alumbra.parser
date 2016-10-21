(ns alumbra.generators.value
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators.common :refer :all]
            [clojure.string :as string]))

(def -integer
  gen/int)

(def -digits
  (as-> "0123456789" <>
    (gen/elements <>)
    (gen/vector <> 1 8)
    (gen/fmap #(apply str %) <>)))

(def -exponent
  (gen/let [digits -digits
            sign   (maybe (gen/elements ["+" "-"]))
            e      (gen/elements ["e" "E"])]
    (gen/return (str e sign digits))))

(def -float
  (->> (gen/one-of
         [(gen/fmap #(str "." %) -digits)
          -exponent
          (gen/let [e -exponent
                    d -digits]
            (gen/return (str "." d e)))])
       (gen/tuple -integer)
       (gen/fmap #(apply str %))))

(def -string
  (->> (gen/not-empty gen/string-ascii)
       (gen/fmap #(string/replace
                    %
                    #"[\"\\]"
                    (fn [[match]]
                      (str "\\" match))))
       (gen/fmap #(str \" % \"))))

(def -bool
  (gen/elements ["true" "false"]))

(def -enum
  (->> (gen/such-that #(not (#{"true" "false" "null"} %)) -name)
       (gen/fmap string/upper-case)))

(defn -list
  [g]
  (gen/let [vs (gen/vector g 0 5)]
    (gen/return
      (format "[%s]" (string/join ", " vs)))))

(defn -object
  [g]
  (let [field-gen (gen/let [n -name, v g]
                    (gen/return (str n ": " v)))]
    (gen/let [fields (gen/vector field-gen 0 3)]
      (gen/return
        (format "{%s}" (string/join ", " fields))))))

(let [wrap #(gen/recursive-gen
              (fn [g]
                (gen/frequency
                  [[10 (-list g)]
                   [10 (-object g)]
                   [80 g]]))
              %)]
  (def -value
    (wrap
      (gen/one-of
        [-variable
         -integer
         -float
         -string
         -bool
         -enum])))

  (def -const
    (wrap
      (gen/one-of
        [-integer
         -float
         -string
         -bool
         -enum]))))
