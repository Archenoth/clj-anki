# Introduction to clj-anki

clj-anki is a library designed to allow the creation and reading of
Anki files, and using it should allow you to create Anki decks from
any arbitrary source that you feel like.

At a basic level, you can make notes as a sequence of pairs of strings as so:
```clojure
["Question" "Answer"
 "Another question" "Another answer"]
```

If you need more control over what goes into the cards, there are a
variety of formats that you can use with this library.

# Including the library

To include the library, simply require `clj-anki.core` as whatever
prefix you like. This file will assume a prefix of `anki`, which can
be done as so:

```clojure
(require '[clj-anki.core :as anki])
```

Or if you want it in your namespace declaration:

```clojure
(ns some-namespace.thing
  "Some documentation (Because you should)"
  (:require [clj-anki.core :as anki]))
```

# Writing

In order to write correctly-formatted map lists to a package that can
be imported by Anki, you would write them, like in
the [README](../README.md) as so:

```clojure
(let [cards ["3 + 4" "7"
             "4 + 5" "9"]
  (anki/notes-to-package! cards "math.apkg"))
```

However, if you wished to have multiple answers, you could wrap either
of these entries in a vector:

```clojure
(let [cards [["3 + 4" "7" "Seven"]
             ["4 + 5" "9" "Nine"]]
  (anki/notes-to-package! cards "math.apkg"))
```

This would set the last two values of each entry as answers in your deck.

If vectors are too squishy for your tastes, you can also do this in map-format:

```clojure
(let [cards [{:question "3 + 4" :answers ["7" "Seven"]}
             {:question "4 + 5" :answers ["9" "Nine"]}]]
  (anki/notes-to-package! cards "math.apkg"))
```

In map format, you can also specify tags as a set of strings:

```clojure
(let [cards [{:question "3 + 4" :answers ["7" "Seven"] :tags #{"math"}}
             {:question "4 + 5" :answers ["9" "Nine"] :tags #{"math"}}]]
  (anki/notes-to-package! cards "math.apkg"))
```

If you only have one answer in map format, you can specify `:answer` as a string
instead of `:answers` too:
```clojure
(let [cards [{:question "3 + 4" :answer "7" :tags #{"math"}}
             {:question "4 + 5" :answer "9" :tags #{"math"}}]]
  (anki/notes-to-package! cards "math.apkg"))
```


If for some reason you just want to write the `.anki2` collection database
file, you can do so like:

```clojure
(let [cards ["3 + 4" "7"
             "4 + 5" "9"]
  (anki/notes-to-collection! cards "math.anki2"))
```

# Reading

In order to read the notes from an Anki package, you can, also like
the [README](../README.md) do that as so:

```clojure
(anki/read-notes "math.apkg")
```

Which after running the code in the writing section, you might see:

```clojure
({:question "4 + 5", :answers ["9"], :tags #{"math"}}
 {:question "3 + 4", :answers ["7"], :tags #{"math"}})
```

If you want to read a collection database, you can do so like:

```clojure
(anki/read-notes-from-collection "math.anki2")
```

Which after running the code in the writing section, also returns:

```clojure
({:question "4 + 5", :answers ["9"]}
 {:question "3 + 4", :answers ["7"]})
```
