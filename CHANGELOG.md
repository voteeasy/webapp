# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Entries are written in British English.

## Unreleased

### Added

- Introduced this changelog to document notable changes.
- Added a shared IntelliJ IDEA project code style to prevent explicit Java
  imports from being replaced with wildcard imports during optimisation. Only
  the two project code-style files are versioned; personal IDE settings remain
  ignored ([PR #19]).

### Changed

- Updated Apache Commons Lang from 3.20.0 to 3.22.0 ([PR #19]).
- Replaced wildcard imports throughout the Java sources and tests with explicit
  imports, and migrated the remaining JUnit assertions to AssertJ ([PR #19]).
- Restricted `NUMERIC` settings to whole numbers within the Java `int` range
  (-2,147,483,648 to 2,147,483,647), matching the values read by
  `ReadSettingService`. Validation now uses a regular expression for the format
  and `Integer.parseInt()` for the range, rather than `NumberUtils.isParsable()`.
  Optional `+` or `-` signs and leading zeroes are accepted; decimal numbers,
  exponential notation, `NaN`, infinity, whitespace and out-of-range values are
  rejected. `ALPHANUMERIC` and `UUID` validation remain unchanged ([PR #19]).

### Fixed

- Prevented the Commons Lang update from changing numeric setting validation
  behaviour and allowing values that cannot be read as integers ([PR #19]).

[PR #19]: https://github.com/voteeasy/webapp/pull/19
