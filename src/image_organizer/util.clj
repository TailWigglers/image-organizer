(ns image-organizer.util
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.stacktrace :as cs]
            [clojure.string :as string]
            [me.raynes.fs :as fs]))

(def supported-extensions ["png" "jpg" "jpeg" "bmp" "gif"])
(def app-dir (str (System/getProperty "user.home") "/.image-organizer"))
(def properties-file "properties.edn")

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

(defmacro try-it
  "Tries to evaluate a function and returns the result or an exception"
  [fun]
  (let [e (gensym 'e)]
    `(try
       ~fun
       (catch Exception ~e ~e))))

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
  (let [properties (try-it (read-string (slurp (str app-dir "/" properties-file))))]
    (if (instance? Exception properties)
      properties
      (if (s/valid? ::properties properties)
        properties
        (Exception. "Could not parse properties file")))))

(defn write-properties
  "Writes the properties file."
  [properties]
  (let [properties-path (str app-dir "/" properties-file)]
    (fs/mkdir app-dir)
    (try-it (spit properties-path (pr-str properties)))))

(defn exception->stack-trace-string
  "Converts an exception into a stack trace string"
  [e]
  (with-out-str (cs/print-stack-trace e)))

(defn valid-category?
  "Makes sure the category is valid"
  [typed-text categories]
  (and (not-any? #(= (string/lower-case typed-text)
                     (string/lower-case %))
                 categories)
       (not (= "" typed-text))))

(def invalid-symbols
  [":" "\\" "/" "." "," "[" "]" "{" "}" "(" ")" "!" ";" "\"" "'" "*" "?" "<" ">" "|"])

(defn invalid-symbol?
  "Checks if a character is an invalid symbol"
  [character]
  (let [symbol (str character)]
    (some #(= % symbol) invalid-symbols)))

(defn logo-image-url
  "Load the URL of the logo image file"
  []
  (str (io/resource "image-organizer.png")))

(defn file->url
  "Gets the URL of a file"
  [file]
  (when (some? file)
    (str (.toURL (.toURI file)))))
