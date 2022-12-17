(defproject hsapp "0.1.0-SNAPSHOT"
  :description "Demo application"
  :url "https://github.com/sanmoskalenko"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]

                 [aero "1.1.6"]

                 [healthsamurai/matcho "0.3.9"]

                 [compojure "1.6.3"]
                 [clj-unifier "0.0.15"]
                 [selmer "1.12.55"]
                 [clojure.java-time "1.1.0"]
                 [clj-time "0.15.2"]

                 [ring/ring-defaults "0.3.4"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [ring "1.9.6"]
                 [metosin/ring-http-response "0.9.3"]
                 [ring-cors "0.1.13"]
                 [metosin/muuntaja "0.6.8"]
                 [ring/ring-json "0.5.1" :exclusions [cheshire]]
                 [cheshire "5.11.0"]

                 [mount "0.1.16"]
                 [io.rkn/conformity "0.5.4"]
                 [cprop "0.1.19"]
                 [seancorfield/next.jdbc "1.2.659"]
                 [org.postgresql/postgresql "42.5.1"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [honeysql "1.0.461"]
                 [migratus "1.4.6"]
                 [hikari-cp "3.0.1"]
                 [com.stuartsierra/component "1.1.0"]

                 [cambium/cambium.core "1.1.1"]
                 [cambium/cambium.codec-simple "1.0.0"]
                 [cambium/cambium.logback.core "0.4.5"]

                 [reagent "1.1.1"]
                 [re-frame "1.3.0"]
                 [syn-antd "4.17.2"]
                 [day8.re-frame/tracing "0.6.2"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [binaryage/devtools "1.0.6"]
                 [day8.re-frame/re-frame-10x "1.5.0"]
                 [cljs-ajax "0.8.4"]

                 [thheller/shadow-cljs "2.20.10"]]

  :main server.health.core

  :resource-paths ["resources" "target/resources"]

  :target-path "target/%s"

  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.4.0"]
                                      [hashp "0.2.2"]]
                       :injections   [(require 'hashp.core)]}

             :uberjar {:aot          [server.health.core]
                       :jvm-opts     ["-Dclojure.compiler.direct-linking=true"]
                       :prep-tasks   ["compile" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
                       :uberjar-name "hsapp.jar"}}

  :repl-options {:init-ns server.health.core})
