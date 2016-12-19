(ns clj-anki.record-test
  (:require [clojure.test :refer :all]
            [clj-anki.record :refer :all]))

(deftest convert-map-to-db-test
  (let [note (convert-map-to-db {:question "What's up?" :answers ["Not much, you?"]})
        keys [:id :guid :mid :mod :usn :tags :flds :sfld :csum :flags :data]]

    (testing "The resulting map has no :question or :answers"
      (is (not-any? #(contains? note %) [:questions :answers])))

    (testing "The resulting map has all of the required keys"
      (is (every? #(contains? note %) keys)))

    (testing "There is nothing extra in the map"
      (is (= (count note) 11)))

    (testing "The resulting map has the cornotet question value"
      (is (= (:sfld note) "What's up?")))

    (testing "The resulting map has the cornotetly-split answer value"
      (is (= (:flds note) "What's up?Not much, you?")))))

(deftest card-record-from-note-id-test
  (let [rec (card-record-from-note-id 7590)
        keys [:id :nid :did :ord :mod :usn :type :queue :due :ivl :factor
              :reps :lapses :left :odue :odid :flags :data]]

    (testing "The Note ID is put in the right place"
      (is (= (:nid rec) 7590)))

    (testing "The resulting map has all of the required keys"
      (is (every? #(contains? rec %) keys)))

    (testing "There is nothing extra in the map"
      (is (= (count rec) 18)))))
