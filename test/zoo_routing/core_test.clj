(ns zoo-routing.core-test
  (:require [clojure.test :refer :all]
            [zoo-routing.core :refer :all]
            [zoo-routing.watcher :refer [defaults connect]]
            [clojure.core.async :refer [alts!! <!! <! go chan put! timeout close!]]
            [zoo-routing.routes :refer [routes ADDED REMOVED UPDATED]])
  (:import [org.apache.curator.test TestingServer]
           [org.apache.curator.retry BoundedExponentialBackoffRetry]))

(deftest routing-handler
  (testing "Watch new nodes"
    (let [server (TestingServer. 2111)
          conn (connect "localhost:2111" ""
                        (BoundedExponentialBackoffRetry.
                          (:base-sleep-time-ms defaults)
                          (:max-sleep-time-ms defaults)
                          (:max-retries defaults)))
          ch (chan)
          handler (routes (ADDED "/test/:node" [evt] (put! ch evt)))
          stop-watcher (start-watcher conn handler)
          node-path "/test/test-node"]
      (.. conn (create) (forPath "/test"))
      (try
        (.. conn (create) (forPath node-path))
        (let [[v c] (alts!! [ch (timeout 3000)])
              got (merge v {:stat {} :body nil})]
          (is (= {:type  :NODE_ADDED
                  :stat {}
                  :path node-path
                  :route-params {:node "test-node"}
                  :body  nil}
                 got)))
        (finally
          (try
            (.. conn (delete) (forPath node-path))
            (catch Exception _ nil))
          (close! ch)
          (stop-watcher)
          (.close conn)
          (.stop server)
          (.close server))))))
