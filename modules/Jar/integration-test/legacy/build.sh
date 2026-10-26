#!/usr/bin/env bash
set -euo pipefail
source_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
repo_dir=$(cd -- "$source_dir/../../../.." && pwd)
server_jar="${1:?Usage: build.sh <patched Paper 1.12.2 jar> [AbilityWar.jar]}"
plugin_jar="${2:-$repo_dir/modules/Jar/target/AbilityWar-26.0.1.jar}"
java_bin="${JAVA_HOME:?Set JAVA_HOME to JDK 8}/bin"
output="$source_dir/../../target/legacy-integration-test"
mkdir -p "$output/classes"
"$java_bin/javac" -source 8 -target 8 -cp "$plugin_jar:$server_jar" -d "$output/classes" "$source_dir/LegacySmoke.java"
cp "$source_dir/plugin.yml" "$output/classes/plugin.yml"
"$java_bin/jar" cf "$output/LegacySmoke.jar" -C "$output/classes" .
printf '%s\n' "$output/LegacySmoke.jar"
