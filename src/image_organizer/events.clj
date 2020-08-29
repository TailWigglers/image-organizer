(ns image-organizer.events
  (:require [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [image-organizer.util :as util]))

(defmulti event-handler :event/type)

(defmethod event-handler :default [event]
  (prn event))

(defmethod event-handler ::initialize
  [{:keys [state]}]
  (let [properties (util/read-properties)]
    (if (instance? Exception properties)
      {:state (-> state
                  (assoc :error? true)
                  (assoc :exception properties))}
      (let [{:keys [categories
                    input-folder
                    output-folder]} properties
            image-files (util/load-image-files input-folder)
            loaded-image (if (empty? image-files)
                           nil
                           (io/input-stream (first image-files)))]
        (util/create-subfolders output-folder categories)
        {:state (-> state
                    (assoc :categories categories)
                    (assoc :input-folder input-folder)
                    (assoc :output-folder output-folder)
                    (assoc :image-files image-files)
                    (assoc :loaded-image loaded-image))}))))

(defmethod event-handler ::scene-width
  [{scene-width :fx/event state :state}]
  {:state (assoc state :image-view-width scene-width)})

(defmethod event-handler ::scene-height
  [{scene-height :fx/event state :state}]
  (let [button-height (:button-height state)
        image-view-height (- scene-height button-height)]
    {:state (assoc state :image-view-height image-view-height)}))

(defmethod event-handler ::organize
  [{:keys [sf state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [input-folder (:input-folder state)
            output-folder (:output-folder state)
            image-file (first image-files)
            next-image-file (second image-files)
            loaded-image (if (nil? next-image-file)
                           nil
                           (io/input-stream next-image-file))
            image-name (.getName image-file)
            destination-folder (str output-folder "/" sf)
            destination-file (fs/file (str destination-folder "/" image-name))]
        (fs/move image-file destination-file)
        {:state (-> state
                    (update :image-files rest)
                    (assoc :loaded-image loaded-image)
                    (update :undo-history #(conj % {:event-type :organize
                                                    :name image-name
                                                    :from input-folder
                                                    :to destination-folder})))}))))

(defmethod event-handler ::skip
  [{:keys [state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [image-file (first image-files)
            next-image-file (second image-files)
            loaded-image (if (nil? next-image-file)
                           nil
                           (io/input-stream next-image-file))]
        {:state (-> state
                    (update :image-files rest)
                    (assoc :loaded-image loaded-image)
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
                        (assoc :loaded-image (io/input-stream destination-file))
                        (update :undo-history pop))})
          :skip
          (let [previous-image-file (:image-file last-event)]
            {:state (-> state
                        (update :image-files #(into [previous-image-file] %))
                        (assoc :loaded-image (io/input-stream previous-image-file))
                        (update :undo-history pop))}))))))
