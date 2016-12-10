(ns alumbra.parser.utils
  (:require [clojure.edn :as edn]))

;; ## Names

(defn read-name
  [[_ n]]
  (if (string? n)
    n
    (recur n)))

(defn read-prefixed-name
  [[_ _ n]]
  (read-name n))

;; ## Value Literals

(defn parse-value
  [value-type f]
  (let [value-key (keyword "alumbra" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (let [v' (f v)]
        (-> state
            (assoc :alumbra/value-type value-type)
            (cond-> (some? v') (assoc value-key v')))))))

(defn read-string-literal [s] (edn/read-string s))
(defn read-nested-integer [[_ [_ v]]] (Long. ^String v))

;; ## Types

(defn traverse-named-type
  []
  (fn [traverse-fn state [_ [_ n]]]
    (assoc state
           :alumbra/type-class   :named-type
           :alumbra/type-name    (read-name n)
           :alumbra/non-null?    false)))

(defn traverse-list-type
  []
  (fn [traverse-fn state [_ _ element-type]]
    (assoc state
           :alumbra/type-class   :list-type
           :alumbra/non-null?    false
           :alumbra/element-type (traverse-fn {} element-type))))

(defn traverse-non-null-type
  []
  (fn [traverse-fn state [_ inner-type]]
    (-> (traverse-fn state inner-type)
        (assoc :alumbra/non-null? true))))
