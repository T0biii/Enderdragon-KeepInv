#!/usr/bin/env bash
# Starts a real Paper server in Docker, loads the plugin jar and checks that it enables and works.
# Usage: scripts/smoke-test.sh <plugin-jar> [minecraft-version]
# The minecraft version defaults to the one the plugin is compiled against (gradle/libs.versions.toml).
set -euo pipefail
export MSYS_NO_PATHCONV=1 # keep Git Bash on Windows from rewriting container paths

JAR="${1:?usage: smoke-test.sh <plugin-jar> [minecraft-version]}"
[[ -f "$JAR" ]] || { echo "Jar not found: $JAR" >&2; exit 2; }

MC_VERSION="${2:-}"
if [[ -z "$MC_VERSION" ]]; then
  MC_VERSION="$(sed -n 's/^paper = "\([0-9][0-9.]*\)\.build.*/\1/p' gradle/libs.versions.toml)"
fi
[[ -n "$MC_VERSION" ]] || { echo "Could not determine Minecraft version" >&2; exit 2; }

IMAGE="${SMOKE_IMAGE:-eclipse-temurin:25-jdk}"
NAME="edki-smoke-$$"
WORK="build/smoke"
LOGS="build/smoke-logs"
PLUGIN="EnderDragonKeepInv"

log() { echo "[smoke] $*"; }
server_log() { docker logs "$NAME" 2>&1; }

collect_logs() {
  mkdir -p "$LOGS"
  server_log > "$LOGS/console.log" 2>&1 || true
  docker cp "$NAME:/srv/logs" "$LOGS/logs" >/dev/null 2>&1 || true
  docker cp "$NAME:/srv/plugins/$PLUGIN" "$LOGS/plugin-data" >/dev/null 2>&1 || true
  echo "$MC_VERSION" > "$LOGS/paper-version.txt"
}

cleanup() {
  status=$?
  collect_logs
  if [[ $status -ne 0 ]]; then
    echo "::group::Server log"
    server_log | tail -n 150 || true
    echo "::endgroup::"
  fi
  docker rm -f "$NAME" >/dev/null 2>&1 || true
  exit $status
}
trap cleanup EXIT

fail() { echo "[smoke] FAILED: $*" >&2; exit 1; }

# Waits until the pattern shows up in the server log after the given line offset.
wait_for() {
  local pattern="$1" timeout="$2" offset="${3:-0}" waited=0
  while ! server_log | tail -n +"$((offset + 1))" | grep -Eq "$pattern"; do
    if [[ "$(docker inspect -f '{{.State.Running}}' "$NAME")" != "true" ]]; then
      fail "server exited while waiting for: $pattern"
    fi
    sleep 2
    waited=$((waited + 2))
    [[ $waited -lt $timeout ]] || fail "timeout waiting for: $pattern"
  done
}

# Sends a console command and waits for a matching reply.
run_command() {
  local command="$1" pattern="$2" before
  before="$(server_log | wc -l)"
  log "> $command"
  docker exec "$NAME" sh -c "echo '$command' >> /tmp/in"
  wait_for "$pattern" 60 "$before"
}

log "Resolving Paper $MC_VERSION"
build_json="$(curl -fsSL "https://fill.papermc.io/v3/projects/paper/versions/$MC_VERSION/builds/latest")"
paper_url="$(printf '%s' "$build_json" | grep -Eo 'https://[^"]+\.jar' | head -n 1)"
[[ -n "$paper_url" ]] || fail "no download url for Paper $MC_VERSION"

rm -rf "$WORK" "$LOGS"
mkdir -p "$WORK/plugins"
curl -fsSL "$paper_url" -o "$WORK/paper.jar"
cp "$JAR" "$WORK/plugins/"
echo "eula=true" > "$WORK/eula.txt"
printf 'online-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\n' > "$WORK/server.properties"

log "Starting server"
docker create -i --name "$NAME" -w /srv "$IMAGE" \
  sh -c 'touch /tmp/in; tail -n 0 -f /tmp/in | java -Xmx1500M -jar /srv/paper.jar --nogui' >/dev/null
docker cp "$WORK/." "$NAME:/srv/"
docker start "$NAME" >/dev/null

wait_for 'Done \(' 300
server_log | grep -q "Enabling $PLUGIN" || fail "$PLUGIN was not enabled"
if server_log | grep -Eq "Could not load .*$PLUGIN|Error occurred while enabling $PLUGIN|Disabling $PLUGIN"; then
  fail "$PLUGIN failed to load"
fi
log "Plugin enabled"

run_command 'edki status' 'keep inventory is inactive'
run_command 'execute in minecraft:the_end run forceload add 0 0' 'force loaded'
run_command 'execute in minecraft:the_end run summon minecraft:ender_dragon 0 100 0' 'Summoned new Ender Dragon'
run_command 'edki status' 'keep inventory is ACTIVE'
run_command 'execute in minecraft:the_end run kill @e[type=minecraft:ender_dragon]' 'Killed'
run_command 'edki status' 'keep inventory is inactive'

if server_log | grep -Eq "Exception|\[$PLUGIN\].*(ERROR|SEVERE)"; then
  server_log | grep -E "Exception|\[$PLUGIN\].*(ERROR|SEVERE)" | head -n 10
  fail "errors found in the server log"
fi

log "Stopping server"
docker exec "$NAME" sh -c "echo stop >> /tmp/in"
wait_for 'Closing Server|Stopping server' 60
timeout 60 docker wait "$NAME" >/dev/null || true
log "OK: $PLUGIN works on Paper $MC_VERSION"
