# Demo data for screenshots

`Dockerfile` — no `USER` instruction, flagged on its `FROM` line.
`Dockerfile.safe` — real non-root `USER appuser`, not flagged.
`Dockerfile.multistage-shared-user` — final stage builds on a
PREVIOUS named stage (`FROM base AS final`) that already set a
non-root `USER`; not flagged since 0.2.0 (0.1.0 flagged this by
mistake, since it reset the tracked user on every `FROM`).

## How to get the screenshot

1. `./gradlew runIde` from `dockerfile-missing-user-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `Dockerfile` — a warning should appear on the
   `FROM node:20-slim` line. Open `Dockerfile.safe` and
   `Dockerfile.multistage-shared-user` — no warning on either.
3. Screenshot with the warning visible, save into
   `dockerfile-missing-user-companion/docs/screenshots/`. Close the
   sandbox.
