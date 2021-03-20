(ns gcal-clj.specs-macros
  (:require [clojure.spec.alpha :as s]))


(defn def-map-props-fn [m]
  (doseq [[k v] m]
    (s/def k v)))


(defmacro def-map-props [m]
  (let [forms
        (for [entry m]
          (let [k (key entry)
                v (val entry)]
            `(s/def ~k ~v)))]
    `(do ~@forms)))

(comment
  (macroexpand-1
    '(def-map-props {::f int? ::b string?}))

  (def-map-props {::thing? int?})
  (s/get-spec ::thing?))


(defmacro mapdef-opt [spec-name m]
  (let [opt-un-keys (keys m)]
    `(do (def-map-props ~m)
         (s/def ~spec-name (s/keys :opt-un [~@opt-un-keys])))))


(defmacro mapdef1 [spec-name m]
  (let [req-un-keys (keys m)]
    `(do (def-map-props ~m)
         (s/def ~spec-name (s/keys :req-un [~@req-un-keys])))))


(comment
  (macroexpand-1
    '(mapdef1 ::map1 {::a int?}))

  (mapdef1 ::map1 {::a int?})

  (assert (s/valid? ::a 3))
  (assert (s/valid? ::map1 {:a 3})))


(defmacro mapdef2 [spec-name req-map opt-map]
  (let [req-keys (keys req-map)
        opt-keys (keys opt-map)]
    `(do
       (def-map-props ~req-map)
       (def-map-props ~opt-map)
       (s/def ~spec-name
         (s/keys :req-un [~@req-keys]
                 :opt-un [~@opt-keys])))))

(comment
  (macroexpand-1
    '(mapdef2 ::map2
              {:map2/key1 int?}
              {:map2/key2 string?})))

