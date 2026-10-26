#!/usr/bin/env python3
"""Reuse unchanged adapters from the verified official 3.6.0 distribution.

Only version-specific classes are installed; shared code is always rebuilt.
This is an optional packaging path when historical CraftBukkit build dependencies
are unavailable. The normal full-source Maven build remains supported.
"""
import argparse
import hashlib
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
from zipfile import ZipFile, ZIP_DEFLATED

RELEASE_TAG = "20260804"
RELEASE_SHA256 = "3bf1b4cb445cf3bdd0f1e3e81dcbe864c5e49c29b8fc64b28dff0e3f63dfe720"
# Adapters from 1.17 onward contain runtime fixes and must be rebuilt from source.
VERSIONS = ("v1_12_R1", "v1_13_R1", "v1_13_R2", "v1_14_R1", "v1_15_R1",
            "v1_16_R1", "v1_16_R2", "v1_16_R3")
ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("release_jar", type=Path)
    parser.add_argument("--repository", type=Path, default=Path.home() / ".m2/repository")
    args = parser.parse_args()
    if hashlib.sha256(args.release_jar.read_bytes()).hexdigest() != RELEASE_SHA256:
        raise SystemExit("Expected the unmodified official AbilityWar-3.6.0.jar from release 20260804")
    paths = ["modules/compatibility/Common"] + ["modules/compatibility/" + version for version in VERSIONS]
    subprocess.run(["git", "rev-parse", "--verify", RELEASE_TAG + "^{commit}"], cwd=ROOT, check=True, stdout=subprocess.DEVNULL)
    subprocess.run(["git", "diff", "--exit-code", RELEASE_TAG, "--"] + paths, cwd=ROOT, check=True)
    untracked = subprocess.check_output(["git", "ls-files", "--others", "--exclude-standard", "--"] + paths, cwd=ROOT)
    if untracked.strip():
        raise SystemExit("Legacy modules contain untracked files; rebuild them from source instead")
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    revision = ET.parse(ROOT / "pom.xml").findtext("m:properties/m:revision", namespaces=ns)
    if not revision or any(c in revision for c in "/\\"):
        raise SystemExit("Invalid Maven revision")
    with ZipFile(args.release_jar) as archive:
        classes = {}
        for version in VERSIONS:
            names = [n for n in archive.namelist() if n.startswith("daybreak/abilitywar/")
                     and ("/" + version + "/") in n and n.endswith(".class")]
            required = "daybreak/abilitywar/utils/base/minecraft/nms/" + version + "/NMSImpl.class"
            if required not in names:
                raise SystemExit("Missing adapter in official release: " + version)
            classes[version] = names
        for version, names in classes.items():
            destination = args.repository / "kr/daybreak" / version / revision
            destination.mkdir(parents=True, exist_ok=True)
            artifact = destination / (version + "-" + revision)
            with ZipFile(str(artifact) + ".jar", "w", ZIP_DEFLATED) as output:
                for name in names:
                    output.writestr(archive.getinfo(name), archive.read(name))
            project = ET.Element("project", xmlns=ns["m"])
            for key, value in {
                "modelVersion": "4.0.0", "groupId": "kr.daybreak", "artifactId": version,
                "version": revision,
                "description": "Unchanged adapters from official AbilityWar release " + RELEASE_TAG
                               + "; source archive SHA-256 " + RELEASE_SHA256,
            }.items():
                ET.SubElement(project, key).text = value
            ET.ElementTree(project).write(str(artifact) + ".pom", encoding="utf-8", xml_declaration=True)
            print(version + ": " + str(len(names)) + " unchanged classes installed")


if __name__ == "__main__":
    main()
