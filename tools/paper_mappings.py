#!/usr/bin/env python3
"""Convert a Paper runtime's official reobfuscation map for SpecialSource.

The runtime supplies Mojang-to-Spigot Tiny v2 mappings. CraftBukkit's versioned
package is deliberately restored as well, allowing Paper to remap the same
adapter that CraftBukkit and Spigot load directly.
"""

import argparse
from pathlib import Path
import re
import zipfile


def write_mappings(server_jar, output_path, craft_revision):
    if not re.fullmatch(r"v1_\d+_R\d+", craft_revision):
        raise ValueError("Expected a CraftBukkit revision such as v1_21_R1")
    entries = []
    with zipfile.ZipFile(server_jar) as runtime:
        lines = runtime.read("META-INF/mappings/reobf.tiny").decode("utf-8").splitlines()
        if not lines or lines[0].split("\t") not in (["tiny", "2", "0", "mojang", "spigot"],
                                         ["tiny", "2", "0", "mojang+yarn", "spigot"]):
            raise ValueError("Expected official Mojang-to-Spigot Tiny v2 mappings")
        owner = None
        for line in lines[1:]:
            parts = line.split("\t")
            if parts[0] == "c":
                if len(parts) != 3:
                    raise ValueError("Malformed class mapping: " + line)
                owner = parts[1]
                entries.append(owner + " " + parts[2])
            elif len(parts) > 1 and parts[0] == "" and parts[1] in ("m", "f"):
                if owner is None or len(parts) != 5:
                    raise ValueError("Malformed member mapping: " + line)
                _, kind, descriptor, source, target = parts
                if kind == "m":
                    entries.append(" ".join((owner, source, descriptor, target)))
                else:
                    entries.append(" ".join((owner, source, target)))
        prefix = "org/bukkit/craftbukkit/"
        for name in sorted(runtime.namelist()):
            if name.startswith(prefix) and name.endswith(".class"):
                source = name[:-6]
                target = prefix + craft_revision + "/" + source[len(prefix):]
                entries.append(source + " " + target)
        # This method serves our public IDummy interface as well as the NMS
        # superclass in Mojang source. Keep the public API name on Spigot;
        # the call to super.isAlive() is still mapped using its NMS owner.
        entries.append("daybreak/abilitywar/utils/base/minecraft/nms/"
                       + craft_revision + "/DummyImpl isAlive ()Z isAlive")
    if not entries:
        raise ValueError("No mappings found")
    Path(output_path).write_text("\n".join(entries) + "\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("server_jar", type=Path)
    parser.add_argument("output_path", type=Path)
    parser.add_argument("craft_revision")
    args = parser.parse_args()
    write_mappings(args.server_jar, args.output_path, args.craft_revision)
