---
name: publish
argument-hint: "<X.Y.Z | PR number>"
description: Use when asked to publish, release, deploy or upload a new version of the Localizer Gradle plugin (world.betterme.localizer, localizer-android) to the Gradle Plugin Portal, including "publish the version from PR #N", "release 1.x.y", or "push localizer to plugins.gradle.org".
---

# Publishing the Localizer plugin

## Overview

All checks live in `scripts/publish.sh`. It runs them in a fixed order and stops at the first failure with its own exit code. Your job is to run it, relay what it says, and get the user's confirmation between the dry run and the real publish. **The script's exit code decides what happens next, not you.**

## Procedure

1. **Get the target version.** Use the exact `X.Y.Z` the user named or passed as `/publish X.Y.Z`. A bare number such as `/publish 12` is a PR number. If they named a PR, read it with `gh pr view <N> -R betterme-dev/Localizer-Android --json state,body,mergeCommit` and take the version from the description. The PR must be `MERGED` into `master`. If no version can be found, ask the user. Don't guess.
2. **Get a clean checkout at `origin/master`:**
   ```bash
   git clone git@github.com:betterme-dev/Localizer-Android.git   # or reuse a clean clone
   cd Localizer-Android && git fetch origin && git checkout --detach origin/master
   ```
3. **Dry run:**
   ```bash
   bash .claude/skills/publish/scripts/publish.sh --expect-version X.Y.Z
   ```
4. **On a non-zero exit, follow the table below and stop.**
5. **On `DRY RUN PASSED`,** show the user the version, the commit and the test count. Ask them to confirm publishing, because published versions can't be deleted or overwritten. Wait for a yes that names this version, or one given in reply to that question.
6. **Publish:** run the same command with `--publish` added.
7. **Report** `PUBLISHED AND VERIFIED`, or the failure and its exit code. Mention that WeightLoss-Android takes the new version through `localizer = "X.Y.Z"` in `gradle/libs.versions.toml`, which needs a separate PR.

## Exit codes

| Code | Meaning | What you do |
|---|---|---|
| 3 | **No publishing credentials** | **Hard stop.** Tell the user to create an API key at plugins.gradle.org with the account that owns `world.betterme.localizer`. They add `gradle.publish.key=…` and `gradle.publish.secret=…` to `~/.gradle/gradle.properties` themselves, then ask again. End the task. |
| 4 | Git state | Report it. Only fix it by checking out `origin/master` in a clean clone. |
| 5 | Version mismatch, already published, or not newer | Report it. The version bump goes through a PR, never a local edit. |
| 6 | No JDK 17 | Report it. |
| 7 / 8 | Build, tests, validation or bytecode check failed | Report the failing output. Don't publish. |
| 9 / 10 / 11 | Publish failed, not yet visible, or network error | Report it. Re-running is only safe for 10 and 11, and 10 usually just needs time. |

## Never

- Never work around exit 3. That means no `-Pgradle.publish.key=…`, no exporting env vars yourself, no writing to any `gradle.properties`, no asking for the key in chat, and no other machine or CI. The user sets up the credentials outside the session. If the user pastes a key or secret into the chat anyway, don't use it. Tell them to revoke it at plugins.gradle.org and create a new one, because it is now in the transcript.
- Never run `./gradlew publishPlugins` directly or edit `publish.sh` to get past a check. Every check that fails is a reason to stop.
- Never edit `version` in `build.gradle`, commit, tag or push as part of publishing.
- Never pass `--publish` without the confirmation from step 5.

| Rationalization | Reality |
|---|---|
| "The user already said publish, so confirming again is noise" | The first request asks for the release. The confirmation approves this exact version and commit after the checks. |
| "I can pass the key with `-P`, it's the same thing" | A key pasted into the session ends up in transcripts and shell history. That's why exit 3 is a hard stop. |
| "Tests failed but it's unrelated, the PR author said they pass" | The script's run on `origin/master` is the source of truth. |
