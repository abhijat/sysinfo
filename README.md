# sysinfo

This is a clojure library to examine running processes and other system information on the Linux operating system.

It exposes information from the /proc filesystem to the programming environment.

The following fields are displayed per process at this time:

1. process id
2. user id
3. user info (username, home directory, login shell)
4. Memory stats (size, resident, shared, text, data)
5. Command line for the process

This is a work in progress, right now only the process and CPU interface is available.

Some of the commands that can be executed from the repl are:

1. See all the running processes:


`(proc/processes)`

2. See the commands associated with running processes:


`(map :cmdline (proc/processes)`

3. Find all java processes


`(filter #(re-find #"java" (:cmdline %)) (proc/processes)`

4. See all running processes with their environment variables


`(proc/processes true)`

5. See information about the CPU


`(proc/cpu-info)`

The processes can also display their environment variables. This is suppressed by default for both security and
readability. 

## Web server

Executing the uberjar or `lein run` incantation on the command line starts a web server which shows running processes at `localhost:8890/ps`
and individual processes as `localhost:8890/ps/<pid>` in JSON format. 

The port can be changed by passing another port at the command line as the first argument.

The process data shown on the web service always skips environment data.
