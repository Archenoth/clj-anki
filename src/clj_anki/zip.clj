(ns clj-anki.zip
  "A helper library for working with ZIP files, which Anki stores
  everything with. Super-simple, little directory support. Completely
  imperative code ahoy."
  (:import [java.util.zip ZipInputStream ZipOutputStream ZipEntry]
           java.io.File)
  (require [clojure.java.io :as io]))

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
          (io/copy zis out))))))

(defn compress-files!
  "Given a list of maps of {:file :name} to compress, creates a ZIP
  file at outfile. For each file, :name is the name in the ZIP file
  and :file is the file object."
  [file-list outfile]
  (with-open [zip (ZipOutputStream. (io/output-stream outfile))]
    (doseq [file file-list]
      (.putNextEntry zip (ZipEntry. (:name file)))
      (io/copy (:file file) zip)
      (.closeEntry zip))))
