(ns sysinfo.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [clojure.data.json :as json]
            [sysinfo.proc-parse :as proc])
  (:gen-class))

(defn json-response
  [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body body})

(defn to-json-pretty
  [content]
  (with-out-str (json/pprint content)))

(def json-ok-response (partial json-response 200))

(defn list-processes
  [request]
  (json-ok-response (to-json-pretty (proc/processes))))

(defn cpu-info
  [request]
  (json-ok-response (to-json-pretty (proc/cpu-info))))

(defn show-process
  [request]
  (let [pid (get-in request [:path-params :pid])]
    (println "serving request for" pid)
    (json-ok-response (to-json-pretty (proc/show-process pid)))))

(def routes
  (route/expand-routes
   #{["/ps" :get list-processes :route-name :ps]
     ["/ps/:pid" :get show-process :route-name :pid]
     ["/cpu" :get cpu-info :route-name :cpu]}))

;; TODO accept port from command line
(defn create-server
  [args]
  (let [port (Integer/parseInt (or (first args) "8890"))]
    (http/create-server {::http/routes routes
                         ::http/type :jetty
                         ::http/port port})))

(defn -main
  [& args]
  (http/start (create-server args)))
