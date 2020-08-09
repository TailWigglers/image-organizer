(defproject image-organizer "0.1.0-SNAPSHOT"
  :description "An application for sorting images into categories"
  :url "https://github.com/TailWigglers/image-organizer"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clj-commons/fs "1.5.2"]
                 [fn-fx/fn-fx-openjfx11 "0.5.0-SNAPSHOT"]
                 [org.clojure/clojure "1.10.1"]]
  :main image-organizer.main
  :aot [image-organizer.main]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
