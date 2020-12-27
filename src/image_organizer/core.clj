(ns image-organizer.core
  (:gen-class)
  (:require [cljfx.api :as fx]
            [image-organizer.events :as events]
            [image-organizer.views :as views]))

(def *state
  "State of the application and its default values"
  (atom
   {:categories []
    :input-folder nil
    :output-folder nil
    :image-files []
    :undo-history []
    :button-height 40
    :toolbar-height 40
    :image-view-width 1280
    :image-view-height (- 720 40 40 20)
    :loaded-image nil
    :typed-text ""
    :scene :root
    :exception nil
    :is-repl? true
    :logo-image nil
    :version "1.00"
    :app-name "Image Organizer"}))

(def event-handler
  "Application event handler"
  (-> events/event-handler
      (fx/wrap-co-effects
       {:state (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
       {:state (fx/make-reset-effect *state)})))

(def app
  "Initializes the data state and creates the application"
  (do
    (event-handler {:event/type ::events/initialize})
    (fx/create-app *state
                   :event-handler event-handler
                   :desc-fn views/desc)))

(defn -main
  "Main entry point. Turns of repl mode so when the application
   is closed, JavaFX is shutdown and shutdown-agents is called"
  []
  (swap! *state assoc :is-repl? false))
