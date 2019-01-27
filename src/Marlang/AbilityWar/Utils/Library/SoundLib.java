package Marlang.AbilityWar.Utils.Library;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 소리 라이브러리
 * @author _Marlang 말랑
 */
public class SoundLib {

	public static Sounds AMBIENT_CAVE = new Sounds("AMBIENT_CAVE");
	public static Sounds BLOCK_ANVIL_BREAK = new Sounds("BLOCK_ANVIL_BREAK");
	public static Sounds BLOCK_ANVIL_DESTROY = new Sounds("BLOCK_ANVIL_DESTROY");
	public static Sounds BLOCK_ANVIL_FALL = new Sounds("BLOCK_ANVIL_FALL");
	public static Sounds BLOCK_ANVIL_HIT = new Sounds("BLOCK_ANVIL_HIT");
	public static Sounds BLOCK_ANVIL_LAND = new Sounds("BLOCK_ANVIL_LAND");
	public static Sounds BLOCK_ANVIL_PLACE = new Sounds("BLOCK_ANVIL_PLACE");
	public static Sounds BLOCK_ANVIL_STEP = new Sounds("BLOCK_ANVIL_STEP");
	public static Sounds BLOCK_ANVIL_USE = new Sounds("BLOCK_ANVIL_USE");
	public static Sounds BLOCK_BREWING_STAND_BREW = new Sounds("BLOCK_BREWING_STAND_BREW");
	public static Sounds BLOCK_CHEST_CLOSE = new Sounds("BLOCK_CHEST_CLOSE");
	public static Sounds BLOCK_CHEST_LOCKED = new Sounds("BLOCK_CHEST_LOCKED");
	public static Sounds BLOCK_CHEST_OPEN = new Sounds("BLOCK_CHEST_OPEN");
	public static Sounds BLOCK_CHORUS_FLOWER_DEATH = new Sounds("BLOCK_CHORUS_FLOWER_DEATH");
	public static Sounds BLOCK_CHORUS_FLOWER_GROW = new Sounds("BLOCK_CHORUS_FLOWER_GROW");
	public static Sounds BLOCK_CLOTH_BREAK = new Sounds("BLOCK_CLOTH_BREAK");
	public static Sounds BLOCK_CLOTH_FALL = new Sounds("BLOCK_CLOTH_FALL");
	public static Sounds BLOCK_CLOTH_HIT = new Sounds("BLOCK_CLOTH_HIT");
	public static Sounds BLOCK_CLOTH_PLACE = new Sounds("BLOCK_CLOTH_PLACE");
	public static Sounds BLOCK_CLOTH_STEP = new Sounds("BLOCK_CLOTH_STEP");
	public static Sounds BLOCK_COMPARATOR_CLICK = new Sounds("BLOCK_COMPARATOR_CLICK");
	public static Sounds BLOCK_DISPENSER_DISPENSE = new Sounds("BLOCK_DISPENSER_DISPENSE");
	public static Sounds BLOCK_DISPENSER_FAIL = new Sounds("BLOCK_DISPENSER_FAIL");
	public static Sounds BLOCK_DISPENSER_LAUNCH = new Sounds("BLOCK_DISPENSER_LAUNCH");
	public static Sounds BLOCK_ENCHANTMENT_TABLE_USE = new Sounds("BLOCK_ENCHANTMENT_TABLE_USE");
	public static Sounds BLOCK_ENDERCHEST_CLOSE = new Sounds("BLOCK_ENDERCHEST_CLOSE");
	public static Sounds BLOCK_ENDERCHEST_OPEN = new Sounds("BLOCK_ENDERCHEST_OPEN");
	public static Sounds BLOCK_END_GATEWAY_SPAWN = new Sounds("BLOCK_END_GATEWAY_SPAWN");
	public static Sounds BLOCK_END_PORTAL_FRAME_FILL = new Sounds("BLOCK_END_PORTAL_FRAME_FILL");
	public static Sounds BLOCK_END_PORTAL_SPAWN = new Sounds("BLOCK_END_PORTAL_SPAWN");
	public static Sounds BLOCK_FENCE_GATE_CLOSE = new Sounds("BLOCK_FENCE_GATE_CLOSE");
	public static Sounds BLOCK_FENCE_GATE_OPEN = new Sounds("BLOCK_FENCE_GATE_OPEN");
	public static Sounds BLOCK_FIRE_AMBIENT = new Sounds("BLOCK_FIRE_AMBIENT");
	public static Sounds BLOCK_FIRE_EXTINGUISH = new Sounds("BLOCK_FIRE_EXTINGUISH");
	public static Sounds BLOCK_FURNACE_FIRE_CRACKLE = new Sounds("BLOCK_FURNACE_FIRE_CRACKLE");
	public static Sounds BLOCK_GLASS_BREAK = new Sounds("BLOCK_GLASS_BREAK");
	public static Sounds BLOCK_GLASS_FALL = new Sounds("BLOCK_GLASS_FALL");
	public static Sounds BLOCK_GLASS_HIT = new Sounds("BLOCK_GLASS_HIT");
	public static Sounds BLOCK_GLASS_PLACE = new Sounds("BLOCK_GLASS_PLACE");
	public static Sounds BLOCK_GLASS_STEP = new Sounds("BLOCK_GLASS_STEP");
	public static Sounds BLOCK_GRASS_BREAK = new Sounds("BLOCK_GRASS_BREAK");
	public static Sounds BLOCK_GRASS_FALL = new Sounds("BLOCK_GRASS_FALL");
	public static Sounds BLOCK_GRASS_HIT = new Sounds("BLOCK_GRASS_HIT");
	public static Sounds BLOCK_GRASS_PLACE = new Sounds("BLOCK_GRASS_PLACE");
	public static Sounds BLOCK_GRASS_STEP = new Sounds("BLOCK_GRASS_STEP");
	public static Sounds BLOCK_GRAVEL_BREAK = new Sounds("BLOCK_GRAVEL_BREAK");
	public static Sounds BLOCK_GRAVEL_FALL = new Sounds("BLOCK_GRAVEL_FALL");
	public static Sounds BLOCK_GRAVEL_HIT = new Sounds("BLOCK_GRAVEL_HIT");
	public static Sounds BLOCK_GRAVEL_PLACE = new Sounds("BLOCK_GRAVEL_PLACE");
	public static Sounds BLOCK_GRAVEL_STEP = new Sounds("BLOCK_GRAVEL_STEP");
	public static Sounds BLOCK_IRON_DOOR_CLOSE = new Sounds("BLOCK_IRON_DOOR_CLOSE");
	public static Sounds BLOCK_IRON_DOOR_OPEN = new Sounds("BLOCK_IRON_DOOR_OPEN");
	public static Sounds BLOCK_IRON_TRAPDOOR_CLOSE = new Sounds("BLOCK_IRON_TRAPDOOR_CLOSE");
	public static Sounds BLOCK_IRON_TRAPDOOR_OPEN = new Sounds("BLOCK_IRON_TRAPDOOR_OPEN");
	public static Sounds BLOCK_LADDER_BREAK = new Sounds("BLOCK_LADDER_BREAK");
	public static Sounds BLOCK_LADDER_FALL = new Sounds("BLOCK_LADDER_FALL");
	public static Sounds BLOCK_LADDER_HIT = new Sounds("BLOCK_LADDER_HIT");
	public static Sounds BLOCK_LADDER_PLACE = new Sounds("BLOCK_LADDER_PLACE");
	public static Sounds BLOCK_LADDER_STEP = new Sounds("BLOCK_LADDER_STEP");
	public static Sounds BLOCK_LAVA_AMBIENT = new Sounds("BLOCK_LAVA_AMBIENT");
	public static Sounds BLOCK_LAVA_EXTINGUISH = new Sounds("BLOCK_LAVA_EXTINGUISH");
	public static Sounds BLOCK_LAVA_POP = new Sounds("BLOCK_LAVA_POP");
	public static Sounds BLOCK_LEVER_CLICK = new Sounds("BLOCK_LEVER_CLICK");
	public static Sounds BLOCK_METAL_BREAK = new Sounds("BLOCK_METAL_BREAK");
	public static Sounds BLOCK_METAL_FALL = new Sounds("BLOCK_METAL_FALL");
	public static Sounds BLOCK_METAL_HIT = new Sounds("BLOCK_METAL_HIT");
	public static Sounds BLOCK_METAL_PLACE = new Sounds("BLOCK_METAL_PLACE");
	public static Sounds BLOCK_METAL_PRESSUREPLATE_CLICK_OFF = new Sounds("BLOCK_METAL_PRESSUREPLATE_CLICK_OFF");
	public static Sounds BLOCK_METAL_PRESSUREPLATE_CLICK_ON = new Sounds("BLOCK_METAL_PRESSUREPLATE_CLICK_ON");
	public static Sounds BLOCK_METAL_STEP = new Sounds("BLOCK_METAL_STEP");
	public static Sounds BLOCK_NOTE_BASEDRUM = new Sounds("BLOCK_NOTE_BASEDRUM");
	public static Sounds BLOCK_NOTE_BASS = new Sounds("BLOCK_NOTE_BASS");
	public static Sounds BLOCK_NOTE_BELL = new Sounds("BLOCK_NOTE_BELL");
	public static Sounds BLOCK_NOTE_CHIME = new Sounds("BLOCK_NOTE_CHIME");
	public static Sounds BLOCK_NOTE_FLUTE = new Sounds("BLOCK_NOTE_FLUTE");
	public static Sounds BLOCK_NOTE_GUITAR = new Sounds("BLOCK_NOTE_GUITAR");
	public static Sounds BLOCK_NOTE_HARP = new Sounds("BLOCK_NOTE_HARP");
	public static Sounds BLOCK_NOTE_HAT = new Sounds("BLOCK_NOTE_HAT");
	public static Sounds BLOCK_NOTE_PLING = new Sounds("BLOCK_NOTE_PLING");
	public static Sounds BLOCK_NOTE_SNARE = new Sounds("BLOCK_NOTE_SNARE");
	public static Sounds BLOCK_NOTE_XYLOPHONE = new Sounds("BLOCK_NOTE_XYLOPHONE");
	public static Sounds BLOCK_PISTON_CONTRACT = new Sounds("BLOCK_PISTON_CONTRACT");
	public static Sounds BLOCK_PISTON_EXTEND = new Sounds("BLOCK_PISTON_EXTEND");
	public static Sounds BLOCK_PORTAL_AMBIENT = new Sounds("BLOCK_PORTAL_AMBIENT");
	public static Sounds BLOCK_PORTAL_TRAVEL = new Sounds("BLOCK_PORTAL_TRAVEL");
	public static Sounds BLOCK_PORTAL_TRIGGER = new Sounds("BLOCK_PORTAL_TRIGGER");
	public static Sounds BLOCK_REDSTONE_TORCH_BURNOUT = new Sounds("BLOCK_REDSTONE_TORCH_BURNOUT");
	public static Sounds BLOCK_SAND_BREAK = new Sounds("BLOCK_SAND_BREAK");
	public static Sounds BLOCK_SAND_FALL = new Sounds("BLOCK_SAND_FALL");
	public static Sounds BLOCK_SAND_HIT = new Sounds("BLOCK_SAND_HIT");
	public static Sounds BLOCK_SAND_PLACE = new Sounds("BLOCK_SAND_PLACE");
	public static Sounds BLOCK_SAND_STEP = new Sounds("BLOCK_SAND_STEP");
	public static Sounds BLOCK_SHULKER_BOX_CLOSE = new Sounds("BLOCK_SHULKER_BOX_CLOSE");
	public static Sounds BLOCK_SHULKER_BOX_OPEN = new Sounds("BLOCK_SHULKER_BOX_OPEN");
	public static Sounds BLOCK_SLIME_BREAK = new Sounds("BLOCK_SLIME_BREAK");
	public static Sounds BLOCK_SLIME_FALL = new Sounds("BLOCK_SLIME_FALL");
	public static Sounds BLOCK_SLIME_HIT = new Sounds("BLOCK_SLIME_HIT");
	public static Sounds BLOCK_SLIME_PLACE = new Sounds("BLOCK_SLIME_PLACE");
	public static Sounds BLOCK_SLIME_STEP = new Sounds("BLOCK_SLIME_STEP");
	public static Sounds BLOCK_SNOW_BREAK = new Sounds("BLOCK_SNOW_BREAK");
	public static Sounds BLOCK_SNOW_FALL = new Sounds("BLOCK_SNOW_FALL");
	public static Sounds BLOCK_SNOW_HIT = new Sounds("BLOCK_SNOW_HIT");
	public static Sounds BLOCK_SNOW_PLACE = new Sounds("BLOCK_SNOW_PLACE");
	public static Sounds BLOCK_SNOW_STEP = new Sounds("BLOCK_SNOW_STEP");
	public static Sounds BLOCK_STONE_BREAK = new Sounds("BLOCK_STONE_BREAK");
	public static Sounds BLOCK_STONE_BUTTON_CLICK_OFF = new Sounds("BLOCK_STONE_BUTTON_CLICK_OFF");
	public static Sounds BLOCK_STONE_BUTTON_CLICK_ON = new Sounds("BLOCK_STONE_BUTTON_CLICK_ON");
	public static Sounds BLOCK_STONE_FALL = new Sounds("BLOCK_STONE_FALL");
	public static Sounds BLOCK_STONE_HIT = new Sounds("BLOCK_STONE_HIT");
	public static Sounds BLOCK_STONE_PLACE = new Sounds("BLOCK_STONE_PLACE");
	public static Sounds BLOCK_STONE_PRESSUREPLATE_CLICK_OFF = new Sounds("BLOCK_STONE_PRESSUREPLATE_CLICK_OFF");
	public static Sounds BLOCK_STONE_PRESSUREPLATE_CLICK_ON = new Sounds("BLOCK_STONE_PRESSUREPLATE_CLICK_ON");
	public static Sounds BLOCK_STONE_STEP = new Sounds("BLOCK_STONE_STEP");
	public static Sounds BLOCK_TRIPWIRE_ATTACH = new Sounds("BLOCK_TRIPWIRE_ATTACH");
	public static Sounds BLOCK_TRIPWIRE_CLICK_OFF = new Sounds("BLOCK_TRIPWIRE_CLICK_OFF");
	public static Sounds BLOCK_TRIPWIRE_CLICK_ON = new Sounds("BLOCK_TRIPWIRE_CLICK_ON");
	public static Sounds BLOCK_TRIPWIRE_DETACH = new Sounds("BLOCK_TRIPWIRE_DETACH");
	public static Sounds BLOCK_WATERLILY_PLACE = new Sounds("BLOCK_WATERLILY_PLACE");
	public static Sounds BLOCK_WATER_AMBIENT = new Sounds("BLOCK_WATER_AMBIENT");
	public static Sounds BLOCK_WOODEN_DOOR_CLOSE = new Sounds("BLOCK_WOODEN_DOOR_CLOSE");
	public static Sounds BLOCK_WOODEN_DOOR_OPEN = new Sounds("BLOCK_WOODEN_DOOR_OPEN");
	public static Sounds BLOCK_WOODEN_TRAPDOOR_CLOSE = new Sounds("BLOCK_WOODEN_TRAPDOOR_CLOSE");
	public static Sounds BLOCK_WOODEN_TRAPDOOR_OPEN = new Sounds("BLOCK_WOODEN_TRAPDOOR_OPEN");
	public static Sounds BLOCK_WOOD_BREAK = new Sounds("BLOCK_WOOD_BREAK");
	public static Sounds BLOCK_WOOD_BUTTON_CLICK_OFF = new Sounds("BLOCK_WOOD_BUTTON_CLICK_OFF");
	public static Sounds BLOCK_WOOD_BUTTON_CLICK_ON = new Sounds("BLOCK_WOOD_BUTTON_CLICK_ON");
	public static Sounds BLOCK_WOOD_FALL = new Sounds("BLOCK_WOOD_FALL");
	public static Sounds BLOCK_WOOD_HIT = new Sounds("BLOCK_WOOD_HIT");
	public static Sounds BLOCK_WOOD_PLACE = new Sounds("BLOCK_WOOD_PLACE");
	public static Sounds BLOCK_WOOD_PRESSUREPLATE_CLICK_OFF = new Sounds("BLOCK_WOOD_PRESSUREPLATE_CLICK_OFF");
	public static Sounds BLOCK_WOOD_PRESSUREPLATE_CLICK_ON = new Sounds("BLOCK_WOOD_PRESSUREPLATE_CLICK_ON");
	public static Sounds BLOCK_WOOD_STEP = new Sounds("BLOCK_WOOD_STEP");
	public static Sounds ENCHANT_THORNS_HIT = new Sounds("ENCHANT_THORNS_HIT");
	public static Sounds ENTITY_ARMORSTAND_BREAK = new Sounds("ENTITY_ARMORSTAND_BREAK");
	public static Sounds ENTITY_ARMORSTAND_FALL = new Sounds("ENTITY_ARMORSTAND_FALL");
	public static Sounds ENTITY_ARMORSTAND_HIT = new Sounds("ENTITY_ARMORSTAND_HIT");
	public static Sounds ENTITY_ARMORSTAND_PLACE = new Sounds("ENTITY_ARMORSTAND_PLACE");
	public static Sounds ENTITY_ARROW_HIT = new Sounds("ENTITY_ARROW_HIT");
	public static Sounds ENTITY_ARROW_HIT_PLAYER = new Sounds("ENTITY_ARROW_HIT_PLAYER");
	public static Sounds ENTITY_ARROW_SHOOT = new Sounds("ENTITY_ARROW_SHOOT");
	public static Sounds ENTITY_BAT_AMBIENT = new Sounds("ENTITY_BAT_AMBIENT");
	public static Sounds ENTITY_BAT_DEATH = new Sounds("ENTITY_BAT_DEATH");
	public static Sounds ENTITY_BAT_HURT = new Sounds("ENTITY_BAT_HURT");
	public static Sounds ENTITY_BAT_LOOP = new Sounds("ENTITY_BAT_LOOP");
	public static Sounds ENTITY_BAT_TAKEOFF = new Sounds("ENTITY_BAT_TAKEOFF");
	public static Sounds ENTITY_BLAZE_AMBIENT = new Sounds("ENTITY_BLAZE_AMBIENT");
	public static Sounds ENTITY_BLAZE_BURN = new Sounds("ENTITY_BLAZE_BURN");
	public static Sounds ENTITY_BLAZE_DEATH = new Sounds("ENTITY_BLAZE_DEATH");
	public static Sounds ENTITY_BLAZE_HURT = new Sounds("ENTITY_BLAZE_HURT");
	public static Sounds ENTITY_BLAZE_SHOOT = new Sounds("ENTITY_BLAZE_SHOOT");
	public static Sounds ENTITY_BOAT_PADDLE_LAND = new Sounds("ENTITY_BOAT_PADDLE_LAND");
	public static Sounds ENTITY_BOAT_PADDLE_WATER = new Sounds("ENTITY_BOAT_PADDLE_WATER");
	public static Sounds ENTITY_BOBBER_RETRIEVE = new Sounds("ENTITY_BOBBER_RETRIEVE");
	public static Sounds ENTITY_BOBBER_SPLASH = new Sounds("ENTITY_BOBBER_SPLASH");
	public static Sounds ENTITY_BOBBER_THROW = new Sounds("ENTITY_BOBBER_THROW");
	public static Sounds ENTITY_CAT_AMBIENT = new Sounds("ENTITY_CAT_AMBIENT");
	public static Sounds ENTITY_CAT_DEATH = new Sounds("ENTITY_CAT_DEATH");
	public static Sounds ENTITY_CAT_HISS = new Sounds("ENTITY_CAT_HISS");
	public static Sounds ENTITY_CAT_HURT = new Sounds("ENTITY_CAT_HURT");
	public static Sounds ENTITY_CAT_PURR = new Sounds("ENTITY_CAT_PURR");
	public static Sounds ENTITY_CAT_PURREOW = new Sounds("ENTITY_CAT_PURREOW");
	public static Sounds ENTITY_CHICKEN_AMBIENT = new Sounds("ENTITY_CHICKEN_AMBIENT");
	public static Sounds ENTITY_CHICKEN_DEATH = new Sounds("ENTITY_CHICKEN_DEATH");
	public static Sounds ENTITY_CHICKEN_EGG = new Sounds("ENTITY_CHICKEN_EGG");
	public static Sounds ENTITY_CHICKEN_HURT = new Sounds("ENTITY_CHICKEN_HURT");
	public static Sounds ENTITY_CHICKEN_STEP = new Sounds("ENTITY_CHICKEN_STEP");
	public static Sounds ENTITY_COW_AMBIENT = new Sounds("ENTITY_COW_AMBIENT");
	public static Sounds ENTITY_COW_DEATH = new Sounds("ENTITY_COW_DEATH");
	public static Sounds ENTITY_COW_HURT = new Sounds("ENTITY_COW_HURT");
	public static Sounds ENTITY_COW_MILK = new Sounds("ENTITY_COW_MILK");
	public static Sounds ENTITY_COW_STEP = new Sounds("ENTITY_COW_STEP");
	public static Sounds ENTITY_CREEPER_DEATH = new Sounds("ENTITY_CREEPER_DEATH");
	public static Sounds ENTITY_CREEPER_HURT = new Sounds("ENTITY_CREEPER_HURT");
	public static Sounds ENTITY_CREEPER_PRIMED = new Sounds("ENTITY_CREEPER_PRIMED");
	public static Sounds ENTITY_DONKEY_AMBIENT = new Sounds("ENTITY_DONKEY_AMBIENT");
	public static Sounds ENTITY_DONKEY_ANGRY = new Sounds("ENTITY_DONKEY_ANGRY");
	public static Sounds ENTITY_DONKEY_CHEST = new Sounds("ENTITY_DONKEY_CHEST");
	public static Sounds ENTITY_DONKEY_DEATH = new Sounds("ENTITY_DONKEY_DEATH");
	public static Sounds ENTITY_DONKEY_HURT = new Sounds("ENTITY_DONKEY_HURT");
	public static Sounds ENTITY_EGG_THROW = new Sounds("ENTITY_EGG_THROW");
	public static Sounds ENTITY_ELDER_GUARDIAN_AMBIENT = new Sounds("ENTITY_ELDER_GUARDIAN_AMBIENT");
	public static Sounds ENTITY_ELDER_GUARDIAN_AMBIENT_LAND = new Sounds("ENTITY_ELDER_GUARDIAN_AMBIENT_LAND");
	public static Sounds ENTITY_ELDER_GUARDIAN_CURSE = new Sounds("ENTITY_ELDER_GUARDIAN_CURSE");
	public static Sounds ENTITY_ELDER_GUARDIAN_DEATH = new Sounds("ENTITY_ELDER_GUARDIAN_DEATH");
	public static Sounds ENTITY_ELDER_GUARDIAN_DEATH_LAND = new Sounds("ENTITY_ELDER_GUARDIAN_DEATH_LAND");
	public static Sounds ENTITY_ELDER_GUARDIAN_FLOP = new Sounds("ENTITY_ELDER_GUARDIAN_FLOP");
	public static Sounds ENTITY_ELDER_GUARDIAN_HURT = new Sounds("ENTITY_ELDER_GUARDIAN_HURT");
	public static Sounds ENTITY_ELDER_GUARDIAN_HURT_LAND = new Sounds("ENTITY_ELDER_GUARDIAN_HURT_LAND");
	public static Sounds ENTITY_ENDERDRAGON_AMBIENT = new Sounds("ENTITY_ENDERDRAGON_AMBIENT");
	public static Sounds ENTITY_ENDERDRAGON_DEATH = new Sounds("ENTITY_ENDERDRAGON_DEATH");
	public static Sounds ENTITY_ENDERDRAGON_FIREBALL_EXPLODE = new Sounds("ENTITY_ENDERDRAGON_FIREBALL_EXPLODE");
	public static Sounds ENTITY_ENDERDRAGON_FLAP = new Sounds("ENTITY_ENDERDRAGON_FLAP");
	public static Sounds ENTITY_ENDERDRAGON_GROWL = new Sounds("ENTITY_ENDERDRAGON_GROWL");
	public static Sounds ENTITY_ENDERDRAGON_HURT = new Sounds("ENTITY_ENDERDRAGON_HURT");
	public static Sounds ENTITY_ENDERDRAGON_SHOOT = new Sounds("ENTITY_ENDERDRAGON_SHOOT");
	public static Sounds ENTITY_ENDEREYE_DEATH = new Sounds("ENTITY_ENDEREYE_DEATH");
	public static Sounds ENTITY_ENDEREYE_LAUNCH = new Sounds("ENTITY_ENDEREYE_LAUNCH");
	public static Sounds ENTITY_ENDERMEN_AMBIENT = new Sounds("ENTITY_ENDERMEN_AMBIENT");
	public static Sounds ENTITY_ENDERMEN_DEATH = new Sounds("ENTITY_ENDERMEN_DEATH");
	public static Sounds ENTITY_ENDERMEN_HURT = new Sounds("ENTITY_ENDERMEN_HURT");
	public static Sounds ENTITY_ENDERMEN_SCREAM = new Sounds("ENTITY_ENDERMEN_SCREAM");
	public static Sounds ENTITY_ENDERMEN_STARE = new Sounds("ENTITY_ENDERMEN_STARE");
	public static Sounds ENTITY_ENDERMEN_TELEPORT = new Sounds("ENTITY_ENDERMEN_TELEPORT");
	public static Sounds ENTITY_ENDERMITE_AMBIENT = new Sounds("ENTITY_ENDERMITE_AMBIENT");
	public static Sounds ENTITY_ENDERMITE_DEATH = new Sounds("ENTITY_ENDERMITE_DEATH");
	public static Sounds ENTITY_ENDERMITE_HURT = new Sounds("ENTITY_ENDERMITE_HURT");
	public static Sounds ENTITY_ENDERMITE_STEP = new Sounds("ENTITY_ENDERMITE_STEP");
	public static Sounds ENTITY_ENDERPEARL_THROW = new Sounds("ENTITY_ENDERPEARL_THROW");
	public static Sounds ENTITY_EVOCATION_FANGS_ATTACK = new Sounds("ENTITY_EVOCATION_FANGS_ATTACK");
	public static Sounds ENTITY_EVOCATION_ILLAGER_AMBIENT = new Sounds("ENTITY_EVOCATION_ILLAGER_AMBIENT");
	public static Sounds ENTITY_EVOCATION_ILLAGER_CAST_SPELL = new Sounds("ENTITY_EVOCATION_ILLAGER_CAST_SPELL");
	public static Sounds ENTITY_EVOCATION_ILLAGER_DEATH = new Sounds("ENTITY_EVOCATION_ILLAGER_DEATH");
	public static Sounds ENTITY_EVOCATION_ILLAGER_HURT = new Sounds("ENTITY_EVOCATION_ILLAGER_HURT");
	public static Sounds ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK = new Sounds("ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK");
	public static Sounds ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON = new Sounds("ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON");
	public static Sounds ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO = new Sounds("ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO");
	public static Sounds ENTITY_EXPERIENCE_BOTTLE_THROW = new Sounds("ENTITY_EXPERIENCE_BOTTLE_THROW");
	public static Sounds ENTITY_EXPERIENCE_ORB_PICKUP = new Sounds("ENTITY_EXPERIENCE_ORB_PICKUP");
	public static Sounds ENTITY_FIREWORK_BLAST = new Sounds("ENTITY_FIREWORK_BLAST");
	public static Sounds ENTITY_FIREWORK_BLAST_FAR = new Sounds("ENTITY_FIREWORK_BLAST_FAR");
	public static Sounds ENTITY_FIREWORK_LARGE_BLAST = new Sounds("ENTITY_FIREWORK_LARGE_BLAST");
	public static Sounds ENTITY_FIREWORK_LARGE_BLAST_FAR = new Sounds("ENTITY_FIREWORK_LARGE_BLAST_FAR");
	public static Sounds ENTITY_FIREWORK_LAUNCH = new Sounds("ENTITY_FIREWORK_LAUNCH");
	public static Sounds ENTITY_FIREWORK_SHOOT = new Sounds("ENTITY_FIREWORK_SHOOT");
	public static Sounds ENTITY_FIREWORK_TWINKLE = new Sounds("ENTITY_FIREWORK_TWINKLE");
	public static Sounds ENTITY_FIREWORK_TWINKLE_FAR = new Sounds("ENTITY_FIREWORK_TWINKLE_FAR");
	public static Sounds ENTITY_GENERIC_BIG_FALL = new Sounds("ENTITY_GENERIC_BIG_FALL");
	public static Sounds ENTITY_GENERIC_BURN = new Sounds("ENTITY_GENERIC_BURN");
	public static Sounds ENTITY_GENERIC_DEATH = new Sounds("ENTITY_GENERIC_DEATH");
	public static Sounds ENTITY_GENERIC_DRINK = new Sounds("ENTITY_GENERIC_DRINK");
	public static Sounds ENTITY_GENERIC_EAT = new Sounds("ENTITY_GENERIC_EAT");
	public static Sounds ENTITY_GENERIC_EXPLODE = new Sounds("ENTITY_GENERIC_EXPLODE");
	public static Sounds ENTITY_GENERIC_EXTINGUISH_FIRE = new Sounds("ENTITY_GENERIC_EXTINGUISH_FIRE");
	public static Sounds ENTITY_GENERIC_HURT = new Sounds("ENTITY_GENERIC_HURT");
	public static Sounds ENTITY_GENERIC_SMALL_FALL = new Sounds("ENTITY_GENERIC_SMALL_FALL");
	public static Sounds ENTITY_GENERIC_SPLASH = new Sounds("ENTITY_GENERIC_SPLASH");
	public static Sounds ENTITY_GENERIC_SWIM = new Sounds("ENTITY_GENERIC_SWIM");
	public static Sounds ENTITY_GHAST_AMBIENT = new Sounds("ENTITY_GHAST_AMBIENT");
	public static Sounds ENTITY_GHAST_DEATH = new Sounds("ENTITY_GHAST_DEATH");
	public static Sounds ENTITY_GHAST_HURT = new Sounds("ENTITY_GHAST_HURT");
	public static Sounds ENTITY_GHAST_SCREAM = new Sounds("ENTITY_GHAST_SCREAM");
	public static Sounds ENTITY_GHAST_SHOOT = new Sounds("ENTITY_GHAST_SHOOT");
	public static Sounds ENTITY_GHAST_WARN = new Sounds("ENTITY_GHAST_WARN");
	public static Sounds ENTITY_GUARDIAN_AMBIENT = new Sounds("ENTITY_GUARDIAN_AMBIENT");
	public static Sounds ENTITY_GUARDIAN_AMBIENT_LAND = new Sounds("ENTITY_GUARDIAN_AMBIENT_LAND");
	public static Sounds ENTITY_GUARDIAN_ATTACK = new Sounds("ENTITY_GUARDIAN_ATTACK");
	public static Sounds ENTITY_GUARDIAN_DEATH = new Sounds("ENTITY_GUARDIAN_DEATH");
	public static Sounds ENTITY_GUARDIAN_DEATH_LAND = new Sounds("ENTITY_GUARDIAN_DEATH_LAND");
	public static Sounds ENTITY_GUARDIAN_FLOP = new Sounds("ENTITY_GUARDIAN_FLOP");
	public static Sounds ENTITY_GUARDIAN_HURT = new Sounds("ENTITY_GUARDIAN_HURT");
	public static Sounds ENTITY_GUARDIAN_HURT_LAND = new Sounds("ENTITY_GUARDIAN_HURT_LAND");
	public static Sounds ENTITY_HORSE_AMBIENT = new Sounds("ENTITY_HORSE_AMBIENT");
	public static Sounds ENTITY_HORSE_ANGRY = new Sounds("ENTITY_HORSE_ANGRY");
	public static Sounds ENTITY_HORSE_ARMOR = new Sounds("ENTITY_HORSE_ARMOR");
	public static Sounds ENTITY_HORSE_BREATHE = new Sounds("ENTITY_HORSE_BREATHE");
	public static Sounds ENTITY_HORSE_DEATH = new Sounds("ENTITY_HORSE_DEATH");
	public static Sounds ENTITY_HORSE_EAT = new Sounds("ENTITY_HORSE_EAT");
	public static Sounds ENTITY_HORSE_GALLOP = new Sounds("ENTITY_HORSE_GALLOP");
	public static Sounds ENTITY_HORSE_HURT = new Sounds("ENTITY_HORSE_HURT");
	public static Sounds ENTITY_HORSE_JUMP = new Sounds("ENTITY_HORSE_JUMP");
	public static Sounds ENTITY_HORSE_LAND = new Sounds("ENTITY_HORSE_LAND");
	public static Sounds ENTITY_HORSE_SADDLE = new Sounds("ENTITY_HORSE_SADDLE");
	public static Sounds ENTITY_HORSE_STEP = new Sounds("ENTITY_HORSE_STEP");
	public static Sounds ENTITY_HORSE_STEP_WOOD = new Sounds("ENTITY_HORSE_STEP_WOOD");
	public static Sounds ENTITY_HOSTILE_BIG_FALL = new Sounds("ENTITY_HOSTILE_BIG_FALL");
	public static Sounds ENTITY_HOSTILE_DEATH = new Sounds("ENTITY_HOSTILE_DEATH");
	public static Sounds ENTITY_HOSTILE_HURT = new Sounds("ENTITY_HOSTILE_HURT");
	public static Sounds ENTITY_HOSTILE_SMALL_FALL = new Sounds("ENTITY_HOSTILE_SMALL_FALL");
	public static Sounds ENTITY_HOSTILE_SPLASH = new Sounds("ENTITY_HOSTILE_SPLASH");
	public static Sounds ENTITY_HOSTILE_SWIM = new Sounds("ENTITY_HOSTILE_SWIM");
	public static Sounds ENTITY_HUSK_AMBIENT = new Sounds("ENTITY_HUSK_AMBIENT");
	public static Sounds ENTITY_HUSK_DEATH = new Sounds("ENTITY_HUSK_DEATH");
	public static Sounds ENTITY_HUSK_HURT = new Sounds("ENTITY_HUSK_HURT");
	public static Sounds ENTITY_HUSK_STEP = new Sounds("ENTITY_HUSK_STEP");
	public static Sounds ENTITY_ILLUSION_ILLAGER_AMBIENT = new Sounds("ENTITY_ILLUSION_ILLAGER_AMBIENT");
	public static Sounds ENTITY_ILLUSION_ILLAGER_CAST_SPELL = new Sounds("ENTITY_ILLUSION_ILLAGER_CAST_SPELL");
	public static Sounds ENTITY_ILLUSION_ILLAGER_DEATH = new Sounds("ENTITY_ILLUSION_ILLAGER_DEATH");
	public static Sounds ENTITY_ILLUSION_ILLAGER_HURT = new Sounds("ENTITY_ILLUSION_ILLAGER_HURT");
	public static Sounds ENTITY_ILLUSION_ILLAGER_MIRROR_MOVE = new Sounds("ENTITY_ILLUSION_ILLAGER_MIRROR_MOVE");
	public static Sounds ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS = new Sounds("ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS");
	public static Sounds ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR = new Sounds("ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR");
	public static Sounds ENTITY_IRONGOLEM_ATTACK = new Sounds("ENTITY_IRONGOLEM_ATTACK");
	public static Sounds ENTITY_IRONGOLEM_DEATH = new Sounds("ENTITY_IRONGOLEM_DEATH");
	public static Sounds ENTITY_IRONGOLEM_HURT = new Sounds("ENTITY_IRONGOLEM_HURT");
	public static Sounds ENTITY_IRONGOLEM_STEP = new Sounds("ENTITY_IRONGOLEM_STEP");
	public static Sounds ENTITY_ITEMFRAME_ADD_ITEM = new Sounds("ENTITY_ITEMFRAME_ADD_ITEM");
	public static Sounds ENTITY_ITEMFRAME_BREAK = new Sounds("ENTITY_ITEMFRAME_BREAK");
	public static Sounds ENTITY_ITEMFRAME_PLACE = new Sounds("ENTITY_ITEMFRAME_PLACE");
	public static Sounds ENTITY_ITEMFRAME_REMOVE_ITEM = new Sounds("ENTITY_ITEMFRAME_REMOVE_ITEM");
	public static Sounds ENTITY_ITEMFRAME_ROTATE_ITEM = new Sounds("ENTITY_ITEMFRAME_ROTATE_ITEM");
	public static Sounds ENTITY_ITEM_BREAK = new Sounds("ENTITY_ITEM_BREAK");
	public static Sounds ENTITY_ITEM_PICKUP = new Sounds("ENTITY_ITEM_PICKUP");
	public static Sounds ENTITY_LEASHKNOT_BREAK = new Sounds("ENTITY_LEASHKNOT_BREAK");
	public static Sounds ENTITY_LEASHKNOT_PLACE = new Sounds("ENTITY_LEASHKNOT_PLACE");
	public static Sounds ENTITY_LIGHTNING_IMPACT = new Sounds("ENTITY_LIGHTNING_IMPACT");
	public static Sounds ENTITY_LIGHTNING_THUNDER = new Sounds("ENTITY_LIGHTNING_THUNDER");
	public static Sounds ENTITY_LINGERINGPOTION_THROW = new Sounds("ENTITY_LINGERINGPOTION_THROW");
	public static Sounds ENTITY_LLAMA_AMBIENT = new Sounds("ENTITY_LLAMA_AMBIENT");
	public static Sounds ENTITY_LLAMA_ANGRY = new Sounds("ENTITY_LLAMA_ANGRY");
	public static Sounds ENTITY_LLAMA_CHEST = new Sounds("ENTITY_LLAMA_CHEST");
	public static Sounds ENTITY_LLAMA_DEATH = new Sounds("ENTITY_LLAMA_DEATH");
	public static Sounds ENTITY_LLAMA_EAT = new Sounds("ENTITY_LLAMA_EAT");
	public static Sounds ENTITY_LLAMA_HURT = new Sounds("ENTITY_LLAMA_HURT");
	public static Sounds ENTITY_LLAMA_SPIT = new Sounds("ENTITY_LLAMA_SPIT");
	public static Sounds ENTITY_LLAMA_STEP = new Sounds("ENTITY_LLAMA_STEP");
	public static Sounds ENTITY_LLAMA_SWAG = new Sounds("ENTITY_LLAMA_SWAG");
	public static Sounds ENTITY_MAGMACUBE_DEATH = new Sounds("ENTITY_MAGMACUBE_DEATH");
	public static Sounds ENTITY_MAGMACUBE_HURT = new Sounds("ENTITY_MAGMACUBE_HURT");
	public static Sounds ENTITY_MAGMACUBE_JUMP = new Sounds("ENTITY_MAGMACUBE_JUMP");
	public static Sounds ENTITY_MAGMACUBE_SQUISH = new Sounds("ENTITY_MAGMACUBE_SQUISH");
	public static Sounds ENTITY_MINECART_INSIDE = new Sounds("ENTITY_MINECART_INSIDE");
	public static Sounds ENTITY_MINECART_RIDING = new Sounds("ENTITY_MINECART_RIDING");
	public static Sounds ENTITY_MOOSHROOM_SHEAR = new Sounds("ENTITY_MOOSHROOM_SHEAR");
	public static Sounds ENTITY_MULE_AMBIENT = new Sounds("ENTITY_MULE_AMBIENT");
	public static Sounds ENTITY_MULE_CHEST = new Sounds("ENTITY_MULE_CHEST");
	public static Sounds ENTITY_MULE_DEATH = new Sounds("ENTITY_MULE_DEATH");
	public static Sounds ENTITY_MULE_HURT = new Sounds("ENTITY_MULE_HURT");
	public static Sounds ENTITY_PAINTING_BREAK = new Sounds("ENTITY_PAINTING_BREAK");
	public static Sounds ENTITY_PAINTING_PLACE = new Sounds("ENTITY_PAINTING_PLACE");
	public static Sounds ENTITY_PARROT_AMBIENT = new Sounds("ENTITY_PARROT_AMBIENT");
	public static Sounds ENTITY_PARROT_DEATH = new Sounds("ENTITY_PARROT_DEATH");
	public static Sounds ENTITY_PARROT_EAT = new Sounds("ENTITY_PARROT_EAT");
	public static Sounds ENTITY_PARROT_FLY = new Sounds("ENTITY_PARROT_FLY");
	public static Sounds ENTITY_PARROT_HURT = new Sounds("ENTITY_PARROT_HURT");
	public static Sounds ENTITY_PARROT_IMITATE_BLAZE = new Sounds("ENTITY_PARROT_IMITATE_BLAZE");
	public static Sounds ENTITY_PARROT_IMITATE_CREEPER = new Sounds("ENTITY_PARROT_IMITATE_CREEPER");
	public static Sounds ENTITY_PARROT_IMITATE_ELDER_GUARDIAN = new Sounds("ENTITY_PARROT_IMITATE_ELDER_GUARDIAN");
	public static Sounds ENTITY_PARROT_IMITATE_ENDERDRAGON = new Sounds("ENTITY_PARROT_IMITATE_ENDERDRAGON");
	public static Sounds ENTITY_PARROT_IMITATE_ENDERMAN = new Sounds("ENTITY_PARROT_IMITATE_ENDERMAN");
	public static Sounds ENTITY_PARROT_IMITATE_ENDERMITE = new Sounds("ENTITY_PARROT_IMITATE_ENDERMITE");
	public static Sounds ENTITY_PARROT_IMITATE_EVOCATION_ILLAGER = new Sounds("ENTITY_PARROT_IMITATE_EVOCATION_ILLAGER");
	public static Sounds ENTITY_PARROT_IMITATE_GHAST = new Sounds("ENTITY_PARROT_IMITATE_GHAST");
	public static Sounds ENTITY_PARROT_IMITATE_HUSK = new Sounds("ENTITY_PARROT_IMITATE_HUSK");
	public static Sounds ENTITY_PARROT_IMITATE_ILLUSION_ILLAGER = new Sounds("ENTITY_PARROT_IMITATE_ILLUSION_ILLAGER");
	public static Sounds ENTITY_PARROT_IMITATE_MAGMACUBE = new Sounds("ENTITY_PARROT_IMITATE_MAGMACUBE");
	public static Sounds ENTITY_PARROT_IMITATE_POLAR_BEAR = new Sounds("ENTITY_PARROT_IMITATE_POLAR_BEAR");
	public static Sounds ENTITY_PARROT_IMITATE_SHULKER = new Sounds("ENTITY_PARROT_IMITATE_SHULKER");
	public static Sounds ENTITY_PARROT_IMITATE_SILVERFISH = new Sounds("ENTITY_PARROT_IMITATE_SILVERFISH");
	public static Sounds ENTITY_PARROT_IMITATE_SKELETON = new Sounds("ENTITY_PARROT_IMITATE_SKELETON");
	public static Sounds ENTITY_PARROT_IMITATE_SLIME = new Sounds("ENTITY_PARROT_IMITATE_SLIME");
	public static Sounds ENTITY_PARROT_IMITATE_SPIDER = new Sounds("ENTITY_PARROT_IMITATE_SPIDER");
	public static Sounds ENTITY_PARROT_IMITATE_STRAY = new Sounds("ENTITY_PARROT_IMITATE_STRAY");
	public static Sounds ENTITY_PARROT_IMITATE_VEX = new Sounds("ENTITY_PARROT_IMITATE_VEX");
	public static Sounds ENTITY_PARROT_IMITATE_VINDICATION_ILLAGER = new Sounds("ENTITY_PARROT_IMITATE_VINDICATION_ILLAGER");
	public static Sounds ENTITY_PARROT_IMITATE_WITCH = new Sounds("ENTITY_PARROT_IMITATE_WITCH");
	public static Sounds ENTITY_PARROT_IMITATE_WITHER = new Sounds("ENTITY_PARROT_IMITATE_WITHER");
	public static Sounds ENTITY_PARROT_IMITATE_WITHER_SKELETON = new Sounds("ENTITY_PARROT_IMITATE_WITHER_SKELETON");
	public static Sounds ENTITY_PARROT_IMITATE_WOLF = new Sounds("ENTITY_PARROT_IMITATE_WOLF");
	public static Sounds ENTITY_PARROT_IMITATE_ZOMBIE = new Sounds("ENTITY_PARROT_IMITATE_ZOMBIE");
	public static Sounds ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN = new Sounds("ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN");
	public static Sounds ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER = new Sounds("ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER");
	public static Sounds ENTITY_PARROT_STEP = new Sounds("ENTITY_PARROT_STEP");
	public static Sounds ENTITY_PIG_AMBIENT = new Sounds("ENTITY_PIG_AMBIENT");
	public static Sounds ENTITY_PIG_DEATH = new Sounds("ENTITY_PIG_DEATH");
	public static Sounds ENTITY_PIG_HURT = new Sounds("ENTITY_PIG_HURT");
	public static Sounds ENTITY_PIG_SADDLE = new Sounds("ENTITY_PIG_SADDLE");
	public static Sounds ENTITY_PIG_STEP = new Sounds("ENTITY_PIG_STEP");
	public static Sounds ENTITY_PLAYER_ATTACK_CRIT = new Sounds("ENTITY_PLAYER_ATTACK_CRIT");
	public static Sounds ENTITY_PLAYER_ATTACK_KNOCKBACK = new Sounds("ENTITY_PLAYER_ATTACK_KNOCKBACK");
	public static Sounds ENTITY_PLAYER_ATTACK_NODAMAGE = new Sounds("ENTITY_PLAYER_ATTACK_NODAMAGE");
	public static Sounds ENTITY_PLAYER_ATTACK_STRONG = new Sounds("ENTITY_PLAYER_ATTACK_STRONG");
	public static Sounds ENTITY_PLAYER_ATTACK_SWEEP = new Sounds("ENTITY_PLAYER_ATTACK_SWEEP");
	public static Sounds ENTITY_PLAYER_ATTACK_WEAK = new Sounds("ENTITY_PLAYER_ATTACK_WEAK");
	public static Sounds ENTITY_PLAYER_BIG_FALL = new Sounds("ENTITY_PLAYER_BIG_FALL");
	public static Sounds ENTITY_PLAYER_BREATH = new Sounds("ENTITY_PLAYER_BREATH");
	public static Sounds ENTITY_PLAYER_BURP = new Sounds("ENTITY_PLAYER_BURP");
	public static Sounds ENTITY_PLAYER_DEATH = new Sounds("ENTITY_PLAYER_DEATH");
	public static Sounds ENTITY_PLAYER_HURT = new Sounds("ENTITY_PLAYER_HURT");
	public static Sounds ENTITY_PLAYER_HURT_DROWN = new Sounds("ENTITY_PLAYER_HURT_DROWN");
	public static Sounds ENTITY_PLAYER_HURT_ON_FIRE = new Sounds("ENTITY_PLAYER_HURT_ON_FIRE");
	public static Sounds ENTITY_PLAYER_LEVELUP = new Sounds("ENTITY_PLAYER_LEVELUP");
	public static Sounds ENTITY_PLAYER_SMALL_FALL = new Sounds("ENTITY_PLAYER_SMALL_FALL");
	public static Sounds ENTITY_PLAYER_SPLASH = new Sounds("ENTITY_PLAYER_SPLASH");
	public static Sounds ENTITY_PLAYER_SWIM = new Sounds("ENTITY_PLAYER_SWIM");
	public static Sounds ENTITY_POLAR_BEAR_AMBIENT = new Sounds("ENTITY_POLAR_BEAR_AMBIENT");
	public static Sounds ENTITY_POLAR_BEAR_BABY_AMBIENT = new Sounds("ENTITY_POLAR_BEAR_BABY_AMBIENT");
	public static Sounds ENTITY_POLAR_BEAR_DEATH = new Sounds("ENTITY_POLAR_BEAR_DEATH");
	public static Sounds ENTITY_POLAR_BEAR_HURT = new Sounds("ENTITY_POLAR_BEAR_HURT");
	public static Sounds ENTITY_POLAR_BEAR_STEP = new Sounds("ENTITY_POLAR_BEAR_STEP");
	public static Sounds ENTITY_POLAR_BEAR_WARNING = new Sounds("ENTITY_POLAR_BEAR_WARNING");
	public static Sounds ENTITY_RABBIT_AMBIENT = new Sounds("ENTITY_RABBIT_AMBIENT");
	public static Sounds ENTITY_RABBIT_ATTACK = new Sounds("ENTITY_RABBIT_ATTACK");
	public static Sounds ENTITY_RABBIT_DEATH = new Sounds("ENTITY_RABBIT_DEATH");
	public static Sounds ENTITY_RABBIT_HURT = new Sounds("ENTITY_RABBIT_HURT");
	public static Sounds ENTITY_RABBIT_JUMP = new Sounds("ENTITY_RABBIT_JUMP");
	public static Sounds ENTITY_SHEEP_AMBIENT = new Sounds("ENTITY_SHEEP_AMBIENT");
	public static Sounds ENTITY_SHEEP_DEATH = new Sounds("ENTITY_SHEEP_DEATH");
	public static Sounds ENTITY_SHEEP_HURT = new Sounds("ENTITY_SHEEP_HURT");
	public static Sounds ENTITY_SHEEP_SHEAR = new Sounds("ENTITY_SHEEP_SHEAR");
	public static Sounds ENTITY_SHEEP_STEP = new Sounds("ENTITY_SHEEP_STEP");
	public static Sounds ENTITY_SHULKER_AMBIENT = new Sounds("ENTITY_SHULKER_AMBIENT");
	public static Sounds ENTITY_SHULKER_BULLET_HIT = new Sounds("ENTITY_SHULKER_BULLET_HIT");
	public static Sounds ENTITY_SHULKER_BULLET_HURT = new Sounds("ENTITY_SHULKER_BULLET_HURT");
	public static Sounds ENTITY_SHULKER_CLOSE = new Sounds("ENTITY_SHULKER_CLOSE");
	public static Sounds ENTITY_SHULKER_DEATH = new Sounds("ENTITY_SHULKER_DEATH");
	public static Sounds ENTITY_SHULKER_HURT = new Sounds("ENTITY_SHULKER_HURT");
	public static Sounds ENTITY_SHULKER_HURT_CLOSED = new Sounds("ENTITY_SHULKER_HURT_CLOSED");
	public static Sounds ENTITY_SHULKER_OPEN = new Sounds("ENTITY_SHULKER_OPEN");
	public static Sounds ENTITY_SHULKER_SHOOT = new Sounds("ENTITY_SHULKER_SHOOT");
	public static Sounds ENTITY_SHULKER_TELEPORT = new Sounds("ENTITY_SHULKER_TELEPORT");
	public static Sounds ENTITY_SILVERFISH_AMBIENT = new Sounds("ENTITY_SILVERFISH_AMBIENT");
	public static Sounds ENTITY_SILVERFISH_DEATH = new Sounds("ENTITY_SILVERFISH_DEATH");
	public static Sounds ENTITY_SILVERFISH_HURT = new Sounds("ENTITY_SILVERFISH_HURT");
	public static Sounds ENTITY_SILVERFISH_STEP = new Sounds("ENTITY_SILVERFISH_STEP");
	public static Sounds ENTITY_SKELETON_AMBIENT = new Sounds("ENTITY_SKELETON_AMBIENT");
	public static Sounds ENTITY_SKELETON_DEATH = new Sounds("ENTITY_SKELETON_DEATH");
	public static Sounds ENTITY_SKELETON_HORSE_AMBIENT = new Sounds("ENTITY_SKELETON_HORSE_AMBIENT");
	public static Sounds ENTITY_SKELETON_HORSE_DEATH = new Sounds("ENTITY_SKELETON_HORSE_DEATH");
	public static Sounds ENTITY_SKELETON_HORSE_HURT = new Sounds("ENTITY_SKELETON_HORSE_HURT");
	public static Sounds ENTITY_SKELETON_HURT = new Sounds("ENTITY_SKELETON_HURT");
	public static Sounds ENTITY_SKELETON_SHOOT = new Sounds("ENTITY_SKELETON_SHOOT");
	public static Sounds ENTITY_SKELETON_STEP = new Sounds("ENTITY_SKELETON_STEP");
	public static Sounds ENTITY_SLIME_ATTACK = new Sounds("ENTITY_SLIME_ATTACK");
	public static Sounds ENTITY_SLIME_DEATH = new Sounds("ENTITY_SLIME_DEATH");
	public static Sounds ENTITY_SLIME_HURT = new Sounds("ENTITY_SLIME_HURT");
	public static Sounds ENTITY_SLIME_JUMP = new Sounds("ENTITY_SLIME_JUMP");
	public static Sounds ENTITY_SLIME_SQUISH = new Sounds("ENTITY_SLIME_SQUISH");
	public static Sounds ENTITY_SMALL_MAGMACUBE_DEATH = new Sounds("ENTITY_SMALL_MAGMACUBE_DEATH");
	public static Sounds ENTITY_SMALL_MAGMACUBE_HURT = new Sounds("ENTITY_SMALL_MAGMACUBE_HURT");
	public static Sounds ENTITY_SMALL_MAGMACUBE_SQUISH = new Sounds("ENTITY_SMALL_MAGMACUBE_SQUISH");
	public static Sounds ENTITY_SMALL_SLIME_DEATH = new Sounds("ENTITY_SMALL_SLIME_DEATH");
	public static Sounds ENTITY_SMALL_SLIME_HURT = new Sounds("ENTITY_SMALL_SLIME_HURT");
	public static Sounds ENTITY_SMALL_SLIME_JUMP = new Sounds("ENTITY_SMALL_SLIME_JUMP");
	public static Sounds ENTITY_SMALL_SLIME_SQUISH = new Sounds("ENTITY_SMALL_SLIME_SQUISH");
	public static Sounds ENTITY_SNOWBALL_THROW = new Sounds("ENTITY_SNOWBALL_THROW");
	public static Sounds ENTITY_SNOWMAN_AMBIENT = new Sounds("ENTITY_SNOWMAN_AMBIENT");
	public static Sounds ENTITY_SNOWMAN_DEATH = new Sounds("ENTITY_SNOWMAN_DEATH");
	public static Sounds ENTITY_SNOWMAN_HURT = new Sounds("ENTITY_SNOWMAN_HURT");
	public static Sounds ENTITY_SNOWMAN_SHOOT = new Sounds("ENTITY_SNOWMAN_SHOOT");
	public static Sounds ENTITY_SPIDER_AMBIENT = new Sounds("ENTITY_SPIDER_AMBIENT");
	public static Sounds ENTITY_SPIDER_DEATH = new Sounds("ENTITY_SPIDER_DEATH");
	public static Sounds ENTITY_SPIDER_HURT = new Sounds("ENTITY_SPIDER_HURT");
	public static Sounds ENTITY_SPIDER_STEP = new Sounds("ENTITY_SPIDER_STEP");
	public static Sounds ENTITY_SPLASH_POTION_BREAK = new Sounds("ENTITY_SPLASH_POTION_BREAK");
	public static Sounds ENTITY_SPLASH_POTION_THROW = new Sounds("ENTITY_SPLASH_POTION_THROW");
	public static Sounds ENTITY_SQUID_AMBIENT = new Sounds("ENTITY_SQUID_AMBIENT");
	public static Sounds ENTITY_SQUID_DEATH = new Sounds("ENTITY_SQUID_DEATH");
	public static Sounds ENTITY_SQUID_HURT = new Sounds("ENTITY_SQUID_HURT");
	public static Sounds ENTITY_STRAY_AMBIENT = new Sounds("ENTITY_STRAY_AMBIENT");
	public static Sounds ENTITY_STRAY_DEATH = new Sounds("ENTITY_STRAY_DEATH");
	public static Sounds ENTITY_STRAY_HURT = new Sounds("ENTITY_STRAY_HURT");
	public static Sounds ENTITY_STRAY_STEP = new Sounds("ENTITY_STRAY_STEP");
	public static Sounds ENTITY_TNT_PRIMED = new Sounds("ENTITY_TNT_PRIMED");
	public static Sounds ENTITY_VEX_AMBIENT = new Sounds("ENTITY_VEX_AMBIENT");
	public static Sounds ENTITY_VEX_CHARGE = new Sounds("ENTITY_VEX_CHARGE");
	public static Sounds ENTITY_VEX_DEATH = new Sounds("ENTITY_VEX_DEATH");
	public static Sounds ENTITY_VEX_HURT = new Sounds("ENTITY_VEX_HURT");
	public static Sounds ENTITY_VILLAGER_AMBIENT = new Sounds("ENTITY_VILLAGER_AMBIENT");
	public static Sounds ENTITY_VILLAGER_DEATH = new Sounds("ENTITY_VILLAGER_DEATH");
	public static Sounds ENTITY_VILLAGER_HURT = new Sounds("ENTITY_VILLAGER_HURT");
	public static Sounds ENTITY_VILLAGER_NO = new Sounds("ENTITY_VILLAGER_NO");
	public static Sounds ENTITY_VILLAGER_TRADING = new Sounds("ENTITY_VILLAGER_TRADING");
	public static Sounds ENTITY_VILLAGER_YES = new Sounds("ENTITY_VILLAGER_YES");
	public static Sounds ENTITY_VINDICATION_ILLAGER_AMBIENT = new Sounds("ENTITY_VINDICATION_ILLAGER_AMBIENT");
	public static Sounds ENTITY_VINDICATION_ILLAGER_DEATH = new Sounds("ENTITY_VINDICATION_ILLAGER_DEATH");
	public static Sounds ENTITY_VINDICATION_ILLAGER_HURT = new Sounds("ENTITY_VINDICATION_ILLAGER_HURT");
	public static Sounds ENTITY_WITCH_AMBIENT = new Sounds("ENTITY_WITCH_AMBIENT");
	public static Sounds ENTITY_WITCH_DEATH = new Sounds("ENTITY_WITCH_DEATH");
	public static Sounds ENTITY_WITCH_DRINK = new Sounds("ENTITY_WITCH_DRINK");
	public static Sounds ENTITY_WITCH_HURT = new Sounds("ENTITY_WITCH_HURT");
	public static Sounds ENTITY_WITCH_THROW = new Sounds("ENTITY_WITCH_THROW");
	public static Sounds ENTITY_WITHER_AMBIENT = new Sounds("ENTITY_WITHER_AMBIENT");
	public static Sounds ENTITY_WITHER_BREAK_BLOCK = new Sounds("ENTITY_WITHER_BREAK_BLOCK");
	public static Sounds ENTITY_WITHER_DEATH = new Sounds("ENTITY_WITHER_DEATH");
	public static Sounds ENTITY_WITHER_HURT = new Sounds("ENTITY_WITHER_HURT");
	public static Sounds ENTITY_WITHER_SHOOT = new Sounds("ENTITY_WITHER_SHOOT");
	public static Sounds ENTITY_WITHER_SKELETON_AMBIENT = new Sounds("ENTITY_WITHER_SKELETON_AMBIENT");
	public static Sounds ENTITY_WITHER_SKELETON_DEATH = new Sounds("ENTITY_WITHER_SKELETON_DEATH");
	public static Sounds ENTITY_WITHER_SKELETON_HURT = new Sounds("ENTITY_WITHER_SKELETON_HURT");
	public static Sounds ENTITY_WITHER_SKELETON_STEP = new Sounds("ENTITY_WITHER_SKELETON_STEP");
	public static Sounds ENTITY_WITHER_SPAWN = new Sounds("ENTITY_WITHER_SPAWN");
	public static Sounds ENTITY_WOLF_AMBIENT = new Sounds("ENTITY_WOLF_AMBIENT");
	public static Sounds ENTITY_WOLF_DEATH = new Sounds("ENTITY_WOLF_DEATH");
	public static Sounds ENTITY_WOLF_GROWL = new Sounds("ENTITY_WOLF_GROWL");
	public static Sounds ENTITY_WOLF_HOWL = new Sounds("ENTITY_WOLF_HOWL");
	public static Sounds ENTITY_WOLF_HURT = new Sounds("ENTITY_WOLF_HURT");
	public static Sounds ENTITY_WOLF_PANT = new Sounds("ENTITY_WOLF_PANT");
	public static Sounds ENTITY_WOLF_SHAKE = new Sounds("ENTITY_WOLF_SHAKE");
	public static Sounds ENTITY_WOLF_STEP = new Sounds("ENTITY_WOLF_STEP");
	public static Sounds ENTITY_WOLF_WHINE = new Sounds("ENTITY_WOLF_WHINE");
	public static Sounds ENTITY_ZOMBIE_AMBIENT = new Sounds("ENTITY_ZOMBIE_AMBIENT");
	public static Sounds ENTITY_ZOMBIE_ATTACK_DOOR_WOOD = new Sounds("ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
	public static Sounds ENTITY_ZOMBIE_ATTACK_IRON_DOOR = new Sounds("ENTITY_ZOMBIE_ATTACK_IRON_DOOR");
	public static Sounds ENTITY_ZOMBIE_BREAK_DOOR_WOOD = new Sounds("ENTITY_ZOMBIE_BREAK_DOOR_WOOD");
	public static Sounds ENTITY_ZOMBIE_DEATH = new Sounds("ENTITY_ZOMBIE_DEATH");
	public static Sounds ENTITY_ZOMBIE_HORSE_AMBIENT = new Sounds("ENTITY_ZOMBIE_HORSE_AMBIENT");
	public static Sounds ENTITY_ZOMBIE_HORSE_DEATH = new Sounds("ENTITY_ZOMBIE_HORSE_DEATH");
	public static Sounds ENTITY_ZOMBIE_HORSE_HURT = new Sounds("ENTITY_ZOMBIE_HORSE_HURT");
	public static Sounds ENTITY_ZOMBIE_HURT = new Sounds("ENTITY_ZOMBIE_HURT");
	public static Sounds ENTITY_ZOMBIE_INFECT = new Sounds("ENTITY_ZOMBIE_INFECT");
	public static Sounds ENTITY_ZOMBIE_PIG_AMBIENT = new Sounds("ENTITY_ZOMBIE_PIG_AMBIENT");
	public static Sounds ENTITY_ZOMBIE_PIG_ANGRY = new Sounds("ENTITY_ZOMBIE_PIG_ANGRY");
	public static Sounds ENTITY_ZOMBIE_PIG_DEATH = new Sounds("ENTITY_ZOMBIE_PIG_DEATH");
	public static Sounds ENTITY_ZOMBIE_PIG_HURT = new Sounds("ENTITY_ZOMBIE_PIG_HURT");
	public static Sounds ENTITY_ZOMBIE_STEP = new Sounds("ENTITY_ZOMBIE_STEP");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_AMBIENT = new Sounds("ENTITY_ZOMBIE_VILLAGER_AMBIENT");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_CONVERTED = new Sounds("ENTITY_ZOMBIE_VILLAGER_CONVERTED");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_CURE = new Sounds("ENTITY_ZOMBIE_VILLAGER_CURE");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_DEATH = new Sounds("ENTITY_ZOMBIE_VILLAGER_DEATH");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_HURT = new Sounds("ENTITY_ZOMBIE_VILLAGER_HURT");
	public static Sounds ENTITY_ZOMBIE_VILLAGER_STEP = new Sounds("ENTITY_ZOMBIE_VILLAGER_STEP");
	public static Sounds ITEM_ARMOR_EQUIP_CHAIN = new Sounds("ITEM_ARMOR_EQUIP_CHAIN");
	public static Sounds ITEM_ARMOR_EQUIP_DIAMOND = new Sounds("ITEM_ARMOR_EQUIP_DIAMOND");
	public static Sounds ITEM_ARMOR_EQUIP_ELYTRA = new Sounds("ITEM_ARMOR_EQUIP_ELYTRA");
	public static Sounds ITEM_ARMOR_EQUIP_GENERIC = new Sounds("ITEM_ARMOR_EQUIP_GENERIC");
	public static Sounds ITEM_ARMOR_EQUIP_GOLD = new Sounds("ITEM_ARMOR_EQUIP_GOLD");
	public static Sounds ITEM_ARMOR_EQUIP_IRON = new Sounds("ITEM_ARMOR_EQUIP_IRON");
	public static Sounds ITEM_ARMOR_EQUIP_LEATHER = new Sounds("ITEM_ARMOR_EQUIP_LEATHER");
	public static Sounds ITEM_BOTTLE_EMPTY = new Sounds("ITEM_BOTTLE_EMPTY");
	public static Sounds ITEM_BOTTLE_FILL = new Sounds("ITEM_BOTTLE_FILL");
	public static Sounds ITEM_BOTTLE_FILL_DRAGONBREATH = new Sounds("ITEM_BOTTLE_FILL_DRAGONBREATH");
	public static Sounds ITEM_BUCKET_EMPTY = new Sounds("ITEM_BUCKET_EMPTY");
	public static Sounds ITEM_BUCKET_EMPTY_LAVA = new Sounds("ITEM_BUCKET_EMPTY_LAVA");
	public static Sounds ITEM_BUCKET_FILL = new Sounds("ITEM_BUCKET_FILL");
	public static Sounds ITEM_BUCKET_FILL_LAVA = new Sounds("ITEM_BUCKET_FILL_LAVA");
	public static Sounds ITEM_CHORUS_FRUIT_TELEPORT = new Sounds("ITEM_CHORUS_FRUIT_TELEPORT");
	public static Sounds ITEM_ELYTRA_FLYING = new Sounds("ITEM_ELYTRA_FLYING");
	public static Sounds ITEM_FIRECHARGE_USE = new Sounds("ITEM_FIRECHARGE_USE");
	public static Sounds ITEM_FLINTANDSTEEL_USE = new Sounds("ITEM_FLINTANDSTEEL_USE");
	public static Sounds ITEM_HOE_TILL = new Sounds("ITEM_HOE_TILL");
	public static Sounds ITEM_SHIELD_BLOCK = new Sounds("ITEM_SHIELD_BLOCK");
	public static Sounds ITEM_SHIELD_BREAK = new Sounds("ITEM_SHIELD_BREAK");
	public static Sounds ITEM_SHOVEL_FLATTEN = new Sounds("ITEM_SHOVEL_FLATTEN");
	public static Sounds ITEM_TOTEM_USE = new Sounds("ITEM_TOTEM_USE");
	public static Sounds MUSIC_CREATIVE = new Sounds("MUSIC_CREATIVE");
	public static Sounds MUSIC_CREDITS = new Sounds("MUSIC_CREDITS");
	public static Sounds MUSIC_DRAGON = new Sounds("MUSIC_DRAGON");
	public static Sounds MUSIC_END = new Sounds("MUSIC_END");
	public static Sounds MUSIC_GAME = new Sounds("MUSIC_GAME");
	public static Sounds MUSIC_MENU = new Sounds("MUSIC_MENU");
	public static Sounds MUSIC_NETHER = new Sounds("MUSIC_NETHER");
	public static Sounds RECORD_11 = new Sounds("RECORD_11");
	public static Sounds RECORD_13 = new Sounds("RECORD_13");
	public static Sounds RECORD_BLOCKS = new Sounds("RECORD_BLOCKS");
	public static Sounds RECORD_CAT = new Sounds("RECORD_CAT");
	public static Sounds RECORD_CHIRP = new Sounds("RECORD_CHIRP");
	public static Sounds RECORD_FAR = new Sounds("RECORD_FAR");
	public static Sounds RECORD_MALL = new Sounds("RECORD_MALL");
	public static Sounds RECORD_MELLOHI = new Sounds("RECORD_MELLOHI");
	public static Sounds RECORD_STAL = new Sounds("RECORD_STAL");
	public static Sounds RECORD_STRAD = new Sounds("RECORD_STRAD");
	public static Sounds RECORD_WAIT = new Sounds("RECORD_WAIT");
	public static Sounds RECORD_WARD = new Sounds("RECORD_WARD");
	public static Sounds UI_BUTTON_CLICK = new Sounds("UI_BUTTON_CLICK");
	public static Sounds UI_TOAST_CHALLENGE_COMPLETE = new Sounds("UI_TOAST_CHALLENGE_COMPLETE");
	public static Sounds UI_TOAST_IN = new Sounds("UI_TOAST_IN");
	public static Sounds UI_TOAST_OUT = new Sounds("UI_TOAST_OUT");
	public static Sounds WEATHER_RAIN = new Sounds("WEATHER_RAIN");
	public static Sounds WEATHER_RAIN_ABOVE = new Sounds("WEATHER_RAIN_ABOVE");

	public static class Sounds {

		String soundName;
		Sound sound = null;

		public Sounds(String soundName) {
			this.soundName = soundName;

			for(Sound s : Sound.values()) {
				if(s.toString().equalsIgnoreCase(getName())) {
					sound = s;
				}
			}
		}

		public String getName() {
			return soundName;
		}
		
		public Sound getSound() {
			return sound;
		}
		
		public void playSound(Player p) {
			Sound s = getSound();
			if(s != null) {
				p.playSound(p.getLocation(), s, 10, 1);
			}
		}
		
		public void broadcastSound() {
			for(Player p : Bukkit.getOnlinePlayers()) {
				playSound(p);
			}
		}
		
	}
	
	public static Instruments PIANO = new Instruments("PIANO");
	public static Instruments BASS_DRUM = new Instruments("BASS_DRUM");
	public static Instruments SNARE_DRUM = new Instruments("SNARE_DRUM");
	public static Instruments STICKS = new Instruments("STICKS");
	public static Instruments BASS_GUITAR = new Instruments("BASS_GUITAR");
	public static Instruments FLUTE = new Instruments("FLUTE");
	public static Instruments BELL = new Instruments("BELL");
	public static Instruments GUITAR = new Instruments("GUITAR");
	public static Instruments CHIME = new Instruments("CHIME");
	public static Instruments XYLOPHONE = new Instruments("XYLOPHONE");
	
	public static class Instruments {

		String instrumentName;
		Instrument instrument = null;

		public Instruments(String instrumentName) {
			this.instrumentName = instrumentName;
			
			for(Instrument i : Instrument.values()) {
				if(i.toString().equalsIgnoreCase(getName())) {
					instrument = i;
				}
			}
		}

		public String getName() {
			return instrumentName;
		}
		
		public Instrument getInstrument() {
			return instrument;
		}
		
		public void playInstrument(Player p, Note note) {
			Instrument i = getInstrument();
			if(i != null) {
				p.playNote(p.getLocation(), i, note);
			}
		}
		
	}
	
}
