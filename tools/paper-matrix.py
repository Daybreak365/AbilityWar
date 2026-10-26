#!/usr/bin/env python3
"""Download official Paper test bundles and optionally unpack them with Paperclip.

Does not start a game server, accept the Minecraft EULA, or modify plugin files.
No third-party Python packages are required.
"""

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import urllib.error
import urllib.request
from pathlib import Path


USER_AGENT = "AbilityWar compatibility tests (https://github.com/Daybreak365/AbilityWar)"


def fetch(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": USER_AGENT}), timeout=120)


def checksum(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def prepare(version, args):
    if not re.fullmatch(r"\d+\.\d+(?:\.\d+)?", version):
        raise ValueError("Use an exact stable Minecraft version: " + version)
    destination = args.directory.resolve() / version
    destination.mkdir(parents=True, exist_ok=True)
    metadata_path = destination / "build.json"
    # Reuse the selected build for repeatable reruns; --refresh selects latest.
    if metadata_path.exists() and not args.refresh:
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
    else:
        url = "https://fill.papermc.io/v3/projects/paper/versions/" + version + "/builds"
        try:
            with fetch(url) as response:
                builds = json.load(response)
        except urllib.error.HTTPError as error:
            if error.code == 404:
                raise ValueError("Paper has no archived builds for " + version) from error
            raise
        if not isinstance(builds, list) or not builds:
            raise ValueError("Paper has no archived builds for " + version)
        stable = [build for build in builds if build["channel"] == "STABLE"]
        metadata = max(stable or builds, key=lambda build: build["id"])
        metadata_path.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    download = metadata["downloads"]["server:default"]
    expected = download["checksums"]["sha256"]
    if not re.fullmatch(r"[0-9a-f]{64}", expected):
        raise ValueError("Invalid official SHA256 for " + version)
    server = destination / "paper.jar"
    if not server.exists() or checksum(server) != expected:
        partial = destination / "paper.jar.download"
        try:
            with fetch(download["url"]) as response, partial.open("wb") as output:
                shutil.copyfileobj(response, output)
            if checksum(partial) != expected:
                raise ValueError("Official SHA256 mismatch for " + version)
            partial.replace(server)
        finally:
            partial.unlink(missing_ok=True)
    print(version + " build=" + str(metadata["id"]) + " sha256=" + expected + " path=" + str(server), flush=True)
    if args.java:
        command = [str(args.java.resolve())] + args.jvm_arg + ["-Dpaperclip.patchonly=true", "-jar", str(server)]
        log = destination / "patch.log"
        with log.open("w", encoding="utf-8") as output:
            subprocess.run(command, cwd=destination, stdout=output, stderr=subprocess.STDOUT,
                           check=True, timeout=args.timeout)
        print("Paperclip unpacked " + version + "; log=" + str(log), flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("versions", nargs="+", help="Exact stable releases, e.g. 1.18.2 1.20.6")
    parser.add_argument("--directory", type=Path, required=True, help="Directory for isolated test bundles")
    parser.add_argument("--refresh", action="store_true", help="Refresh the pinned build metadata")
    parser.add_argument("--java", type=Path, help="Java executable for Paperclip unpacking; omit to only download")
    parser.add_argument("--jvm-arg", action="append", default=[], help="Unpacker JVM argument, e.g. --jvm-arg=-Xmx1G")
    parser.add_argument("--timeout", type=int, default=300, help="Paperclip timeout per version in seconds")
    args = parser.parse_args()
    for version in args.versions:
        prepare(version, args)


if __name__ == "__main__":
    main()
