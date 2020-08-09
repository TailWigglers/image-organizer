(ns image-organizer.main
  (:require [image-organizer.core :as core])
  (:gen-class :extends javafx.application.Application))

(defn -start
  [app stage]
  (core/start {:root-stage? false}))

(defn -main
  [& args]
  (javafx.application.Application/launch image_organizer.main
                                         (into-array String args)))

