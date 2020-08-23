(ns image-organizer.core
  (:require [cljfx.api :as fx]
            [image-organizer.events :as events]
            [image-organizer.views :as views]
            [image-organizer.util :as util]))

(def *state
  (atom
   {:image-files []
    :undo-history []}))

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
  (swap! *state assoc :image-files (util/load-image-files events/folder-to-organize))
  (util/create-subfolders events/output-folder views/sorting-folders)
  (fx/mount-renderer *state renderer))

(run)

