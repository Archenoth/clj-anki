# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [0.0.3] - 2017-12-29
### Added
- User-friendly functions that accept a large number of input styles
  for notes, namely `notes-to-package!` and `notes-to-collection!`
- Clojure specs
- Generative testing for the conversion of these input styles

## [0.0.2] - 2016-12-21
### Added
- Considerably nicer documentation in the code itself.
- Much more thorough test coverage
- Information about Clojars and Travis CI in the README
- Continuous integration configuration

## [0.0.1] - 2016-12-17
### Fixed
- No more intermediary files ruining your day! all of them are now
  secure intermediary files.
- Every local file is a resource so JARs can remain self-contained.

## [0.0.0] - 2016-12-16
### Added
- Added function to read apkg files `read-notes`.
- Added function to read anki2 database files
  `read-notes-from-collection`.
- Added function to write apkg files `map-seq-to-package!`.
- Added function to write anki2 database files
  `map-seq-to-collection!`.
- Added basic read/write uniformity tests for both pacakage and
  database files.

[Unreleased]: https://github.com/Archenoth/clj-anki/compare/v0.0.2...HEAD
[0.0.2]: https://github.com/Archenoth/clj-anki/releases/tag/v0.0.2
[0.0.1]: https://github.com/Archenoth/clj-anki/releases/tag/v0.0.1
[0.0.0]: https://github.com/Archenoth/clj-anki/releases/tag/v0.0.0
