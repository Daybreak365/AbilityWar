#!/usr/bin/env bash
set -euo pipefail
source_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
repo_dir=$(cd -- "$source_dir/../../../.." && pwd)
bukkit_jar="${1:?Usage: build.sh <Bukkit 1.15.2 API jar> [AbilityWar.jar]}"
plugin_jar="${2:-$repo_dir/modules/Jar/target/AbilityWar-26.0.0.jar}"
java_bin="${JAVA_HOME:?Set JAVA_HOME to JDK 8 or newer}/bin"
output="$source_dir/../../target/matrix-integration-test"
mkdir -p "$output/classes"
"$java_bin/javac" -source 8 -target 8 -cp "$plugin_jar:$bukkit_jar" -d "$output/classes" "$source_dir/MatrixSmoke.java"
cp "$source_dir/plugin.yml" "$output/classes/plugin.yml"
"$java_bin/jar" cf "$output/MatrixSmoke.jar" -C "$output/classes" .
printf '%s\n' "$output/MatrixSmoke.jar"
