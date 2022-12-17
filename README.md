# hsapp

Demo app in Clojure.

## Usage
To build the application in a jar, run the following command in the terminal:

    make jar

To start the application in docker, you need to run the following command:

    make up

To stop the application in docker you need to run the following command:

    make down

To run the application on a local kubernetes cluster, run the following command:

    make kube-up 

To stop the local kubernetes cluster, run the command: 

    make kube-down

To install dependencies, run the command:

    make deps 

## Description

With the default configuration, the application, after launch, is available on `localhost:3000`

## License

Copyright © 2022 sanmoskalenko

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html._
