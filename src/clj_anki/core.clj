(ns clj-anki.core
  "The core library for interacting with Anki files. The two main
  functions of interest are 'read-notes' and 'map-seq-to-package!'"
  (:import java.io.File)
  (:require [clj-anki.zip :as zip]
            [clj-anki.record :as rec]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

;; Constants
(def database-spec
  "The a map to use as the base for database connections.

  Set up according to the data source information here:
  http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html"
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"})

;; Clojure Specs
;; Note field specifications
(s/def ::non-empty-string (s/and string? not-empty))
(s/def ::question ::non-empty-string)
(s/def ::answer ::non-empty-string)
(s/def ::tag ::non-empty-string)
(s/def ::answers (s/coll-of ::answer :into [] :min-count 1))
(s/def ::tags (s/coll-of ::tag :into #{}))

;; Allowed note argument specs
;; {:question "What's up?" :answers ["Not a lot" "You?"] :tags #{"me_irl"}}
(s/def ::keyed-note
  (s/keys :req-un [::question ::answers]
          :opt-un [::tags]))

;; {:question "What's up?" :answer "Not a lot" :tags #{"me_irl"}}
(s/def ::keyed-single-note
  (s/keys :req-un [::question ::answer]
          :opt-un [::tags]))

;; ["What's up?" "Not a lot" "You?"]
(s/def ::listed-note
  (s/and (s/cat :question ::question
                :answers (s/+ ::answer))))

;; "What's up?" "Not a lot"
(s/def ::note-pair
  (s/cat :question ::question
         :answer ::answer))

;; Splitting the note types into multi-answer and single-answer notes
(s/def ::notes
  (s/+ (s/alt :multi ::keyed-note
              :multi ::listed-note
              :single ::keyed-single-note
              :single ::note-pair)))

;; Note multimethods
(defmulti normalize-note
  "Normalizes a conformed note-type into a map that can be used by
  `map-seq-to-collection!`"
  first)

(defmethod normalize-note :single single-note-normalize [[_ note]]
  (assoc (dissoc note :answer) :answers [(:answer note)]))

(defmethod normalize-note :multi multi-note-normalize [[_ note]]
  note)

(defn normalize-notes
  "Normalizes a set of notes from the user in any form into
  keyed-notes to be used by other functions."
  [& notes]
  (map normalize-note (s/conform ::notes notes)))

(s/fdef normalize-notes
  :args ::notes
  :ret (s/coll-of ::keyed-note))

;; Functions!
(defn read-notes-from-collection
  "Given the path to a .anki2 file, reads :answers, :question,
  and :tags entries from its notes and returns them as a list of maps.

If you had a deck with two cards like:

| Question | Answers | Tags |
|----------+---------+------|
| 2 + 2    | 4, Four | math |
| 3 + 3    | 6, Six  | math |

You would get back a collection of maps like:

({:question \"2 + 2\", :answers [\"4\" \"Four\"], :tags #{\"math\"}}
 {:question \"3 + 3\", :answers [\"6\" \"Six\"], :tags #{\"math\"}})"
  [anki-collection]
  (map #(assoc % :tags (->> (str/split (:tags %) #" ") (filter (complement str/blank?)) set)
               :answers (rest (str/split (:answers %) #"\x1f")))
       (-> (assoc database-spec :subname anki-collection)
           (sql/query "SELECT sfld AS question, flds AS answers, tags FROM notes"))))

(defn read-notes
  "Given the path to a .apkg file, reads :answers, :question,
  and :tags fields and returns them in the same way
  read-notes-from-collection does, so given a deck with the cards:

| Question                     | Answers                 | Tags         |
|------------------------------+-------------------------+--------------|
| Guy who wrote this sentence  | Archenoth               | people       |
| Person reading this sentence | (Uh, what's your name?) | people, cool |

You would get back a collection of maps like:

({:question \"Guy who wrote this sentence\",
  :answers (\"Archenoth\"),
  :tags #{\"people\"}}

 {:question \"Person reading this sentence\",
  :answers (\"(Uh, what's your name?)\"),
  :tags #{\"cool\" \"people\"}})"
  [anki-package]
  (let [tempfile (File/createTempFile "clj-anki" ".sqlite")]
    (zip/extract-file-from-zip! anki-package "collection.anki2" tempfile)
    (let [data (read-notes-from-collection tempfile)]
      (io/delete-file tempfile)
      data)))

(defn map-seq-to-collection!
  "Given a sequence of maps with :answers, :tags, and :question
  entries, this function will create a new Anki collection at the path
  specified by the outfile.

  For each map in the collection:
  - :question is a String, and will be shown on the front of the card
  - :answers is a collection of Strings representing the fields on the
    back of the card
  - :tags are an optional collection of Strings that will be set as
    tags. Tags cannot have spaces in them.

  Altogether the first argument for this function should look
  something like:

  [{:question \"Nice weather we're having!\" :answers [\"Y-you too...\"]}
   {:question \"What's up?\" :answers [\"I'm good\"]}
   {:question \"This one or that one?\" :answers [\"Yes\"]}]

  ...give or take some people skills, of course."
  [inmaps outfile]
  (io/copy (io/input-stream (io/resource "blank.sqlite")) (io/file outfile))
  (sql/with-db-connection [db (assoc database-spec :subname outfile)]
    (let [values (map rec/convert-map-to-db inmaps)]
      (sql/insert-multi! db "notes" values)
      (->> (map #(rec/card-record-from-note-id (:id %)) values)
           (sql/insert-multi! db "cards"))))
  outfile)

(defn map-seq-to-package!
  "Like 'map-seq-to-collection!', except the resulting outfile is a
  full-blown Anki package that can be imported into Anki directly.

  Given a collection of maps with :answers, :tags, and :question
  entries, this function will create a new Anki package at the path
  specified by the outfile.

  For each map in the collection:

  - :question is the String that will be shown on the front of the
    card in the resulting package.
  - :answers is a collection of Strings representing the fields on the
    back of the card.
  - :tags is an optional collection of Strings that will be set as
    tags. Tags cannot have spaces in them.

  The first argument to this function should look something similar to:

  [{:question \"Are you going to do more useless basic math examples?\"
    :answers [\"Probably\"]
    :tags #{\"FAQ\"}}
   {:question \"Why are you writing different examples for the same structure?\"
    :answers [\"Because different examples make things easier to remember!\"]
    :tags #{\"FAQ\"}}
  {:question \"What does a card with no tags and two answers look like?\"
   :answers [\"Like this!\" \"See?\"]}]"
  [inmaps outfile]
  (let [collection-file (File/createTempFile "clj-anki" ".apkg")
        media-file (File/createTempFile "media" ".json")]
    (map-seq-to-collection! inmaps (.getAbsoluteFile collection-file))
    (io/copy (io/input-stream (io/resource "media")) media-file)
    (zip/compress-files! [{:name "collection.anki2" :file collection-file}
                          {:name "media" :file media-file}]
                         outfile)
    (io/delete-file media-file)
    (io/delete-file collection-file)))

(defn notes-to-collection!
  "Given a list of notes and a file path, makes an Anki
  collection (database) at `outfile`

  Notes can be in a number of styles. For example:

  - Pairs:
  ([<question> <answer> <question> <answer> ...])

  (let [notes [\"What's new Scooby Doo?\" \"<silence>\"
               \"Where are you?\" \"<more silence>\"]]
    (notes-to-collection! notes \"scooby.anki2\"))

  - Lists:
    ([[<question> <answer> <answer> <answer> ...]
      [<question> ...]])

    (let [notes [[\"What's new Scooby Doo?\" \"<silence>\" \"Zoinks!\"]
                 [\"Where are you?\" \"<more silence>\" \"Ruh-roh!\"]]]
      (notes-to-collection! notes \"scooby.anki2\"))

  - Maps:
    ([{:question <question>
       :answer <answer>
       :tags #{<tag> <tag>}}
      ...])

    (let [notes [{:question \"What's new Scooby Doo?\"
                  :answer \"<silence>\"
                  :tags #{\"Scooby\"}}

                 {:question \"Where are you?\"
                  :answer \"<more silence>\"
                  :tags #{\"Scooby\"}}]]
      (notes-to-collection! notes \"scooby.anki2\"))

  - Maps with multiple answers:
    ([{:question <question>
       :answers [<answer> <answer> ...]
       :tags #{<tag> <tag>}}
      ...])

    (let [notes [{:question \"What's new Scooby Doo?\"
                  :answers [\"<silence>\" \"Zoinks!\"]
                  :tags #{\"Scooby\"}}

                 {:question \"Where are you?\"
                  :answers [\"<more silence>\" \"Ruh-roh!\"]
                  :tags #{\"Scooby\"}}]]
      (notes-to-collection! notes \"scooby.anki2\"))

  - Also mixes of any of the above work in unison:

    (let [notes [\"What's new Scooby Doo?\" \"<silence>\"
                 {:question \"Where are you?\"
                  :answer \"<more silence>\"
                  :tags #{\"Scooby\"}}]]
      (notes-to-collection! notes \"scooby.anki2\"))

  (See `notes-to-package!` for more examples.)"
  [notes outfile]
  (-> (apply normalize-notes notes)
      (map-seq-to-collection! outfile)))

(defn notes-to-package!
  "Given a list of `notes` and an `outfile` (Usually an .apkg),
  this function creates an Anki package containing all of the notes.

  Notes can be in a number of styles. (And mixed!) For example:

  (let [cards [{:question \"Do you know the muffin man?\"
                :answers [\"The muffin man?\" \"The muffin man!\"]}

               \"But, do you know the muffin man?\" \"Yes\"

               {:question \"However, do you know the muffin man?\"
                :answers [\"I do\" \"Stop it\"]}

               {:question \"Do you kn--\"
                :answer \"STOP\"
                :tags #{\"repetition\"}}

               {:question \"Where is the muffin man?\"
                :answer \"Maybe Drury Lane..?\"}

               \"Where?\" \"I said I think he lives on Drury Lane!\"

               [\"What are we talking about?\" \"...\" \"Ugh\"]]]
    (notes-to-package! cards \"muffin-man.apkg\"))

  (See `notes-to-collection!` for more elaborate format examples)"
  [notes outfile]
  (-> (apply normalize-notes notes)
      (map-seq-to-package! outfile)))
