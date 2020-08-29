(ns image-organizer.core
  (:require [cljfx.api :as fx]
            [image-organizer.events :as events]
            [image-organizer.views :as views]))

(def *state
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
    :exception nil}))

(def event-handler
  (-> events/event-handler
      (fx/wrap-co-effects
       {:state (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
       {:state (fx/make-reset-effect *state)})
      (fx/wrap-async)))

(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type views/root)
   :opts {:fx.opt/map-event-handler event-handler}))

(def app
  (do
    (event-handler {:event/type ::events/initialize})
    (fx/create-app *state
                   :event-handler event-handler
                   :desc-fn views/desc)))
