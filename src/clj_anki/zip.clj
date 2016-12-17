(ns clj-anki.zip
  "A helper library for working with ZIP files, which Anki stores
  everything with. Super-simple, little directory support. Completely
  imperative code ahoy."
  (:import [java.util.zip ZipInputStream ZipOutputStream ZipEntry]
           java.io.File)
  (require [clojure.java.io :as io]))

(def buffer-size
  "The amount to read at a time when streaming buffers."
  512)

(defn extract-file-from-zip!
  "Given a zipfile and the name of a file in it, this function will
  either extract the file to the specified outfile location or to the
  current folder as the same name as in the ZIP if nothing is
  specified."
  [zipfile file & [outfile]]
  (let [zis (ZipInputStream. (io/input-stream zipfile))]
    (loop [entry (.getNextEntry zis)]
      (if (not (= file (.getName entry)))
        (recur (.getNextEntry zis))
        (with-open [out (io/output-stream (or outfile file))]
          (io/copy zis out :buffer-size buffer-size))))))

(defn compress-files!
  "Given a list of files to compress, creates a flat ZIP file at
  outfile."
  [file-list outfile]
  (with-open [zip (ZipOutputStream. (io/output-stream outfile))]
    (doseq [file file-list]
      (.putNextEntry zip (ZipEntry. (.getName (io/file file))))
      (io/copy (io/file file) zip :buffer-size buffer-size)
      (.closeEntry zip))))
