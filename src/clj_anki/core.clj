(ns clj-anki.core
  "The core library for interacting with Anki files"
  (:import java.io.File)
  (require [clj-anki.zip :as zip]
           [clj-anki.record :as rec]
           [clojure.java.jdbc :as sql]
           [clojure.string :as str]
           [clojure.java.io :as io]))

(def database-spec
  "The base map to use for database connections."
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"})

(defn read-notes-from-collection
  "Reads the :answers, :question, and :tags entries from the notes of
  an Anki collection."
  [anki-collection]
  (map #(assoc % :tags (->> (str/split (:tags %) #" ") (filter (complement str/blank?)) set)
               :answers (rest (str/split (:answers %) #"\x1f")))
       (-> (assoc database-spec :subname anki-collection)
           (sql/query "SELECT sfld AS question, flds AS answers, tags FROM notes"))))

(defn read-notes
  "Reads notes from an Anki 2 Package and returns them as a list of
  maps with the :question being the question field, the :answer being
  a list of answer fields, and the :tags field being a set of tags."
  [anki-package]
  (let [tempfile (File/createTempFile "clj-anki" ".sqlite")]
    (zip/extract-file-from-zip! anki-package "collection.anki2" tempfile)
    (let [data (read-notes-from-collection tempfile)]
      (io/delete-file tempfile)
      data)))

(defn map-seq-to-collection!
  "Given a sequence of maps with :answers, :tags, and :question
  entries, this function will create a new Anki collection."
  [inmap outfile]
  (io/copy (io/input-stream (io/resource "blank.sqlite")) (io/file outfile))
  (sql/with-db-connection [db (assoc database-spec :subname outfile)]
    (let [values (map rec/convert-map-to-db inmap)]
      (sql/insert-multi! db "notes" values)
      (->> (map #(rec/card-record-from-note-id (:id %)) values)
           (sql/insert-multi! db "cards"))))
  outfile)

(defn map-seq-to-package!
  "Given a sequence of maps with :answers, :tags, and :question
  entries, this function will create a new Anki package that can be
  imported into Anki."
  [inmap outfile]
  (io/copy (io/input-stream (io/resource "media")) (io/file "media"))
  (let [files [(map-seq-to-collection! inmap "collection.anki2") "media"]]
    (zip/compress-files! files outfile)
    (io/delete-file "media")
    (io/delete-file "collection.anki2")))
