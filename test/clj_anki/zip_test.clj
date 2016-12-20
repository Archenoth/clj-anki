(ns clj-anki.zip-test
  (:require [clojure.test :refer :all]
            [clj-anki.zip :refer :all]
            [clojure.java.io :as io]))

(deftest zip-test
  (spit "test/tf" "恥しい" :encoding "UTF-8")

  (let [slurp8 #(slurp % :encoding "UTF-8")]
    (testing "We can compress a file, and get it back out"
      (compress-files! [{:name "test/tf2" :file (io/file "test/tf")}] "test/t.zip")
      (extract-file-from-zip! "test/t.zip" "test/tf2")
      (is (= (slurp8 "test/tf") (slurp8 "test/tf2"))))

    (testing "We can compress a file, and get it back out as another name"
      (compress-files! [{:name "test/tf2" :file (io/file "test/tf")}] "test/t.zip")
      (extract-file-from-zip! "test/t.zip" "test/tf2" "test/tf3")
      (is (= (slurp8 "test/tf") (slurp8 "test/tf3")))))

  (doseq [file ["test/tf" "test/tf2" "test/tf3" "test/t.zip"]]
    (io/delete-file file true)))
