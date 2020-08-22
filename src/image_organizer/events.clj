(ns image-organizer.events
  (:require [me.raynes.fs :as fs]
            [image-organizer.core :refer [output-folder folder-to-organize]]))

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::organize
  [{:keys [sf state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [image-file (first image-files)
            image-name (.getName image-file)
            destination-folder (str output-folder "/" sf)
            destination-file (fs/file (str destination-folder "/" image-name))]
        (fs/move image-file destination-file)
        {:state (-> state
                    (update :image-files rest)
                    (update :undo-history #(conj % {:event-type :organize
                                                    :name image-name
                                                    :from folder-to-organize
                                                    :to destination-folder})))}))))

(defmethod event-handler ::skip
  [{:keys [state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [image-file (first image-files)]
        {:state (-> state
                    (update :image-files rest)
                    (update :undo-history #(conj % {:event-type :skip
                                                    :image-file image-file})))}))))

(defmethod event-handler ::undo
  [{:keys [state]}]
  (let [undo-history (:undo-history state)]
    (if (empty? undo-history)
      {:state state}
      (let [last-event (peek undo-history)
            event-type (:event-type last-event)]
        (case event-type
          :organize
          (let [source-folder (:to last-event)
                destination-folder (:from last-event)
                image-name (:name last-event)
                image-file (fs/file (str source-folder "/" image-name))
                destination-file (fs/file (str destination-folder "/" image-name))]
            (fs/move image-file destination-file)
            {:state (-> state
                        (update :image-files #(conj % destination-file))
                        (update :undo-history pop))})
          :skip
          {:state (-> state
                      (update :image-files #(into [(:image-file last-event)] %))
                      (update :undo-history pop))})))))
