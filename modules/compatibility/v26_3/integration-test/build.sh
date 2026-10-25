#!/usr/bin/env bash
# Build an optional test plugin for a disposable CraftBukkit 26.3 server.
set -euo pipefail
source_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
repo_dir=$(cd -- "$source_dir/../../../.." && pwd)
server_dir=$(cd -- "${1:?Usage: build.sh <unpacked CraftBukkit server directory>}" && pwd)
java_bin="${JAVA_HOME:?Set JAVA_HOME to JDK 25}/bin"
output="$source_dir/../target/integration-test"
mkdir -p "$output/classes"
test -f "$repo_dir/modules/Jar/target/AbilityWar-26.0.0.jar"
test -d "$server_dir/bundler/versions"
"$java_bin/javac" --release 25 -cp "$repo_dir/modules/Jar/target/AbilityWar-26.0.0.jar:$server_dir/bundler/libraries/*:$server_dir/bundler/versions/*" -d "$output/classes" "$source_dir/Smoke.java" "$source_dir/GameplaySmoke.java"
cp "$source_dir/plugin.yml" "$output/classes/plugin.yml"
"$java_bin/jar" cf "$output/CompatibilitySmoke.jar" -C "$output/classes" .
printf '%s\n' "$output/CompatibilitySmoke.jar"
