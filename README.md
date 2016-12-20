# clj-anki
A Clojure library designed to allow programmatic interaction
with [Anki](http://ankisrs.net/) packages.

[![Build Status](https://travis-ci.org/Archenoth/clj-anki.svg?branch=master)](https://travis-ci.org/Archenoth/clj-anki)
[![Clojars Project](https://img.shields.io/clojars/v/clj-anki.svg)](https://clojars.org/clj-anki)

Leiningen/Boot

```clojure
[clj-anki "0.0.1"]
```

Gradle

```
compile "clj-anki:clj-anki:0.0.1"
```

Maven

```xml
<dependency>
  <groupId>clj-anki</groupId>
  <artifactId>clj-anki</artifactId>
  <version>0.0.1</version>
</dependency>
```

## Usage

After including this library

```clojure
(require '[clj-anki.core :as anki])
```

You can write questions, answers, and tags to Anki packages. For
example, to write some math questions with the tag "`math`" to a file
called "`math.apkg`", you could:

```clojure
(let [cards [{:question "3 + 4" :answers ["7"] :tags #{"math"}}
             {:question "4 + 5" :answers ["9"] :tags #{"math"}}]]
  (anki/map-seq-to-package! cards "math.apkg"))
```

The resulting package you can import into Anki.

You can also read notes from Anki packages:

```clojure
(anki/read-notes "math.apkg")
```

Which after the above returns:

```clojure
({:question "4 + 5", :answers ["9"], :tags #{"math"}}
 {:question "3 + 4", :answers ["7"], :tags #{"math"}})
```

*Slightly* more in-depth documentation can be
found [here](doc/intro.md).

## License

Copyright © 2016 Archenoth

Distributed under the Eclipse Public License either version 1.0
