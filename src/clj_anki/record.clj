(ns clj-anki.record
  "An unsightly helper library for creating database records."
  (:import java.util.Date)
  (:require [clj-anki.hash :as hash]
            [clojure.string :as str]))

(defn convert-map-to-db
  "Given a record with the keys :tags (optional), :answers,
  and :question, will return a map that can be inserted into the Anki
  database."
  [map]
  (-> (assoc map
             :id (rand-int Integer/MAX_VALUE)
             :guid (str/join (map char (int-array (repeatedly 9 #(rand-int 1024)))))
             :mid 1342697561419
             :mod (.getTime (java.util.Date.))
             :usn -1
             :tags (str/join " " (:tags map))
             :flds (str/join "" (concat [(:question map)] (:answers map)))
             :sfld (:question map)
             :csum (hash/anki-checksum (:question map))
             :flags 0
             :data "")
      (dissoc :question :answers)))

(defn card-record-from-note-id
  "Given a note ID, will create a card record"
  [note-id]
  {:id (rand-int Integer/MAX_VALUE)
   :nid note-id
   :did 1398130078204
   :ord 0
   :mod (.getTime (Date.))
   :usn -1
   :type 0
   :queue 0
   :due 0
   :ivl 0
   :factor 0
   :reps 0
   :lapses 0
   :left 0
   :odue 0
   :odid 0
   :flags 0
   :data ""})
