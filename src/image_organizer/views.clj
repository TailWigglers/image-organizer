(ns image-organizer.views
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [image-organizer.events :as events]
            [image-organizer.util :as util]))

(defn image-view
  "Creates description of the image view"
  [image-files width height loaded-image]
  {:fx/type :image-view
   :image {:fx/type :image
           :is loaded-image}
   :x 0
   :y 0
   :fit-width width
   :fit-height height
   :preserve-ratio true})

(defn root
  "Creates description of the application"
  [{:keys [categories
           image-files
           undo-history
           button-height
           image-view-width
           image-view-height
           loaded-image
           toolbar-height
           input-folder
           output-folder]}]
  (let [finished? (empty? image-files)
        input-folder? (nil? input-folder)
        output-folder? (nil? output-folder)
        buttons-disabled? (or finished?
                              input-folder?
                              output-folder?)]
    {:fx/type :stage
     :showing true
     :title "Image Organizer"
     :min-width 1280
     :min-height 720
     :on-close-request {:event/type ::events/stop}
     :scene
     {:fx/type :scene
      :on-width-changed {:event/type ::events/scene-width}
      :on-height-changed {:event/type ::events/scene-height}
      :root
      {:fx/type :v-box
       :children
       [{:fx/type :tool-bar
         :pref-height toolbar-height
         :items
         [{:fx/type :button
           :text "Select Input Folder"
           :pref-width 150
           :on-action {:event/type ::events/select-folder
                       :folder-key :input-folder}}
          {:fx/type :button
           :text "Select Ouput Folder"
           :pref-width 150
           :on-action {:event/type ::events/select-folder
                       :folder-key :output-folder}}
          {:fx/type :button
           :pref-width 150
           :text "Select Categories"}]}
        {:fx/type :border-pane
         :v-box/vgrow :always
         :center
         (if input-folder?
           {:fx/type :label
            :text "Input folder not selected!"}
           (if output-folder?
             {:fx/type :label
              :text "Output folder not selected!"}
             (if finished?
               {:fx/type :label
                :text "No images left to organize!"}
               (image-view image-files
                           image-view-width
                           image-view-height
                           loaded-image))))
         :bottom
         {:fx/type :h-box
          :children
          (concat
           (map (fn [sf]
                  {:fx/type :button
                   :text sf
                   :h-box/hgrow :always
                   :max-width java.lang.Double/MAX_VALUE
                   :pref-height button-height
                   :disable buttons-disabled?
                   :on-action {:event/type ::events/organize
                               :sf sf}})
                categories)
           [{:fx/type :button
             :text "Skip"
             :h-box/hgrow :always
             :max-width java.lang.Double/MAX_VALUE
             :pref-height button-height
             :disable buttons-disabled?
             :on-action {:event/type ::events/skip}}
            {:fx/type :button
             :text "Undo"
             :h-box/hgrow :always
             :max-width java.lang.Double/MAX_VALUE
             :pref-height button-height
             :disable (empty? undo-history)
             :on-action {:event/type ::events/undo}}])}}]}}}))

(defn alert
  "Creates description for an exception dialog"
  [{:keys [exception]}]
  (let [error-message (.getMessage exception)
        stack-trace (util/exception->stack-trace-string exception)]
    {:fx/type :dialog
     :showing true
     :title "Error"
     :resizable true
     :on-close-request {:event/type ::events/stop}
     :dialog-pane
     {:fx/type :dialog-pane
      :header-text "An error has occured."
      :content-text error-message
      :button-types [:ok]
      :style-class ["alert" "error" "dialog-pane"]
      :expandable-content
      {:fx/type :grid-pane
       :max-width java.lang.Double/MAX_VALUE
       :children
       [{:fx/type :label
         :text "The exception stacktrace was:"
         :grid-pane/column 0
         :grid-pane/row 0}
        {:fx/type :text-area
         :text stack-trace
         :editable false
         :wrap-text true
         :max-width java.lang.Double/MAX_VALUE
         :max-height java.lang.Double/MAX_VALUE
         :grid-pane/vgrow :always
         :grid-pane/hgrow :always
         :grid-pane/column 0
         :grid-pane/row 1}]}}}))

(defn desc
  "Chooses which description to create"
  [{:keys [error?] :as state}]
  (if error?
    (alert state)
    (root state)))
