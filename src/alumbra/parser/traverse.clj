(ns alumbra.parser.traverse)

;; ## Metadata

(defn- attach-position
  [value form]
  {:pre [(map? value)]}
  (if (contains? value :alumbra/metadata)
    value
    (if-let [{:keys [antlr/row antlr/column antlr/index]}
             (some-> form meta :antlr/start)]
      (assoc value
             :alumbra/metadata
             {:row    row
              :column column
              :index  index})
      value)))

;; ## Traversal

(defn- traverse*
  [visitors state form]
  (if (string? form)
    state
    (let [k (first form)]
      (if-let [visitor (get visitors k)]
        (-> (visitor #(traverse* visitors %1 %2) state form)
            (attach-position form))
        (throw
          (IllegalStateException.
            (format "no visitor defined for node type '%s' in: %s"
                    k
                    (pr-str form))))))))

(defn traverser
  "Generate a function that traverses the ANTLR4 parser output using the given
   visitors. Each visitor is a function taking the walker function and the
   current form.

   The result of each visitor should be a map."
  [visitors]
  (let [visitors (->> (for [[ks visitor] visitors
                            k (if (sequential? ks) ks [ks])]
                        [k visitor])
                      (into {}))]
    #(traverse* visitors {} %)))

;; ## Helpers

(defn traverse-body
  [& [preprocess]]
  (if preprocess
    (fn [traverse-fn state [_ & body]]
      (reduce traverse-fn state (map preprocess body)))
    (fn [traverse-fn state [_ & body]]
      (reduce traverse-fn state body))))

(defn collect-as
  [k & [initial-state]]
  (let [initial-state (or initial-state {})]
    (fn [traverse-fn state [_ & body]]
      (let [result (reduce traverse-fn initial-state body)]
        (update state k (fnil conj []) result)))))

(defn body-as
  [k & [initial-state]]
  (let [initial-state (or initial-state {})]
    (fn [traverse-fn state [_ & body]]
      (let [result (mapv #(traverse-fn initial-state %)
                         (remove string? body))]
        (assoc state k result)))))

(defn block-as
  [k & [initial-state]]
  (fn [traverse-fn state [_ _ & body-and-delimiter]]
    (let [body (butlast body-and-delimiter)
          result (mapv #(traverse-fn initial-state %)
                       (remove string? body))]
      (assoc state k result))))

(defn as
  [k & [preprocess]]
  (if preprocess
    (fn [_ state form]
      (assoc state k (preprocess form)))
    (fn [_ state form]
      (assoc state k form))))

(defn unwrap
  []
  (fn [traverse-fn state [_ inner]]
    (traverse-fn state inner)))

(defn unwrap-as
  [k & [initial-state]]
  (let [initial-state (or initial-state {})]
    (fn [traverse-fn state [_ inner]]
      (assoc state k (traverse-fn initial-state inner)))))

(defn unwrap-last-as
  [k & [initial-state]]
  (let [initial-state (or initial-state {})]
    (fn [traverse-fn state [_ & body]]
      (assoc state k (traverse-fn initial-state (last body))))))
