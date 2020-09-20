(ns image-organizer.views
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [image-organizer.events :as events]
            [image-organizer.util :as util])
  (:import [javafx.geometry Insets]))

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
  [{:keys [app-name
           categories
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
        categories? (empty? categories)
        buttons-disabled? (or finished?
                              input-folder?
                              output-folder?
                              categories?)]
    {:fx/type :stage
     :showing true
     :title app-name
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
           :text "Select Categories"
           :on-action {:event/type ::events/open-select-categories}}
          {:fx/type :button
           :pref-width 150
           :text "About"
           :on-action {:event/type ::events/open-about}}]}
        {:fx/type :border-pane
         :v-box/vgrow :always
         :center
         (if input-folder?
           {:fx/type :label
            :text "Input folder not selected!"}
           (if output-folder?
             {:fx/type :label
              :text "Output folder not selected!"}
             (if categories?
               {:fx/type :label
                :text "No categories created!"}
               (if finished?
                 {:fx/type :label
                  :text "No images left to organize!"}
                 (image-view image-files
                             image-view-width
                             image-view-height
                             loaded-image)))))
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

(defn category-text-filter
  "Filters out invalid filename characters and limits the input length"
  [change]
  (let [new-text (.getControlNewText change)]
    (when (or (> (count new-text) 25)
              (some util/invalid-symbol? new-text))
      (.setText change ""))
    change))

(defn select-categories
  "Creates a dialog for selecting categories"
  [{:keys [categories
           typed-text]}]
  (let [valid-category? (util/valid-category? typed-text
                                              categories)]
    {:fx/type :dialog
     :showing true
     :title "Select Categories"
     :resizable false
     :on-close-request {:event/type ::events/close-select-categories}
     :dialog-pane
     {:fx/type :dialog-pane
      :button-types [:ok]
      :content
      {:fx/type :v-box
       :pref-width 300
       :pref-height 400
       :children
       [{:fx/type :scroll-pane
         :v-box/vgrow :always
         :fit-to-width true
         :content
         {:fx/type :v-box
          :children
          (map
           (fn [category]
             {:fx/type :h-box
              :spacing 5
              :padding 5
              :alignment :center-left
              :children
              [{:fx/type :button
                :text "Remove"
                :on-action {:event/type ::events/remove-category
                            :category category}}
               {:fx/type :label
                :text category}]})
           categories)}}
        {:fx/type :h-box
         :children
         [{:fx/type :text-field
           :text typed-text
           :h-box/margin (Insets. 5 5 0 0)
           :h-box/hgrow :always
           :text-formatter {:fx/type :text-formatter
                            :filter category-text-filter}
           :prompt-text "Category"
           :on-text-changed {:event/type ::events/type-text}}
          {:fx/type :button
           :text "Add"
           :h-box/margin (Insets. 5 0 5 0)
           :disable (not valid-category?)
           :on-action {:event/type ::events/add-category}}]}]}}}))

(defn about
  "Creates description for about dialog"
  [{:keys [logo-image version app-name]}]
  {:fx/type :dialog
   :showing true
   :title "About"
   :on-close-request {:event/type ::events/close-about}
   :dialog-pane
   {:fx/type :dialog-pane
    :button-types [:ok]
    :content
    {:fx/type :h-box
     :padding 20
     :children
     [{:fx/type :image-view
       :image {:fx/type :image
               :is logo-image}
       :x 0
       :y 0
       :fit-width 200
       :fit-height 200
       :preserve-ratio true}
      {:fx/type :v-box
       :alignment :center-left
       :padding 20
       :children
       [{:fx/type :label
         :text app-name}
        {:fx/type :label
         :text (str "Version " version)}
        {:fx/type :label
         :text "© Tail Wigglers"}]}]}}})

(defn desc
  "Chooses which description to create"
  [{:keys [scene] :as state}]
  (case scene
    :root (root state)
    :select-categories (select-categories state)
    :alert (alert state)
    :about (about state)))
