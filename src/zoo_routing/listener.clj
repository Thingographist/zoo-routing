(ns zoo-routing.listener
  (:import [org.apache.zookeeper.data Stat]
           [org.apache.curator.framework.recipes.cache TreeCacheEvent TreeCacheListener]
           [clojure.lang IFn]))

(defn stat->map
  [^Stat stat]
  (when stat
    {:czxid (.getCzxid stat)
     :mzxid (.getMzxid stat)
     :ctime (.getCtime stat)
     :mtime (.getMtime stat)
     :version (.getVersion stat)
     :cversion (.getCversion stat)
     :aversion (.getAversion stat)
     :ephemeralOwner (.getEphemeralOwner stat)
     :dataLength (.getDataLength stat)
     :numChildren (.getNumChildren stat)
     :pzxid (.getPzxid stat)}))

(defn TreeCacheEvent->map [^TreeCacheEvent event]
  (if-let [data (.getData event)]
    {:type (keyword (str (.getType event)))
     :path (.getPath data)
     :stat (stat->map (.getStat data))
     :body (.getData data)}
    {:type (keyword (str (.getType event)))}))

(defn make-listener [^IFn handler]
  (reify TreeCacheListener
    (childEvent [this conn evt]
      (let [evt (TreeCacheEvent->map evt)]
        (when (some? (:path evt))
          (handler evt))))))
