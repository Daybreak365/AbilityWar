import importlib.util
from pathlib import Path
import tempfile
import unittest
import zipfile


spec = importlib.util.spec_from_file_location(
    "paper_mappings", Path(__file__).resolve().parents[1] / "paper_mappings.py")
paper_mappings = importlib.util.module_from_spec(spec)
spec.loader.exec_module(paper_mappings)


class PaperMappingsTest(unittest.TestCase):
    def convert(self, text, revision="v1_21_R1"):
        with tempfile.TemporaryDirectory() as directory:
            runtime = Path(directory) / "paper.jar"
            output = Path(directory) / "maps.csrg"
            with zipfile.ZipFile(runtime, "w") as jar:
                jar.writestr("META-INF/mappings/reobf.tiny", text)
                jar.writestr("org/bukkit/craftbukkit/entity/CraftPlayer.class", b"")
                jar.writestr("org/bukkit/entity/Player.class", b"")
            paper_mappings.write_mappings(runtime, output, revision)
            return output.read_text().splitlines()

    def test_members_keep_source_owner_and_descriptor(self):
        result = self.convert(
            "tiny\t2\t0\tmojang\tspigot\n"
            "c\tnet/minecraft/Entity\tnet/minecraft/EntityBase\n"
            "\tf\tZ\tjumping\tbn\n"
            "\tm\t(Lnet/minecraft/Entity;)V\tmove\ta\n")
        self.assertEqual([
            "net/minecraft/Entity net/minecraft/EntityBase",
            "net/minecraft/Entity jumping bn",
            "net/minecraft/Entity move (Lnet/minecraft/Entity;)V a",
            "org/bukkit/craftbukkit/entity/CraftPlayer "
            "org/bukkit/craftbukkit/v1_21_R1/entity/CraftPlayer",
            "daybreak/abilitywar/utils/base/minecraft/nms/v1_21_R1/DummyImpl isAlive ()Z isAlive",
        ], result)

    def test_earlier_paper_namespace_is_accepted(self):
        self.assertEqual("a b", self.convert(
            "tiny\t2\t0\tmojang+yarn\tspigot\nc\ta\tb\n")[0])

    def test_unexpected_namespaces_and_malformed_mapping_fail(self):
        for text in ("", "tiny\t2\t0\tspigot\tmojang\n",
                     "tiny\t2\t0\tmojang\tspigot\n\tf\tZ\tx\ty\n"):
            with self.subTest(text=text), self.assertRaises(ValueError):
                self.convert(text)

    def test_revision_cannot_change_unrelated_packages(self):
        with self.assertRaises(ValueError):
            self.convert("tiny\t2\t0\tmojang\tspigot\nc\ta\tb\n", "../other")


if __name__ == "__main__":
    unittest.main()
