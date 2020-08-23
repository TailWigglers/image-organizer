(ns image-organizer.core
  (:require [cljfx.api :as fx]
            [image-organizer.events :as events]
            [image-organizer.views :as views]
            [image-organizer.util :as util]
            [clojure.java.io :as io]))

(def *state
  (atom
   {:categories ["M" "F" "MM" "MF" "FF" "Group" "SFW" "Delete" "Vore" "Herm"]
    :image-files []
    :undo-history []
    :button-height 40
    :image-view-width 1280
    :image-view-height 660
    :loaded-image nil}))

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

(defn run []
  (event-handler {:event/type ::events/initialize})
  (fx/mount-renderer *state renderer))

(run)



