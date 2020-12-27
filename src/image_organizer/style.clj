(ns image-organizer.style
  (:require [cljfx.css :as css]))

(def style
  (css/register
   ::style
   {".root" {:-fx-font-family "Arial"
             :-fx-font-size "14px"}
    ".border-pane" {:-fx-background-color "#0f1114"}
    ".tool-bar" {:-fx-background-color "#272c35"
                 :-fx-padding [0 0 0 0]}
    ".button" {:-fx-background-color "#272c35"
               :-fx-text-fill "#EBE4C9"
               :-fx-background-radius [0 0 0 0]
               :-fx-background-insets 0
               ":hover" {:-fx-background-color "#454D5C"}}
    ".dialog-pane" {:-fx-background-color "#0f1114"
                    " .button" {:-fx-background-radius [4 4 4 4]
                                :-fx-border-width 0}
                    " .header-panel" {:-fx-background-color "#272c35"}
                    " .details-button" {:-fx-text-fill "#EBE4C9"}}
    ".label" {:-fx-text-fill "#EBE4C9"}
    ".scroll-pane" {:-fx-background-color "#0f1114"
                    :-fx-background-radius [4 4 4 4]
                    :-fx-border-radius [4 4 4 4]
                    :-fx-border-color "#272c35"
                    :-fx-border-width 2
                    " .vbox" {:-fx-background-color "#0f1114"}}
    ".scroll-bar" {:-fx-background-color "#0f1114"
                   " .track" {:-fx-background-color "#0f1114"}
                   " .thumb" {:-fx-background-color "#272c35"
                              :-fx-background-radius [8 8 8 8]
                              ":hover" {:-fx-background-color "#454D5C"}
                              ":pressed" {:-fx-background-color "#454D5C"}}
                   " .decrement-button" {:visibility "hidden"
                                         :-fx-pref-height 0}
                   " .decrement-arrow" {:visibility "hidden"
                                        :-fx-pref-height 0}
                   " .increment-button" {:visibility "hidden"
                                         :-fx-pref-height 0}
                   " .increment-arrow" {:visibility "hidden"
                                        :-fx-pref-height 0}}
    ".text-field" {:-fx-background-color "#0f1114"
                   :-fx-background-radius [4 4 4 4]
                   :-fx-border-radius [4 4 4 4]
                   :-fx-border-color "#272c35"
                   :-fx-border-width 2
                   :-fx-prompt-text-fill "#64635a"
                   :-fx-text-fill "#EBE4C9"}
    ".text-area" {:-fx-text-fill "#EBE4C9"
                  :-fx-background-color "#0f1114"
                  " .content" {:-fx-background-color "#0f1114"
                               :-fx-background-radius 0}}}))







