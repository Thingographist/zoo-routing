(ns zoo-routing.watcher
  (:require [zoo-routing.listener :refer [make-listener]])
  (:import [org.apache.curator.framework CuratorFramework CuratorFrameworkFactory]
           [org.apache.curator.framework.recipes.cache TreeCache]
           [org.apache.curator.retry BoundedExponentialBackoffRetry]))

(def defaults {:base-sleep-time-ms 1000
               :max-sleep-time-ms  30000
               :max-retries        5})

(defn ^CuratorFramework connect [connection-string ns retry-policy]
  (doto
    (.. (CuratorFrameworkFactory/builder)
        (namespace ns)
        (connectString connection-string)
        (retryPolicy retry-policy)
        (build))
    .start))

(defmulti create-watcher (fn [inst & _] (type inst)))

(defmethod create-watcher :default [inst & args]
  (throw (ex-info "use ZooKeeper connection or connection address" {:wrong-arg inst})))

(defmethod create-watcher String [zookeeper-address handler opt]
  (let [retry-policy (:retry-policy opt defaults)
        conn (connect zookeeper-address (:ns opt)
                      (BoundedExponentialBackoffRetry.
                        (:base-sleep-time-ms defaults)
                        (:max-sleep-time-ms defaults)
                        (:max-retries defaults)))
        tc (create-watcher conn handler {:root (:root opt)})]
    (fn []
      (list (tc) (.close conn)))))

(defmethod create-watcher CuratorFramework [conn handler opt]
  (let [tc (.build (TreeCache/newBuilder conn (:root opt)))]
    (. (.getListenable tc)
       (addListener (make-listener handler)))
    (.start tc)
    (fn [] (.close tc))))
