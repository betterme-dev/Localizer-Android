#!/usr/bin/env bash
# Deterministic release gate for the Localizer Gradle plugin (world.betterme.localizer).
#
# Usage (from anywhere inside the Localizer-Android checkout):
#   bash .claude/skills/publishing-localizer-plugin/scripts/publish.sh --expect-version X.Y.Z            # dry run
#   bash .claude/skills/publishing-localizer-plugin/scripts/publish.sh --expect-version X.Y.Z --publish  # real publish
#
# Every check runs in a fixed order and stops at the first failure with its own exit code.
# The script never prints credential values.
#
# Exit codes:
#   0  success (dry run passed, or published and verified)
#   2  usage error
#   3  NO PUBLISHING CREDENTIALS - hard stop
#   4  git state not releasable (dirty tree, not at origin/master)
#   5  version problem (mismatch, malformed, already published, not newer)
#   6  JDK 17 not found
#   7  build, tests or publish validation failed
#   8  wrong bytecode target
#   9  publish failed
#  10  published but not visible on the portal after waiting
#  11  network error talking to the Plugin Portal

set -u
set -o pipefail

PLUGIN_ID="world.betterme.localizer"
METADATA_URL="https://plugins.gradle.org/m2/world/betterme/localizer/localizer-android/maven-metadata.xml"
MARKER_BASE="https://plugins.gradle.org/m2/world/betterme/localizer/world.betterme.localizer.gradle.plugin"
REQUIRED_JDK="17"
REQUIRED_CLASS_MAJOR="61"   # Java 17
CHECK_CLASS="world/betterme/localizer/LocalizerPlugin.class"

EXPECT_VERSION=""
DO_PUBLISH=0

step() { printf '\n==> [%s] %s\n' "$1" "$2"; }
ok()   { printf '    OK: %s\n' "$1"; }
die()  { local code="$1"; shift; printf '\nSTOP (exit %s): %s\n' "$code" "$*" >&2; exit "$code"; }

while [ $# -gt 0 ]; do
  case "$1" in
    --expect-version) [ $# -ge 2 ] || die 2 "--expect-version needs a value"; EXPECT_VERSION="$2"; shift 2 ;;
    --publish) DO_PUBLISH=1; shift ;;
    -h|--help) sed -n '2,26p' "$0"; exit 0 ;;
    *) die 2 "unknown argument: $1" ;;
  esac
done
[ -n "$EXPECT_VERSION" ] || die 2 "--expect-version X.Y.Z is required (the version the user asked to publish)"

# ---------------------------------------------------------------- 1. repo
step 1 "Locate the Localizer-Android checkout"
ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die 4 "not inside a git checkout"
cd "$ROOT" || die 4 "cannot cd to $ROOT"
grep -q "rootProject.name = 'localizer-android'" settings.gradle 2>/dev/null \
  || die 4 "$ROOT is not the Localizer-Android repo (settings.gradle mismatch)"
ok "$ROOT"

# ---------------------------------------------------------------- 2. credentials (HARD STOP)
step 2 "Check Plugin Portal credentials (gradle.publish.key / gradle.publish.secret)"
# Gradle reads these, in this script's supported order:
#   a) env GRADLE_PUBLISH_KEY + GRADLE_PUBLISH_SECRET (supported by com.gradle.plugin-publish)
#   b) $GRADLE_USER_HOME/gradle.properties (default ~/.gradle/gradle.properties)
# The project-level gradle.properties is deliberately NOT accepted: secrets must never be committed.
prop_has_value() { # file key -> 0 if key has a non-empty value
  [ -f "$1" ] || return 1
  tr -d '\r' < "$1" | grep -Eq "^[[:space:]]*$(printf '%s' "$2" | sed 's/\./\\./g')[[:space:]]*[=:][[:space:]]*[^[:space:]]+"
}
USER_PROPS="${GRADLE_USER_HOME:-$HOME/.gradle}/gradle.properties"
CRED_SOURCE=""
if [ -n "${GRADLE_PUBLISH_KEY:-}" ] && [ -n "${GRADLE_PUBLISH_SECRET:-}" ]; then
  CRED_SOURCE="environment (GRADLE_PUBLISH_KEY / GRADLE_PUBLISH_SECRET)"
elif prop_has_value "$USER_PROPS" gradle.publish.key && prop_has_value "$USER_PROPS" gradle.publish.secret; then
  CRED_SOURCE="$USER_PROPS"
fi
if prop_has_value gradle.properties gradle.publish.key || prop_has_value gradle.properties gradle.publish.secret; then
  die 3 "publishing credentials found in the PROJECT gradle.properties. Remove them (never commit secrets) and put them in $USER_PROPS"
fi
if [ -z "$CRED_SOURCE" ]; then
  die 3 "NO PUBLISHING CREDENTIALS. Neither GRADLE_PUBLISH_KEY+GRADLE_PUBLISH_SECRET env vars nor non-empty
      gradle.publish.key + gradle.publish.secret in $USER_PROPS were found.
      The user must create an API key at https://plugins.gradle.org (account that owns $PLUGIN_ID)
      and add both lines to $USER_PROPS themselves. Nothing was built or published."
fi
ok "found in $CRED_SOURCE (values not shown)"

# ---------------------------------------------------------------- 3. git state
step 3 "Check git state: clean tree, HEAD == origin/master"
[ -z "$(git status --porcelain --untracked-files=no)" ] || die 4 "working tree has uncommitted changes to tracked files"
git fetch --quiet origin master || die 4 "git fetch origin master failed"
HEAD_SHA="$(git rev-parse HEAD)"
MASTER_SHA="$(git rev-parse origin/master)"
[ "$HEAD_SHA" = "$MASTER_SHA" ] || die 4 "HEAD ($HEAD_SHA) is not origin/master ($MASTER_SHA). Run: git checkout --detach origin/master"
ok "HEAD = origin/master = ${HEAD_SHA:0:7}"

# ---------------------------------------------------------------- 4. version
step 4 "Check version"
BUILD_VERSION="$(sed -nE "s/^[[:space:]]*version[[:space:]]+['\"]([^'\"]+)['\"].*/\1/p" build.gradle | head -1)"
[ -n "$BUILD_VERSION" ] || die 5 "could not read version from build.gradle"
printf '%s' "$BUILD_VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$' || die 5 "build.gradle version '$BUILD_VERSION' is not X.Y.Z"
[ "$BUILD_VERSION" = "$EXPECT_VERSION" ] \
  || die 5 "build.gradle on origin/master has version $BUILD_VERSION, but $EXPECT_VERSION was requested. Bump it in a PR first; do not edit it here."
METADATA="$(curl -fsS --max-time 30 "$METADATA_URL")" || die 11 "cannot fetch $METADATA_URL"
PUBLISHED="$(printf '%s' "$METADATA" | sed -nE 's/.*<version>([^<]+)<\/version>.*/\1/p')"
LATEST="$(printf '%s' "$METADATA" | sed -nE 's/.*<release>([^<]+)<\/release>.*/\1/p')"
if printf '%s\n' "$PUBLISHED" | grep -Fxq "$BUILD_VERSION"; then
  die 5 "$BUILD_VERSION is already on the Plugin Portal (versions are immutable). Bump to a new version in a PR."
fi
newer() { # $1 > $2 ?
  awk -v a="$1" -v b="$2" 'BEGIN{split(a,x,".");split(b,y,".");for(i=1;i<=3;i++){if(x[i]+0>y[i]+0)exit 0;if(x[i]+0<y[i]+0)exit 1}exit 1}'
}
if [ -n "$LATEST" ]; then newer "$BUILD_VERSION" "$LATEST" || die 5 "$BUILD_VERSION is not newer than the latest published $LATEST"; fi
ok "$BUILD_VERSION (latest published: ${LATEST:-none})"

# ---------------------------------------------------------------- 5. JDK
step 5 "Select JDK $REQUIRED_JDK"
jdk_major() { "$1/bin/java" -version 2>&1 | sed -nE 's/.*version "([0-9]+).*/\1/p' | head -1; }
JDK=""
if [ -x /usr/libexec/java_home ]; then JDK="$(/usr/libexec/java_home -v "$REQUIRED_JDK" 2>/dev/null || true)"; fi
if [ -z "$JDK" ] && [ -n "${JAVA_HOME:-}" ] && [ "$(jdk_major "$JAVA_HOME")" = "$REQUIRED_JDK" ]; then JDK="$JAVA_HOME"; fi
[ -n "$JDK" ] && [ "$(jdk_major "$JDK")" = "$REQUIRED_JDK" ] || die 6 "JDK $REQUIRED_JDK not found (set JAVA_HOME to a JDK $REQUIRED_JDK)"
export JAVA_HOME="$JDK"
ok "$JAVA_HOME"

# ---------------------------------------------------------------- 6. build + test + validate
step 6 "Build, test, validate publication"
./gradlew --no-daemon --console=plain clean build publishPlugins --validate-only \
  || die 7 "gradle build/test/validate failed; fix on a branch and merge a PR"
TESTS="$(cat build/test-results/test/*.xml 2>/dev/null | grep -Eo '<testsuite [^>]*' | sed -nE 's/.*tests="([0-9]+)".*failures="([0-9]+)".*errors="([0-9]+)".*/\1 \2 \3/p' \
  | awk '{t+=$1;f+=$2+$3}END{print t+0, f+0}')"
set -- $TESTS
[ "${1:-0}" -gt 0 ] || die 7 "no test results found"
[ "${2:-1}" -eq 0 ] || die 7 "$2 test failure(s)"
ok "$1 tests passed, validation passed"

# ---------------------------------------------------------------- 7. bytecode target
step 7 "Check bytecode target (Java $REQUIRED_JDK = class major $REQUIRED_CLASS_MAJOR)"
JAR="build/libs/localizer-android-$BUILD_VERSION.jar"
[ -f "$JAR" ] || die 8 "$JAR not built"
MAJOR="$(unzip -p "$JAR" "$CHECK_CLASS" | head -c 8 | od -An -tu1 | awk 'NR==1{print $7*256+$8}')"
[ "$MAJOR" = "$REQUIRED_CLASS_MAJOR" ] || die 8 "class major version is $MAJOR, expected $REQUIRED_CLASS_MAJOR"
ok "major $MAJOR"

if [ "$DO_PUBLISH" -ne 1 ]; then
  printf '\nDRY RUN PASSED: %s %s from %s is ready to publish.\n' "$PLUGIN_ID" "$BUILD_VERSION" "${HEAD_SHA:0:7}"
  printf 'Re-run with --publish after the user confirms. Published versions cannot be deleted or overwritten.\n'
  exit 0
fi

# ---------------------------------------------------------------- 8. publish
step 8 "Publish $PLUGIN_ID $BUILD_VERSION to the Gradle Plugin Portal"
./gradlew --no-daemon --console=plain publishPlugins || die 9 "publishPlugins failed"
ok "upload accepted"

# ---------------------------------------------------------------- 9. verify
step 9 "Verify $BUILD_VERSION is live"
MARKER_URL="$MARKER_BASE/$BUILD_VERSION/world.betterme.localizer.gradle.plugin-$BUILD_VERSION.pom"
i=0
while [ $i -lt 20 ]; do
  CODE="$(curl -s -o /dev/null -w '%{http_code}' --max-time 30 "$MARKER_URL")"
  if [ "$CODE" = "200" ] && curl -fsS --max-time 30 "$METADATA_URL" | grep -q "<version>$BUILD_VERSION</version>"; then
    printf '\nPUBLISHED AND VERIFIED: %s %s (commit %s)\n' "$PLUGIN_ID" "$BUILD_VERSION" "${HEAD_SHA:0:7}"
    exit 0
  fi
  i=$((i + 1)); sleep 15
done
die 10 "published, but $BUILD_VERSION not visible after 5 minutes. Check https://plugins.gradle.org/plugin/$PLUGIN_ID (new versions can need manual approval)."
