(ns clj-anki.core-test
  (:require [clojure.test :refer :all]
            [clj-anki.core :refer :all]
            [clojure.java.io :as io]
            [clojure.spec :as s]))

(def test-map-list
  "The map of data to test"
  [{:question "3 + 4" :answers ["7"] :tags #{"math"}}
   {:question "4 + 5" :answers ["9"] :tags #{"math"}}])

(def test-collection-file
  "The name of the test Anki collection file"
  "test.anki2")

(def test-package-file
  "The name of the test Anki package file"
  "test.apkg")

(deftest api-ui-test
  (testing "If the user can input all of our accepted spec inputs into normalize-notes"
    (is (s/exercise-fn `normalize-notes))))

(deftest read-write-test
  (testing "Database read and write uniformity"
    (map-seq-to-collection! test-map-list test-collection-file)
    (is (= (set test-map-list)
           (set (read-notes-from-collection test-collection-file))))
    (io/delete-file test-collection-file))

  (testing "Anki package read and write uniformity"
    (map-seq-to-package! test-map-list test-package-file)
    (is (= (set test-map-list)
           (set (read-notes test-package-file))))
    (io/delete-file test-package-file)))
