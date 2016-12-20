# Introduction to clj-anki

clj-anki is a library designed to allow the creation and reading of
Anki files, and using it should allow you to create Anki decks from
any arbitrary source that you feel like, so long as you create maps in
the format:

```clojure
{:question "Question side",
 :answers ["Answer 1" "Answer 2"],
 :tags #{"any" "number" "of" "tags"}}
```

This is the primary format that this library reads and writes with.

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
(let [cards [{:question "3 + 4" :answers ["7"] :tags #{"math"}}
             {:question "4 + 5" :answers ["9"] :tags #{"math"}}]]
  (anki/map-seq-to-package! cards "math.apkg"))
```

However, if you just want to write the `.anki2` collection database
file, you can do so like:

```clojure
(let [cards [{:question "3 + 4" :answers ["7"] :tags #{"math"}}
             {:question "4 + 5" :answers ["9"] :tags #{"math"}}]]
  (anki/map-seq-to-collection! cards "math.anki2"))
```

This will create a database file on its own.

# Reading

In order to read the notes from an Anki package, you can, also like
the [README](../README.md) do that as so:

```clojure
(anki/read-notes "math.apkg")
```

Which after running the code in the writing section, returns:

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
({:question "4 + 5", :answers ["9"], :tags #{"math"}}
 {:question "3 + 4", :answers ["7"], :tags #{"math"}})
```
