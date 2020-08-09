(ns image-organizer.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]]
            [fn-fx.fx-dom :as dom]
            [me.raynes.fs :as fs]))

(def folder-to-organize "/Users/seledrex/Desktop/to-sort")
(def output-folder "/Users/seledrex/Desktop/output")
(def supported-extensions ["png" "jpg" "jpeg"])
(def sorting-folders ["M" "F" "MM" "MF" "FF" "Group" "SFW" "Delete" "Vore" "Herm"])
(def total-buttons (+ 2 (count sorting-folders)))
(def application-width 1280)
(def application-height 720)
(def button-width (double (/ application-width total-buttons)))
(def button-height 40)
(def image-height (- application-height button-height))
(def event-types [:organize :skip :undo])

(defui ImageDisplay
  (render [this image-files]
          (ui/image-view
           :image (ui/image
                   :is (io/input-stream (first image-files)))
           :x 0
           :y 0
           :fit-width application-width
           :fit-height image-height
           :preserve-ratio true)))

(defui Stage
  (render [this {:keys [image-files undo-history]}]
          (let [finished? (empty? image-files)]
            (ui/stage
             :title "Image Organizer"
             :shown true
             :min-width application-width
             :min-height application-height
             :resizable false
             :scene (ui/scene
                     :root (ui/border-pane
                            :center (if (empty? image-files)
                                      (ui/label
                                       :text "No images left to organize!")
                                      (image-display image-files))
                            :bottom (ui/h-box
                                     :children (concat
                                                (map (fn [sf]
                                                       (ui/button
                                                        :text sf
                                                        :pref-width button-width
                                                        :pref-height button-height
                                                        :on-action {:event :organize
                                                                    :sf sf}
                                                        :disable finished?))
                                                     sorting-folders)
                                                [(ui/button
                                                  :text "Skip"
                                                  :pref-width button-width
                                                  :pref-height button-height
                                                  :on-action {:event :skip}
                                                  :disable finished?)
                                                 (ui/button
                                                  :text "Undo"
                                                  :pref-width button-width
                                                  :pref-height button-height
                                                  :on-action {:event :undo}
                                                  :disable (empty? undo-history))]))))))))

(defn load-image-files
  [path]
  (->> path
       (fs/list-dir)
       (filter (fn [image-file]
                 (some (fn [ext]
                         (string/ends-with? (.getName image-file)
                                            (str "." ext)))
                       supported-extensions)))))

(defn handle-organize
  [data-state sf]
  (let [image-files (get @data-state :image-files)]
    (when (not-empty image-files)
      (let [image-file (first image-files)
            image-name (.getName image-file)
            destination-folder (str output-folder "/" sf)
            destination-file (fs/file (str destination-folder "/" image-name))]
        (fs/move image-file destination-file)
        (swap! data-state
               (fn [state]
                 (-> state
                     (update :image-files rest)
                     (update :undo-history
                             (fn [history]
                               (concat [{:event :organize
                                         :name image-name
                                         :from folder-to-organize
                                         :to destination-folder}]
                                       history))))))))))

(defn handle-skip
  [data-state]
  (let [image-files (get @data-state :image-files)]
    (when (not-empty image-files)
      (let [image-file (first image-files)]
        (swap! data-state
               (fn [state]
                 (-> state
                     (update :image-files rest)
                     (update :undo-history
                             (fn [history]
                               (concat [{:event :skip
                                         :image-file image-file}]
                                       history))))))))))

(defn handle-undo
  [data-state]
  (when (not-empty (get @data-state :undo-history))
    (let [last-event (-> @data-state :undo-history first)
          event-type (:event last-event)]
      (case event-type
        :organize
        (let [source-folder (:to last-event)
              destination-folder (:from last-event)
              image-name (:name last-event)
              image-file (fs/file (str source-folder "/" image-name))
              destination-file (fs/file (str destination-folder "/" image-name))]
          (fs/move image-file destination-file)
          (swap! data-state
                 (fn [state]
                   (-> state
                       (update :image-files
                               (fn [image-files]
                                 (concat [destination-file]
                                         image-files)))
                       (update :undo-history rest)))))
        :skip
        (swap! data-state
               (fn [state]
                 (-> state
                     (update :image-files
                             (fn [image-files]
                               (concat [(:image-file last-event)]
                                       image-files)))
                     (update :undo-history rest))))))))

(defn create-sorting-folders
  []
  (doall
   (map (fn [subfolder]
          (fs/mkdir (str output-folder "/" subfolder)))
        sorting-folders)))

(defn -main
  [& args]
  (let [data-state (atom {:image-files (load-image-files folder-to-organize)
                          :undo-history []})
        handler-fn (fn [{:keys [event] :as all-data}]
                     (case event
                       :organize (handle-organize data-state (:sf all-data))
                       :skip (handle-skip data-state)
                       :undo (handle-undo data-state)))
        ui-state (agent (dom/app (stage @data-state) handler-fn))]
    (create-sorting-folders)
    (add-watch data-state :ui (fn [_ _ _ _]
                                (send ui-state
                                      (fn [old-ui]
                                        (dom/update-app old-ui (stage @data-state))))))))

(comment
  (-main)
  )
