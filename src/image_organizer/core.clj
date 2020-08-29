(ns image-organizer.core
  (:gen-class)
  (:require [cljfx.api :as fx]
            [image-organizer.events :as events]
            [image-organizer.views :as views]))

(def *state
  "State of the application and its default values"
  (atom
   {:categories []
    :input-folder ""
    :output-folder ""
    :image-files []
    :undo-history []
    :button-height 40
    :image-view-width 1280
    :image-view-height 660
    :loaded-image nil
    :error? false
    :exception nil
    :is-repl? true}))

(def event-handler
  "Application event handler"
  (-> events/event-handler
      (fx/wrap-co-effects
       {:state (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
       {:state (fx/make-reset-effect *state)})
      (fx/wrap-async)))

(def app
  "Initializes the data state and creates the application"
  (do
    (event-handler {:event/type ::events/initialize
                    :fx/sync true})
    (fx/create-app *state
                   :event-handler event-handler
                   :desc-fn views/desc)))

(defn -main
  "Main entry point. Turns of repl mode so when the application
   is closed, JavaFX is shutdown and shutdown-agents is called"
  [& args]
  (swap! *state assoc :is-repl? false))
