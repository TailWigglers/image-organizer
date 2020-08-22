(ns image-organizer.util
  (:require [me.raynes.fs :as fs]
            [clojure.string :as string]))

(def supported-extensions ["png" "jpg" "jpeg"])

(defn load-image-files
  [path]
  (->> path
       (fs/list-dir)
       (filter (fn [image-file]
                 (some (fn [ext]
                         (string/ends-with? (.getName image-file)
                                            (str "." ext)))
                       supported-extensions)))))

(defn create-subfolders
  [folder subfolders]
  (doall
   (map (fn [subfolder]
          (fs/mkdir (str folder "/" subfolder)))
        subfolders)))
