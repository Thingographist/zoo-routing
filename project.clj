(defproject zoo-routing "0.1.0"
  :description "compojure for zookeeper"
  :url "https://github.com/Thingographist/zoo-routing"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]

                 ;; ROUTING
                 [clout "2.1.2" :exclusions [instaparse org.clojure/clojure]]
                 [instaparse "1.4.8"]

                 ;; CURATOR
                 [org.apache.curator/curator-framework "4.0.0"]
                 [org.apache.curator/curator-recipes "4.0.0"]
                 [org.apache.curator/curator-test "4.0.0"]]


  :profiles {:dev         {:dependencies [[org.apache.curator/curator-client "4.0.0"]]} ;; ZOOKEEPER 3.5
             :repl        {:dependencies [[org.apache.zookeeper/zookeeper "3.4.11"] ;; ZOOKEEPER 3.4
                                          [org.apache.curator/curator-client "4.0.0" :exclusions [org.apache.zookeeper/zookeeper]]]}
             :zookeeper-4 {:dependencies [[org.apache.zookeeper/zookeeper "3.4.11"] ;; ZOOKEEPER 3.4
                                          [org.apache.curator/curator-client "4.0.0" :exclusions [org.apache.zookeeper/zookeeper]]]}
             :zookeeper-5 {:dependencies [[org.apache.curator/curator-client "4.0.0"]]}}

  :repl-options {:host "0.0.0.0"
                 :port 4001})
