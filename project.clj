(defproject clj-anki "0.0.3"
  :description "A Clojure library for creating and interacting with Anki files."
  :url "https://github.com/Archenoth/clj-anki"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["res"]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.xerial/sqlite-jdbc "3.7.2"]])
