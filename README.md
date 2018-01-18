# zoo-routing

A Clojure library for compojure like listeners.

## Usage

```clojure
(require '[zoo-routing.routes :refer [routes]])

(def raw-routes
    (routes
      (ADDED "/path/:node{.*}" [{:keys [route-params] :as req}] (prn req))
      (UPDATED "/path/:node{.*}" [{:keys [route-params] :as req}] (prn req))
      (REMOVED "/path/:node{.*}" [{:keys [route-params] :as req}] (prn req))))
```

And given request 

```clojure
{:type :NODE_ADDED,
 :path "/path/quota",
 :route-params {:node "quota"}
 :stat {:dataLength 0,
        :numChildren 0,
        :pzxid 0,
        :aversion 0,
        :ephemeralOwner 0,
        :cversion 0,
        :ctime 0,
        :czxid 0,
        :mzxid 0,
        :version 0,
        :mtime 0},
 :body byte-array
}
```

## License

Copyright © 2018 Thingographist

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
