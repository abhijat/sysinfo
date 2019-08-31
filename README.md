# sysinfo

This is a clojure library to examine running processes and other system information on the Linux operating system.

It exposes information from the /proc filesystem to the programming environment.

This is a work in progress, right now only the process interface is available.

Some of the commands that can be executed from the repl are:

1. See all the running processes:
`(processes)`

2. See the commands associated with running processes:
`(map :cmdline (processes)`

3. Find all java processes
`(filter #(re-find #"java" (:cmdline %)) (processes)`

All of the processes can also show their environment variables. This is suppressed by default for both security and
readability. It can be enabled by passing a truthy param to `processes`.

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
