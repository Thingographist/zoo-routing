(ns zoo-routing.routes
  (:require [clojure.set :as set]
            [zoo-routing.clout :as zclout]
            [clout.core :as clout]))

(defn- literal? [x]
  (if (coll? x)
    (every? literal? x)
    (not (or (symbol? x) (list? x)))))

(defn- method-matches? [request method]
  (or (nil? method)
      (= (:type request) method)))

(defn- assoc-route-params [request params]
  (merge-with merge request {:route-params params}))

(defn route-request [request route]
  (when-let [params (clout/route-matches route request)]
    (assoc-route-params request params)))

(defn prepare-route [route]
  (cond
    (string? route)
    (zclout/route-compile route)
    (and (vector? route) (literal? route))
    (zclout/route-compile
      (first route)
      (apply hash-map (rest route)))
    (vector? route)
    `(zclout/route-compile
       ~(first route)
       ~(apply hash-map (rest route)))
    :else
    `(if (string? ~route)
       (zclout/route-compile ~route)
       ~route)))

(defn make-route [method path handler]
  (let [router (prepare-route path)]
    (fn [req]
      (when (method-matches? req method)
        (when-let [req (route-request req router)]
          (handler req)
          true)))))

(defn routes [& routes]
  (fn [req] (some #(% req) routes)))

(defmacro ADDED [path args & fn-body]
  `(make-route :NODE_ADDED ~path (fn ~args ~@fn-body)))

(defmacro UPDATED [path args & fn-body]
  `(make-route :NODE_UPDATED ~path (fn ~args ~@fn-body)))

(defmacro REMOVED [path args & fn-body]
  `(make-route :NODE_REMOVED ~path (fn ~args ~@fn-body)))
