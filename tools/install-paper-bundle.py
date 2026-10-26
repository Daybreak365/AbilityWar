#!/usr/bin/env python3
"""Install a verified, unpacked official Paper bundle as a compile-only dependency.

Requires Python 3.9 or newer. Run Paper with -Dpaperclip.patchonly=true first.
The server directory must contain
paper.jar and its official fill.papermc.io build response as build.json. This tool
does not execute downloaded code or put server classes into the plugin JAR.
"""

import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import xml.etree.ElementTree as ET
import zipfile


CRAFT_REVISIONS = {
    "1.20.5": "v1_20_R4", "1.20.6": "v1_20_R4",
    "1.21": "v1_21_R1", "1.21.1": "v1_21_R1",
    "1.21.2": "v1_21_R2", "1.21.3": "v1_21_R2",
    "1.21.4": "v1_21_R3", "1.21.5": "v1_21_R4",
    "1.21.6": "v1_21_R5", "1.21.7": "v1_21_R5", "1.21.8": "v1_21_R5",
    "1.21.9": "v1_21_R6", "1.21.10": "v1_21_R6", "1.21.11": "v1_21_R7",
}


def digest(path):
    checksum = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            checksum.update(chunk)
    return checksum.hexdigest()


def checked_file(directory, relative, expected):
    path = (directory / relative).resolve()
    if not path.is_relative_to(directory.resolve()):
        raise ValueError("Bundle path escapes its directory: " + relative)
    if not path.is_file() or digest(path) != expected:
        raise ValueError("Missing or checksum-mismatched bundle file: " + str(path))
    return path


def pom(group, artifact, version, description, dependencies=()):
    root = ET.Element("project", xmlns="http://maven.apache.org/POM/4.0.0")
    for tag, value in (("modelVersion", "4.0.0"), ("groupId", group),
                       ("artifactId", artifact), ("version", version),
                       ("description", description)):
        ET.SubElement(root, tag).text = value
    if dependencies:
        parent = ET.SubElement(root, "dependencies")
        for dep_group, dep_artifact, dep_version in dependencies:
            dependency = ET.SubElement(parent, "dependency")
            for tag, value in (("groupId", dep_group), ("artifactId", dep_artifact),
                               ("version", dep_version)):
                ET.SubElement(dependency, tag).text = value
    ET.indent(root)
    return ET.tostring(root, encoding="utf-8", xml_declaration=True)


def install(repository, source, group, artifact, version, description, dependencies=()):
    directory = repository / group.replace(".", "/") / artifact / version
    directory.mkdir(parents=True, exist_ok=True)
    stem = artifact + "-" + version
    destination = directory / (stem + ".jar")
    if not destination.exists() or digest(destination) != digest(source):
        shutil.copyfile(source, destination)
    (directory / (stem + ".pom")).write_bytes(
        pom(group, artifact, version, description, dependencies))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("server_directory", type=Path)
    parser.add_argument("--maven-repo", type=Path, default=Path.home() / ".m2/repository")
    args = parser.parse_args()
    directory = args.server_directory.resolve()
    metadata = json.loads((directory / "build.json").read_text())
    download = metadata["downloads"]["server:default"]
    bundle_hash = download["checksums"]["sha256"]
    bundle = checked_file(directory, "paper.jar", bundle_hash)
    if not download["url"].startswith("https://fill-data.papermc.io/"):
        raise ValueError("build.json must describe an official Paper download")
    with zipfile.ZipFile(bundle) as archive:
        versions = archive.read("META-INF/versions.list").decode().strip().splitlines()
        if len(versions) != 1:
            raise ValueError("Expected exactly one patched server")
        server_hash, minecraft_version, server_path = versions[0].split("\t")
        if not re.fullmatch(r"\d+(?:\.\d+){1,2}", minecraft_version):
            raise ValueError("Only stable Minecraft releases are accepted")
        server = checked_file(directory / "versions", server_path, server_hash)
        libraries = []
        for line in archive.read("META-INF/libraries.list").decode().strip().splitlines():
            checksum, coordinate, relative = line.split("\t")
            libraries.append((checked_file(directory / "libraries", relative, checksum),
                              checksum, coordinate))
    # Validate the entire bundle before changing the local Maven repository.
    dependencies = []
    for library, checksum, coordinate in libraries:
        # Private fingerprinted coordinates avoid replacing upstream library POMs
        # or accidentally resolving a different transitive version from a repository.
        artifact = "library-" + checksum
        dependency = ("kr.daybreak.paper-bundle", artifact, "1")
        install(args.maven_repo, library, *dependency,
                "Compile-only Paper bundled library " + coordinate)
        dependencies.append(dependency)
    version = minecraft_version + "-R0.1-SNAPSHOT"
    install(args.maven_repo, server, "io.papermc.paper", "paper-server", version,
            "Locally prepared compile-only official Paper build " + str(metadata["id"])
            + "; source SHA-256 " + bundle_hash, dependencies)
    if minecraft_version in CRAFT_REVISIONS:
        from paper_mappings import write_mappings
        mappings = (args.maven_repo / "io/papermc/paper/paper-server" / version
                    / ("paper-server-" + version + "-maps-spigot.csrg"))
        write_mappings(server, mappings, CRAFT_REVISIONS[minecraft_version])
    print("Installed io.papermc.paper:paper-server:" + version
          + " and " + str(len(libraries)) + " verified compile libraries")


if __name__ == "__main__":
    main()
