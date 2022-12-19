![Clojure CI](https://github.com/sanmoskalenko/hsapp/workflows/Clojure%20CI/badge.svg)

# hsapp

Demo application for working with patients on the stack `Clojure/ClojureScript + PostgreSQL`. The application implements the following patient logic:
* browsing;
* search;
* filtering;
* creation;
* deletion;

For the user interface, the application uses the [Ant.Design](https://ant.design/) component library.

With the default configuration, the application is accessible on `localhost:3000` after startup.

## Usage

#### Make sure you have `Docker` installed and running before using.

> Attention! If you already have `clojure/lein/node/kubectl/kind`installed, the packages will be updated

To install dependencies (make sure you have homebrew installed before starting):

    make deps 

Install project-only dependencies (node_modules & lein deps): 

    make prj-deps

To build the application in a jar:

    make jar

To start the application in docker:

    make up

To stop the application in docker:

    make down

To run the application on a local kubernetes cluster:

    make kube-up 

To stop the local kubernetes cluster: 

    make kube-down

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
