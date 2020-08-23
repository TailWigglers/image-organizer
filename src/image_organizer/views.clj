(ns image-organizer.views
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [image-organizer.events :as events]))

(defn image-view
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
  [{:keys [categories
           image-files
           undo-history
           button-height
           image-view-width
           image-view-height
           loaded-image]}]
  (let [finished? (empty? image-files)]
    {:fx/type :stage
     :showing true
     :title "Image Organizer"
     :min-width 1280
     :min-height 720
     :scene {:fx/type :scene
             :on-width-changed {:event/type ::events/scene-width}
             :on-height-changed {:event/type ::events/scene-height}
             :root {:fx/type :border-pane
                    :center (if finished?
                              {:fx/type :label
                               :text "No images left to organize!"}
                              (image-view image-files
                                          image-view-width
                                          image-view-height
                                          loaded-image))
                    :bottom {:fx/type :h-box
                             :children (concat
                                        (map (fn [sf]
                                               {:fx/type :button
                                                :text sf
                                                :h-box/hgrow :always
                                                :max-width java.lang.Double/MAX_VALUE
                                                :pref-height button-height
                                                :disable finished?
                                                :on-action {:event/type ::events/organize
                                                            :sf sf}})
                                             categories)
                                        [{:fx/type :button
                                          :text "Skip"
                                          :h-box/hgrow :always
                                          :max-width java.lang.Double/MAX_VALUE
                                          :pref-height button-height
                                          :disable finished?
                                          :on-action {:event/type ::events/skip}}
                                         {:fx/type :button
                                          :text "Undo"
                                          :h-box/hgrow :always
                                          :max-width java.lang.Double/MAX_VALUE
                                          :pref-height button-height
                                          :disable (empty? undo-history)
                                          :on-action {:event/type ::events/undo}}])}}}}))
