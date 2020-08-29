(ns image-organizer.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]))

(s/def
  ::categories
  (s/coll-of string?))

(s/def
  ::input-folder
  string?)

(s/def
  ::output-folder
  string?)

(s/def
  ::properties
  (s/keys :req-un [::categories
                   ::input-folder
                   ::output-folder]))

(def supported-extensions ["png" "jpg" "jpeg"])

(defn has-extension?
  "Checks if a filename has a given extension"
  [filename ext]
  (string/ends-with? filename (str "." ext)))

(defn has-supported-extension?
  "Checks if a filename has a given extension from a coll of extensions"
  [filename extensions]
  (some (fn [ext] (has-extension? filename ext))
        extensions))

(defn load-image-files
  "Loads image files from a specified path"
  [path]
  (->> path
       (fs/list-dir)
       (filter (fn [image-file]
                 (has-supported-extension? (.getName image-file)
                                           supported-extensions)))))

(defn create-subfolders
  "Creates subfolders in a specified folder"
  [folder subfolders]
  (doall
   (map (fn [subfolder]
          (fs/mkdir (str folder "/" subfolder)))
        subfolders)))

(defn read-properties
  "Reads the properties file. This will either return a map containing
   the data of the properties file or an exception if there was an error"
  []
  (let [properties (try-it (read-string (slurp "properties.edn")))]
    (if (instance? Exception properties)
      properties
      (if (s/valid? ::properties properties)
        properties
        (Exception. "Could not parse properties file")))))

(defmacro try-it
  "Tries to evaluate a function and returns the result or an exception"
  [fun]
  (let [e (gensym 'e)]
    `(try
       ~fun
       (catch Exception ~e ~e))))

(defn exception->stack-trace-string
  "Converts an exception into a stack trace string"
  [e]
  (with-out-str (clojure.stacktrace/print-stack-trace e)))

(defn load-file
  "Loads an image from a file"
  [file]
  (if (nil? file)
    nil
    (try-it (io/input-stream file))))