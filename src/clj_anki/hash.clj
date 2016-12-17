(ns clj-anki.hash
  "A helper library for hashing things."
  (:import java.security.MessageDigest
           java.math.BigInteger))

(defn sha1-hex
  "Returns an SHA1 of the passed-in string."
  [string]
  (.toString
   (->> (.getBytes string)
        (.digest (MessageDigest/getInstance "sha1"))
        (BigInteger. 1))
   16))

(defn anki-checksum [string]
  "Returns an Anki-style string checksum for the passed in string"
  (BigInteger.
   (->> (sha1-hex string)
        (take 8)
        (apply str))
   16))
