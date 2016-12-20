(ns clj-anki.hash-test
  (:require [clojure.test :refer :all]
            [clj-anki.hash :refer :all]))

(deftest sha1-test
  (testing "We can get an SHA1 hex string of the passed in value"
    (is (= (sha1-hex "embarrasing") "f20ee772b10c517107620d3be38697967a7a2e96"))
    (is (= (sha1-hex "漢字") "50008262c76205f015248f124c87b9fe463ead9f"))))

(deftest anki-checksum-test
  (testing "We mangle strings into integers the right way for Anki"
    (is (= (anki-checksum "embarrasing") 4061063026))
    (is (= (anki-checksum "漢字") 1342210658))))
