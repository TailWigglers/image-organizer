(ns image-organizer.views
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [image-organizer.events :as events]))

(def sorting-folders ["M" "F" "MM" "MF" "FF" "Group" "SFW" "Delete" "Vore" "Herm"])
(def total-buttons (+ 2 (count sorting-folders)))
(def application-width 1280)
(def application-height 720)
(def button-width (double (/ application-width total-buttons)))
(def button-height 40)
(def image-height (- application-height button-height))

(defn image-display [image-files]
  {:fx/type :image-view
   :image {:fx/type :image
           :is (io/input-stream (first image-files))}
   :x 0
   :y 0
   :fit-width application-width
   :fit-height image-height
   :preserve-ratio true})

(defn root [{:keys [image-files undo-history]}]
  (let [finished? (empty? image-files)]
    {:fx/type :stage
     :showing true
     :title "Image Organizer"
     :min-width application-width
     :min-height application-height
     :resizable false
     :scene {:fx/type :scene
             :root {:fx/type :border-pane
                    :center (if finished?
                              {:fx/type :label
                               :text "No images left to organize!"}
                              (image-display image-files))
                    :bottom {:fx/type :h-box
                             :children (concat
                                        (map (fn [sf]
                                               {:fx/type :button
                                                :text sf
                                                :pref-width button-width
                                                :pref-height button-height
                                                :disable finished?
                                                :on-action {:event/type ::events/organize
                                                            :sf sf}})
                                             sorting-folders)
                                        [{:fx/type :button
                                          :text "Skip"
                                          :pref-width button-width
                                          :pref-height button-height
                                          :disable finished?
                                          :on-action {:event/type ::events/skip}}
                                         {:fx/type :button
                                          :text "Undo"
                                          :pref-width button-width
                                          :pref-height button-height
                                          :disable (empty? undo-history)
                                          :on-action {:event/type ::events/undo}}])}}}}))
