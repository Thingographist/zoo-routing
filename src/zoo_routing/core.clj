(ns zoo-routing.core
  (:require [clojure.string :as string]
            [zoo-routing.watcher :refer [create-watcher]]
            [zoo-routing.routes :as routes])
  (:import [clojure.lang IFn]))

(defn start-watcher
"
  conn-or-zookeeper-address - String zookeeper host or inst of CuratorFramework

  opt:
  * ns - zookeeper namespace
  * root - root watched node
  * retry-policy - zookeeper RetryPolicy

  defaults RetryPolicy
  {:base-sleep-time-ms 1000
   :max-sleep-time-ms  30000
   :max-retries        5}
"
  [conn-or-zookeeper-address ^IFn handler
   & [{:as opt}]]
  (create-watcher
    conn-or-zookeeper-address handler
    (merge {:root "/" :ns ""} opt)))

