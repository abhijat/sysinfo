(ns sysinfo.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.pprint :as pprint])
  (:gen-class))

(defn wrap-slurp [process-path leaf]
  (slurp (java.io.FileReader. (str process-path "/" leaf))))

(defn replace-nulls [e]
  (s/trim (s/replace e (char 0) \ )))

(defn cmdline [process-path]
  (replace-nulls (slurp (io/file process-path "cmdline"))))

(defn environ [process-path]
  "Extracts a process's environment into a map, by splitting /proc/<pid>/environ on ="
  (let [null-pattern (re-pattern (str (char 0)))
        env (wrap-slurp process-path "environ")
        env (s/split env null-pattern)
        env (map #(s/split % #"=" 2) env)]
    (into {} (for [[k v] env]
               [(keyword k) v]))))

(defn mem-info [process-path]
  (let [readings (map #(Long/parseLong %)
                      (s/split (s/trim-newline (wrap-slurp process-path
                                                           "statm"))
                               #" "))
        [size resident shared text _ data _] readings]
    {:size size
     :resident resident
     :shared shared
     :text text
     :data data}))

(defn pid [process-path]
  (Long/parseLong (last (s/split process-path #"/"))))

(defn uid [process-path]
  (Long/parseLong (wrap-slurp process-path "loginuid")))

(defn user-info [process-path]
  (let [user-id (str (uid process-path))
        rows (s/split-lines (slurp "/etc/passwd"))
        users (map #(s/split % #":") rows)
        user-row (first (filter #(= user-id (nth % 2))
                                users))
        [name _ _ _ _ home shell] user-row]
    {:name name
     :home home
     :shell shell}))

(defn processes
  ([show-env]
   (let [process-list (filter #(re-matches #"/proc/[0-9]+" %)
                              (map #(.getPath %)
                                   (.listFiles (io/file "/proc"))))
         funcs [pid uid cmdline user-info mem-info]
         funcs (if show-env (conj funcs environ) funcs)
         ks [:pid :uid :cmdline :user-info :mem-info]
         ks (if show-env (conj ks :env) ks)
         process-data (for [pr process-list :when (not-empty (cmdline pr))]
                        (try
                          (zipmap ks (map #(% pr) funcs))
                          (catch Exception e nil)))]
     (remove nil? process-data)))
  ([] (processes false)))

(defn -main
  [& args]
  (dorun (map pprint/pprint (remove nil? (processes)))))
