<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Dockerfile Missing USER Instruction Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning when a Dockerfile's final build stage never sets a non-root
  `USER` -- the container runs as root by default.
- Reasons over real multi-stage structure: only the file's final
  stage is checked, and the LAST `USER` instruction in that stage
  decides (a switch back to root after a non-root `USER` is still
  flagged).

[Unreleased]: https://github.com/GapHunterLabs/dockerfile-missing-user-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/dockerfile-missing-user-companion/commits/0.1.0
