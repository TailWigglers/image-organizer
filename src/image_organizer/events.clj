(ns image-organizer.events
  (:require [cljfx.api :as fx]
            [image-organizer.util :as util :refer [try-it]]
            [me.raynes.fs :as fs])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))

(defmacro with-exception-handling
  "Handles possible exceptions when updating state"
  [maybe-exception state & body]
  `(if (instance? Exception ~maybe-exception)
     {:state (-> ~state
                 (assoc :scene :alert)
                 (assoc :exception ~maybe-exception))}
     (do ~@body)))

(defn directory-chooser
  "Opens a directory chooser over the specified window"
  [window]
  (let [chooser (doto (DirectoryChooser.)
                  (.setTitle "Select Folder"))]
    @(fx/on-fx-thread (.showDialog chooser window))))

(defn get-window
  "Gets the window from an event's target"
  [event]
  (->> event
       (.getTarget)
       (cast Node)
       (.getScene)
       (.getWindow)))

(defmulti event-handler
  "Defines event handler methods for the application"
  :event/type)

(defmethod event-handler :default
  [event]
  (prn event))

(defmethod event-handler ::initialize
  [{:keys [state]}]
  (let [properties (util/read-properties)]
    (if (instance? Exception properties)
      {:state state}
      (let [{:keys [categories
                    input-folder
                    output-folder]} properties
            image-files (util/load-image-files input-folder)
            loaded-image (util/file->url (first image-files))]
        (with-exception-handling
          loaded-image state
          (util/create-subfolders output-folder categories)
          {:state (-> state
                      (assoc :categories categories)
                      (assoc :input-folder input-folder)
                      (assoc :output-folder output-folder)
                      (assoc :image-files image-files)
                      (assoc :loaded-image loaded-image))})))))

(defmethod event-handler ::scene-width
  [{scene-width :fx/event state :state}]
  {:state (assoc state :image-view-width scene-width)})

(defmethod event-handler ::scene-height
  [{scene-height :fx/event state :state}]
  (let [button-height (:button-height state)
        toolbar-height (:toolbar-height state)
        image-view-height (- scene-height button-height toolbar-height)]
    {:state (assoc state :image-view-height image-view-height)}))

(defmethod event-handler ::organize
  [{:keys [sf state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [output-folder (:output-folder state)
            image-file (first image-files)
            image-name (.getName image-file)
            destination-folder (str output-folder "/" sf)
            destination-file (fs/file (str destination-folder "/" image-name))
            maybe-exception (try-it (fs/move image-file destination-file))]
        (with-exception-handling
          maybe-exception state
          (let [input-folder (:input-folder state)
                next-image-file (second image-files)
                loaded-image (util/file->url next-image-file)]
            (with-exception-handling
              loaded-image state
              {:state
               (-> state
                   (update :image-files rest)
                   (assoc :loaded-image loaded-image)
                   (update :undo-history
                           #(conj % {:event-type :organize
                                     :name image-name
                                     :from input-folder
                                     :to destination-folder})))})))))))

(defmethod event-handler ::skip
  [{:keys [state]}]
  (let [image-files (:image-files state)]
    (if (empty? image-files)
      {:state state}
      (let [image-file (first image-files)
            next-image-file (second image-files)
            loaded-image (util/file->url next-image-file)]
        (with-exception-handling
          loaded-image state
          {:state
           (-> state
               (update :image-files rest)
               (assoc :loaded-image loaded-image)
               (update :undo-history
                       #(conj % {:event-type :skip
                                 :image-file image-file})))})))))

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
                destination-file (fs/file (str destination-folder "/" image-name))
                maybe-exception (fs/move image-file destination-file)]
            (with-exception-handling
              maybe-exception state
              (let [loaded-image (util/file->url destination-file)]
                (with-exception-handling
                  loaded-image state
                  {:state (-> state
                              (update :image-files #(conj % destination-file))
                              (assoc :loaded-image loaded-image)
                              (update :undo-history pop))}))))
          :skip
          (let [previous-image-file (:image-file last-event)
                loaded-image (util/file->url previous-image-file)]
            (with-exception-handling
              loaded-image state
              {:state (-> state
                          (update :image-files #(into [previous-image-file] %))
                          (assoc :loaded-image loaded-image)
                          (update :undo-history pop))})))))))

(defmethod event-handler ::select-folder
  [{:keys [state
           folder-key
           ^ActionEvent fx/event]}]
  (let [window (get-window event)]
    (when-let [folder-file (directory-chooser window)]
      (let [folder-path (.getAbsolutePath folder-file)]
        (case folder-key
          :input-folder
          (let [image-files (util/load-image-files folder-path)
                loaded-image (util/file->url (first image-files))]
            (with-exception-handling
              loaded-image state
              {:state (-> state
                          (assoc folder-key folder-path)
                          (assoc :image-files image-files)
                          (assoc :loaded-image loaded-image))}))
          :output-folder
          (let [categories (:categories state)]
            (when (not-empty categories)
              (util/create-subfolders folder-path categories))
            {:state (assoc state folder-key folder-path)}))))))

(defmethod event-handler ::open-select-categories
  [{:keys [state]}]
  {:state (assoc state :scene :select-categories)})

(defmethod event-handler ::close-select-categories
  [{:keys [state]}]
  (let [image-file (first (:image-files state))
        loaded-image (util/file->url image-file)]
    (with-exception-handling
      loaded-image state
      {:state (-> state
                  (assoc :scene :root)
                  (assoc :loaded-image loaded-image)
                  (assoc :typed-text ""))})))

(defmethod event-handler ::type-text
  [{:keys [state fx/event]}]
  {:state (assoc state :typed-text event)})

(defmethod event-handler ::add-category
  [{:keys [state]}]
  (let [typed-text (:typed-text state)
        output-folder (:output-folder state)
        categories (:categories state)
        new-categories (conj categories typed-text)]
    (when (some? output-folder)
      (util/create-subfolders output-folder new-categories))
    {:state (-> state
                (assoc :categories new-categories)
                (assoc :typed-text ""))}))

(defmethod event-handler ::remove-category
  [{:keys [state category]}]
  {:state (update state :categories #(vec (remove (partial = category) %)))})

(defn remove-index [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn append-at-index [v index value]
  (vec (concat (subvec v 0 index) [value] (subvec v index))))

(defn move-item [v index direction]
  (let [item (get v index)
        new-index (if (= direction :up) (dec index) (inc index))]
    (-> v
        (remove-index index)
        (append-at-index new-index item))))

(defmethod event-handler ::move-category
  [{:keys [state index direction]}]
  {:state (update state :categories #(move-item % index direction))})


(defmethod event-handler ::open-about
  [{:keys [state]}]
  (let [logo-image (util/logo-image-url)]
    (with-exception-handling
      logo-image state
      {:state (-> state
                  (assoc :scene :about)
                  (assoc :logo-image logo-image))})))

(defmethod event-handler ::close-about
  [{:keys [state]}]
  (let [image-file (first (:image-files state))
        loaded-image (util/file->url image-file)]
    (with-exception-handling
      loaded-image state
      {:state (-> state
                  (assoc :scene :root)
                  (assoc :loaded-image loaded-image))})))

(defmethod event-handler ::stop
  [{:keys [state]}]
  (util/write-properties (select-keys state [:categories :input-folder :output-folder]))
  (if (:is-repl? state)
    {:state state}
    (do
      (shutdown-agents)
      (javafx.application.Platform/exit))))
