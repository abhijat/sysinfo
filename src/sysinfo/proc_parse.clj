(ns sysinfo.proc-parse
  (:require [clojure.string :as s]
            [clojure.java.io :as io]))

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

;; TODO is there a better way to create keywords from these?
(def functions [pid uid cmdline user-info mem-info])
(def function-names [:pid :uid :cmdline :user-info :mem-info])

(defn process-info
  ([path] (process-info path false))
  ([path show-env]
   (let [functions (if show-env (conj functions environ) functions)
         function-names (if show-env (conj function-names :env) function-names)]
     (try
       (zipmap function-names (map #(% path) functions))
       (catch Exception e nil)))))

(defn path-for-pid
  [pid]
  (let [path-obj (io/file "/proc" (str pid))]
    (if (.exists path-obj) (.getPath path-obj) nil)))

(defn show-process
  ([pid] (show-process pid false))
  ([pid show-env]
   (when-let [path (path-for-pid pid)]
     (process-info path))))

(defn processes
  ([show-env]
   (let [process-list (filter #(re-matches #"/proc/[0-9]+" %)
                              (map #(.getPath %)
                                   (.listFiles (io/file "/proc"))))
         process-data (for [pr process-list
                            :when (not-empty (cmdline pr))]
                        (process-info pr show-env))]
     (remove nil? process-data)))
  ([] (processes false)))
