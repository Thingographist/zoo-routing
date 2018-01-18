(ns zoo-routing.clout
  (:require [instaparse.core :as insta]
            [clout.core]
            [clojure.set :as set]
            [clojure.string :as string])
  (:import (java.util.regex Matcher)))

(def ^:private re-chars (set "\\.*+|?()[]{}$^"))

(defn- re-escape [s]
  (string/escape s #(if (re-chars %) (str \\ %))))

(defn- re-groups* [^Matcher matcher]
  (for [i (range (.groupCount matcher))]
    (.group matcher (int (inc i)))))

(defn- assoc-conj [m k v]
  (assoc m k
           (if-let [cur (get m k)]
             (if (vector? cur)
               (conj cur v)
               [cur v])
             v)))

(defn- assoc-keys-with-groups [groups keys]
  (reduce (fn [m [k v]] (assoc-conj m k v))
          {}
          (map vector keys groups)))

(defrecord CompiledRoute [source re keys]
  clout.core/Route
  (route-matches [_ request]
    (let [path-info (:path request)
          matcher   (re-matcher re path-info)]
      (if (.matches matcher)
        (assoc-keys-with-groups (re-groups* matcher) keys))))
  Object
  (toString [_] source))

(defn- parse [parser text]
  (let [result (insta/parse parser text)]
    (if (insta/failure? result)
      (throw (ex-info "Parse error in route string" {:failure result}))
      result)))

(defn- find-route-key [form]
  (case (first form)
    :wildcard :*
    :param    (-> form second second keyword)))

(defn- route-keys [parse-tree]
  (->> (rest parse-tree)
       (filter (comp #{:param :wildcard} first))
       (map find-route-key)))

(defn- trim-pattern [pattern]
  (some-> pattern (subs 1 (dec (count pattern)))))

(defn- param-regex [regexs key & [pattern]]
  (str "(" (or (trim-pattern pattern) (regexs key) "[^/,;?]+") ")"))

(defn- route-regex [parse-tree regexs]
  (insta/transform
    {:route    (comp re-pattern str)
     :scheme   #(if (= % "//") "https?://" %)
     :literal  re-escape
     :escaped  #(re-escape (subs % 1))
     :wildcard (constantly "(.*?)")
     :param    (partial param-regex regexs)
     :key      keyword
     :pattern  str}
    parse-tree))

(def ^:private route-parser
  (insta/parser
    "route    = (scheme / part) part*
     scheme   = #'(https?:)?//'
     <part>   = literal | escaped | wildcard | param
     literal  = #'(:[^\\p{L}_*{}\\\\]|[^:*{}\\\\])+'
     escaped  = #'\\\\.'
     wildcard = '*'
     param    = key pattern?
     key      = <':'> #'([\\p{L}_][\\p{L}_0-9-]*)'
     pattern  = '{' (#'(?:[^{}\\\\]|\\\\.)+' | pattern)* '}'"
    :no-slurp true))

(defn route-compile
  "Compile a route string for more efficient route matching."
  ([path]
   (route-compile path {}))
  ([path regexs]
   (route-compile path regexs ->CompiledRoute))
  ([path regexs compiled-builder]
   (let [ast (parse route-parser path)
         ks  (route-keys ast)]
     (assert (set/subset? (set (keys regexs)) (set ks))
             "unused keys in regular expression map")
     (compiled-builder
       path
       (route-regex ast regexs)
       (vec ks)))))
