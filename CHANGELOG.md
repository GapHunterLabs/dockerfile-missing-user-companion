<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Dockerfile Missing USER Instruction Companion Changelog

## [Unreleased]

## [0.2.0]

### Fixed

- The final stage's effective `USER` now follows Docker's own stage
  inheritance: when it builds on a previous named stage from the same
  file (`FROM base AS final`, not an external image), the check picks
  up whatever `USER` that stage chain last set instead of resetting to
  "no USER seen" on every `FROM`. Confirmed with 3 tests that failed
  against the 0.1.0 scanner before the fix -- a real false positive on
  the common pattern of a shared `base` stage that switches to a
  non-root user once.
- A file named `Dockerfile.md`/`.markdown`/`.mdx`/`.rst`/`.adoc` is no
  longer treated as a real Dockerfile -- found via a 220-file real-world
  corpus check, where ~10% of files matching the old `dockerfile.*`
  rule by name turned out to be Markdown cheatsheets with unrelated
  `FROM` lines used as documentation examples. Real variant Dockerfiles
  (`Dockerfile.prod`, `Dockerfile.arm64`) are still recognized.
- "Rate on Marketplace" now links to this plugin's own reviews page
  instead of the vendor page (the numeric plugin ID wasn't known yet
  at 0.1.0 time).

## [0.1.0]

### Added

- Warning when a Dockerfile's final build stage never sets a non-root
  `USER` -- the container runs as root by default.
- Reasons over real multi-stage structure: only the file's final
  stage is checked, and the LAST `USER` instruction in that stage
  decides (a switch back to root after a non-root `USER` is still
  flagged).

[Unreleased]: https://github.com/GapHunterLabs/dockerfile-missing-user-companion/compare/0.2.0...HEAD
[0.2.0]: https://github.com/GapHunterLabs/dockerfile-missing-user-companion/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/GapHunterLabs/dockerfile-missing-user-companion/commits/0.1.0
