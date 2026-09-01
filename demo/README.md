# Demo data for screenshots

`Dockerfile` — no `USER` instruction, flagged on its `FROM` line.
`Dockerfile.safe` — real non-root `USER appuser`, not flagged.

## How to get the screenshot

1. `./gradlew runIde` from `dockerfile-missing-user-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `Dockerfile` — a warning should appear on the
   `FROM node:20-slim` line. Open `Dockerfile.safe` — no warning.
3. Screenshot with the warning visible, save into
   `dockerfile-missing-user-companion/docs/screenshots/`. Close the
   sandbox.
