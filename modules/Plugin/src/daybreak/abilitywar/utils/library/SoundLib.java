package daybreak.abilitywar.utils.library;

import com.google.common.base.Enums;
import daybreak.abilitywar.utils.base.minecraft.compat.nms.Sounds;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 소리 라이브러리
 *
 * @author Daybreak 새벽
 * @version 2.0
 * @since 2019-02-19
 */
public class SoundLib {
	public static final SimpleSound BLOCK_ANVIL_BREAK = new SimpleSound("BLOCK_ANVIL_BREAK");
	public static final SimpleSound BLOCK_ANVIL_DESTROY = new SimpleSound("BLOCK_ANVIL_DESTROY");
	public static final SimpleSound BLOCK_ANVIL_FALL = new SimpleSound("BLOCK_ANVIL_FALL");
	public static final SimpleSound BLOCK_ANVIL_HIT = new SimpleSound("BLOCK_ANVIL_HIT");
	public static final SimpleSound BLOCK_ANVIL_LAND = new SimpleSound("BLOCK_ANVIL_LAND");
	public static final SimpleSound BLOCK_ANVIL_PLACE = new SimpleSound("BLOCK_ANVIL_PLACE");
	public static final SimpleSound BLOCK_ANVIL_STEP = new SimpleSound("BLOCK_ANVIL_STEP");
	public static final SimpleSound BLOCK_ANVIL_USE = new SimpleSound("BLOCK_ANVIL_USE");
	public static final SimpleSound BLOCK_BREWING_STAND_BREW = new SimpleSound("BLOCK_BREWING_STAND_BREW");
	public static final SimpleSound BLOCK_CHEST_CLOSE = new SimpleSound("BLOCK_CHEST_CLOSE");
	public static final SimpleSound BLOCK_CHEST_LOCKED = new SimpleSound("BLOCK_CHEST_LOCKED");
	public static final SimpleSound BLOCK_CHEST_OPEN = new SimpleSound("BLOCK_CHEST_OPEN");
	public static final SimpleSound BLOCK_CHORUS_FLOWER_DEATH = new SimpleSound("BLOCK_CHORUS_FLOWER_DEATH");
	public static final SimpleSound BLOCK_CHORUS_FLOWER_GROW = new SimpleSound("BLOCK_CHORUS_FLOWER_GROW");
	public static final SimpleSound BLOCK_COMPARATOR_CLICK = new SimpleSound("BLOCK_COMPARATOR_CLICK");
	public static final SimpleSound BLOCK_DISPENSER_DISPENSE = new SimpleSound("BLOCK_DISPENSER_DISPENSE");
	public static final SimpleSound BLOCK_DISPENSER_FAIL = new SimpleSound("BLOCK_DISPENSER_FAIL");
	public static final SimpleSound BLOCK_DISPENSER_LAUNCH = new SimpleSound("BLOCK_DISPENSER_LAUNCH");
	public static final SimpleSound BLOCK_ENCHANTMENT_TABLE_USE = new SimpleSound("BLOCK_ENCHANTMENT_TABLE_USE");
	public static final SimpleSound BLOCK_END_GATEWAY_SPAWN = new SimpleSound("BLOCK_END_GATEWAY_SPAWN");
	public static final SimpleSound BLOCK_END_PORTAL_FRAME_FILL = new SimpleSound("BLOCK_END_PORTAL_FRAME_FILL");
	public static final SimpleSound BLOCK_END_PORTAL_SPAWN = new SimpleSound("BLOCK_END_PORTAL_SPAWN");
	public static final SimpleSound BLOCK_ENDER_CHEST_CLOSE = new SimpleSound("BLOCK_ENDER_CHEST_CLOSE", "BLOCK_ENDERCHEST_CLOSE");
	public static final SimpleSound BLOCK_ENDER_CHEST_OPEN = new SimpleSound("BLOCK_ENDER_CHEST_OPEN", "BLOCK_ENDERCHEST_OPEN");
	public static final SimpleSound BLOCK_FENCE_GATE_CLOSE = new SimpleSound("BLOCK_FENCE_GATE_CLOSE");
	public static final SimpleSound BLOCK_FENCE_GATE_OPEN = new SimpleSound("BLOCK_FENCE_GATE_OPEN");
	public static final SimpleSound BLOCK_FIRE_AMBIENT = new SimpleSound("BLOCK_FIRE_AMBIENT");
	public static final SimpleSound BLOCK_FIRE_EXTINGUISH = new SimpleSound("BLOCK_FIRE_EXTINGUISH");
	public static final SimpleSound BLOCK_FURNACE_FIRE_CRACKLE = new SimpleSound("BLOCK_FURNACE_FIRE_CRACKLE");
	public static final SimpleSound BLOCK_GLASS_BREAK = new SimpleSound("BLOCK_GLASS_BREAK");
	public static final SimpleSound BLOCK_GLASS_FALL = new SimpleSound("BLOCK_GLASS_FALL");
	public static final SimpleSound BLOCK_GLASS_HIT = new SimpleSound("BLOCK_GLASS_HIT");
	public static final SimpleSound BLOCK_GLASS_PLACE = new SimpleSound("BLOCK_GLASS_PLACE");
	public static final SimpleSound BLOCK_GLASS_STEP = new SimpleSound("BLOCK_GLASS_STEP");
	public static final SimpleSound BLOCK_GRASS_BREAK = new SimpleSound("BLOCK_GRASS_BREAK");
	public static final SimpleSound BLOCK_GRASS_FALL = new SimpleSound("BLOCK_GRASS_FALL");
	public static final SimpleSound BLOCK_GRASS_HIT = new SimpleSound("BLOCK_GRASS_HIT");
	public static final SimpleSound BLOCK_GRASS_PLACE = new SimpleSound("BLOCK_GRASS_PLACE");
	public static final SimpleSound BLOCK_GRASS_STEP = new SimpleSound("BLOCK_GRASS_STEP");
	public static final SimpleSound BLOCK_GRAVEL_BREAK = new SimpleSound("BLOCK_GRAVEL_BREAK");
	public static final SimpleSound BLOCK_GRAVEL_FALL = new SimpleSound("BLOCK_GRAVEL_FALL");
	public static final SimpleSound BLOCK_GRAVEL_HIT = new SimpleSound("BLOCK_GRAVEL_HIT");
	public static final SimpleSound BLOCK_GRAVEL_PLACE = new SimpleSound("BLOCK_GRAVEL_PLACE");
	public static final SimpleSound BLOCK_GRAVEL_STEP = new SimpleSound("BLOCK_GRAVEL_STEP");
	public static final SimpleSound BLOCK_IRON_DOOR_CLOSE = new SimpleSound("BLOCK_IRON_DOOR_CLOSE");
	public static final SimpleSound BLOCK_IRON_DOOR_OPEN = new SimpleSound("BLOCK_IRON_DOOR_OPEN");
	public static final SimpleSound BLOCK_IRON_TRAPDOOR_CLOSE = new SimpleSound("BLOCK_IRON_TRAPDOOR_CLOSE");
	public static final SimpleSound BLOCK_IRON_TRAPDOOR_OPEN = new SimpleSound("BLOCK_IRON_TRAPDOOR_OPEN");
	public static final SimpleSound BLOCK_LADDER_BREAK = new SimpleSound("BLOCK_LADDER_BREAK");
	public static final SimpleSound BLOCK_LADDER_FALL = new SimpleSound("BLOCK_LADDER_FALL");
	public static final SimpleSound BLOCK_LADDER_HIT = new SimpleSound("BLOCK_LADDER_HIT");
	public static final SimpleSound BLOCK_LADDER_PLACE = new SimpleSound("BLOCK_LADDER_PLACE");
	public static final SimpleSound BLOCK_LADDER_STEP = new SimpleSound("BLOCK_LADDER_STEP");
	public static final SimpleSound BLOCK_LAVA_AMBIENT = new SimpleSound("BLOCK_LAVA_AMBIENT");
	public static final SimpleSound BLOCK_LAVA_EXTINGUISH = new SimpleSound("BLOCK_LAVA_EXTINGUISH");
	public static final SimpleSound BLOCK_LAVA_POP = new SimpleSound("BLOCK_LAVA_POP");
	public static final SimpleSound BLOCK_LEVER_CLICK = new SimpleSound("BLOCK_LEVER_CLICK");
	public static final SimpleSound BLOCK_LILY_PAD_PLACE = new SimpleSound("BLOCK_LILY_PAD_PLACE", "BLOCK_WATERLILY_PLACE");
	public static final SimpleSound BLOCK_METAL_BREAK = new SimpleSound("BLOCK_METAL_BREAK");
	public static final SimpleSound BLOCK_METAL_FALL = new SimpleSound("BLOCK_METAL_FALL");
	public static final SimpleSound BLOCK_METAL_HIT = new SimpleSound("BLOCK_METAL_HIT");
	public static final SimpleSound BLOCK_METAL_PLACE = new SimpleSound("BLOCK_METAL_PLACE");
	public static final SimpleSound BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF = new SimpleSound("BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF", "BLOCK_METAL_PRESSUREPLATE_CLICK_OFF");
	public static final SimpleSound BLOCK_METAL_PRESSURE_PLATE_CLICK_ON = new SimpleSound("BLOCK_METAL_PRESSURE_PLATE_CLICK_ON", "BLOCK_METAL_PRESSUREPLATE_CLICK_ON");
	public static final SimpleSound BLOCK_METAL_STEP = new SimpleSound("BLOCK_METAL_STEP");
	public static final SimpleSound BLOCK_NOTE_BLOCK_BASEDRUM = new SimpleSound("BLOCK_NOTE_BLOCK_BASEDRUM", "BLOCK_NOTE_BASEDRUM");
	public static final SimpleSound BLOCK_NOTE_BLOCK_BASS = new SimpleSound("BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS");
	public static final SimpleSound BLOCK_NOTE_BLOCK_BELL = new SimpleSound("BLOCK_NOTE_BLOCK_BELL", "BLOCK_NOTE_BELL");
	public static final SimpleSound BLOCK_NOTE_BLOCK_CHIME = new SimpleSound("BLOCK_NOTE_BLOCK_CHIME", "BLOCK_NOTE_CHIME");
	public static final SimpleSound BLOCK_NOTE_BLOCK_FLUTE = new SimpleSound("BLOCK_NOTE_BLOCK_FLUTE", "BLOCK_NOTE_FLUTE");
	public static final SimpleSound BLOCK_NOTE_BLOCK_GUITAR = new SimpleSound("BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_GUITAR");
	public static final SimpleSound BLOCK_NOTE_BLOCK_HARP = new SimpleSound("BLOCK_NOTE_BLOCK_HARP", "BLOCK_NOTE_HARP");
	public static final SimpleSound BLOCK_NOTE_BLOCK_HAT = new SimpleSound("BLOCK_NOTE_BLOCK_HAT", "BLOCK_NOTE_HAT");
	public static final SimpleSound BLOCK_NOTE_BLOCK_PLING = new SimpleSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING");
	public static final SimpleSound BLOCK_NOTE_BLOCK_SNARE = new SimpleSound("BLOCK_NOTE_BLOCK_SNARE", "BLOCK_NOTE_SNARE");
	public static final SimpleSound BLOCK_NOTE_BLOCK_XYLOPHONE = new SimpleSound("BLOCK_NOTE_BLOCK_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE");
	public static final SimpleSound BLOCK_PISTON_CONTRACT = new SimpleSound("BLOCK_PISTON_CONTRACT");
	public static final SimpleSound BLOCK_PISTON_EXTEND = new SimpleSound("BLOCK_PISTON_EXTEND");
	public static final SimpleSound BLOCK_PORTAL_AMBIENT = new SimpleSound("BLOCK_PORTAL_AMBIENT");
	public static final SimpleSound BLOCK_PORTAL_TRAVEL = new SimpleSound("BLOCK_PORTAL_TRAVEL");
	public static final SimpleSound BLOCK_PORTAL_TRIGGER = new SimpleSound("BLOCK_PORTAL_TRIGGER");
	public static final SimpleSound BLOCK_REDSTONE_TORCH_BURNOUT = new SimpleSound("BLOCK_REDSTONE_TORCH_BURNOUT");
	public static final SimpleSound BLOCK_SAND_BREAK = new SimpleSound("BLOCK_SAND_BREAK");
	public static final SimpleSound BLOCK_SAND_FALL = new SimpleSound("BLOCK_SAND_FALL");
	public static final SimpleSound BLOCK_SAND_HIT = new SimpleSound("BLOCK_SAND_HIT");
	public static final SimpleSound BLOCK_SAND_PLACE = new SimpleSound("BLOCK_SAND_PLACE");
	public static final SimpleSound BLOCK_SAND_STEP = new SimpleSound("BLOCK_SAND_STEP");
	public static final SimpleSound BLOCK_SHULKER_BOX_CLOSE = new SimpleSound("BLOCK_SHULKER_BOX_CLOSE");
	public static final SimpleSound BLOCK_SHULKER_BOX_OPEN = new SimpleSound("BLOCK_SHULKER_BOX_OPEN");
	public static final SimpleSound BLOCK_SLIME_BLOCK_BREAK = new SimpleSound("BLOCK_SLIME_BLOCK_BREAK", "BLOCK_SLIME_BREAK");
	public static final SimpleSound BLOCK_SLIME_BLOCK_FALL = new SimpleSound("BLOCK_SLIME_BLOCK_FALL", "BLOCK_SLIME_FALL");
	public static final SimpleSound BLOCK_SLIME_BLOCK_HIT = new SimpleSound("BLOCK_SLIME_BLOCK_HIT", "BLOCK_SLIME_HIT");
	public static final SimpleSound BLOCK_SLIME_BLOCK_PLACE = new SimpleSound("BLOCK_SLIME_BLOCK_PLACE", "BLOCK_SLIME_PLACE");
	public static final SimpleSound BLOCK_SLIME_BLOCK_STEP = new SimpleSound("BLOCK_SLIME_BLOCK_STEP", "BLOCK_SLIME_STEP");
	public static final SimpleSound BLOCK_SNOW_BREAK = new SimpleSound("BLOCK_SNOW_BREAK");
	public static final SimpleSound BLOCK_SNOW_FALL = new SimpleSound("BLOCK_SNOW_FALL");
	public static final SimpleSound BLOCK_SNOW_HIT = new SimpleSound("BLOCK_SNOW_HIT");
	public static final SimpleSound BLOCK_SNOW_PLACE = new SimpleSound("BLOCK_SNOW_PLACE");
	public static final SimpleSound BLOCK_SNOW_STEP = new SimpleSound("BLOCK_SNOW_STEP");
	public static final SimpleSound BLOCK_STONE_BREAK = new SimpleSound("BLOCK_STONE_BREAK");
	public static final SimpleSound BLOCK_STONE_BUTTON_CLICK_OFF = new SimpleSound("BLOCK_STONE_BUTTON_CLICK_OFF");
	public static final SimpleSound BLOCK_STONE_BUTTON_CLICK_ON = new SimpleSound("BLOCK_STONE_BUTTON_CLICK_ON");
	public static final SimpleSound BLOCK_STONE_FALL = new SimpleSound("BLOCK_STONE_FALL");
	public static final SimpleSound BLOCK_STONE_HIT = new SimpleSound("BLOCK_STONE_HIT");
	public static final SimpleSound BLOCK_STONE_PLACE = new SimpleSound("BLOCK_STONE_PLACE");
	public static final SimpleSound BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF = new SimpleSound("BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF", "BLOCK_STONE_PRESSUREPLATE_CLICK_OFF");
	public static final SimpleSound BLOCK_STONE_PRESSURE_PLATE_CLICK_ON = new SimpleSound("BLOCK_STONE_PRESSURE_PLATE_CLICK_ON", "BLOCK_STONE_PRESSUREPLATE_CLICK_ON");
	public static final SimpleSound BLOCK_STONE_STEP = new SimpleSound("BLOCK_STONE_STEP");
	public static final SimpleSound BLOCK_TRIPWIRE_ATTACH = new SimpleSound("BLOCK_TRIPWIRE_ATTACH");
	public static final SimpleSound BLOCK_TRIPWIRE_CLICK_OFF = new SimpleSound("BLOCK_TRIPWIRE_CLICK_OFF");
	public static final SimpleSound BLOCK_TRIPWIRE_CLICK_ON = new SimpleSound("BLOCK_TRIPWIRE_CLICK_ON");
	public static final SimpleSound BLOCK_TRIPWIRE_DETACH = new SimpleSound("BLOCK_TRIPWIRE_DETACH");
	public static final SimpleSound BLOCK_WATER_AMBIENT = new SimpleSound("BLOCK_WATER_AMBIENT");
	public static final SimpleSound BLOCK_WOOD_BREAK = new SimpleSound("BLOCK_WOOD_BREAK");
	public static final SimpleSound BLOCK_WOOD_FALL = new SimpleSound("BLOCK_WOOD_FALL");
	public static final SimpleSound BLOCK_WOOD_HIT = new SimpleSound("BLOCK_WOOD_HIT");
	public static final SimpleSound BLOCK_WOOD_PLACE = new SimpleSound("BLOCK_WOOD_PLACE");
	public static final SimpleSound BLOCK_WOOD_STEP = new SimpleSound("BLOCK_WOOD_STEP");
	public static final SimpleSound BLOCK_WOODEN_BUTTON_CLICK_OFF = new SimpleSound("BLOCK_WOODEN_BUTTON_CLICK_OFF", "BLOCK_WOOD_BUTTON_CLICK_OFF");
	public static final SimpleSound BLOCK_WOODEN_BUTTON_CLICK_ON = new SimpleSound("BLOCK_WOODEN_BUTTON_CLICK_ON", "BLOCK_WOOD_BUTTON_CLICK_ON");
	public static final SimpleSound BLOCK_WOODEN_DOOR_CLOSE = new SimpleSound("BLOCK_WOODEN_DOOR_CLOSE");
	public static final SimpleSound BLOCK_WOODEN_DOOR_OPEN = new SimpleSound("BLOCK_WOODEN_DOOR_OPEN");
	public static final SimpleSound BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF = new SimpleSound("BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF", "BLOCK_WOOD_PRESSUREPLATE_CLICK_OFF");
	public static final SimpleSound BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON = new SimpleSound("BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON", "BLOCK_WOOD_PRESSUREPLATE_CLICK_ON");
	public static final SimpleSound BLOCK_WOODEN_TRAPDOOR_CLOSE = new SimpleSound("BLOCK_WOODEN_TRAPDOOR_CLOSE");
	public static final SimpleSound BLOCK_WOODEN_TRAPDOOR_OPEN = new SimpleSound("BLOCK_WOODEN_TRAPDOOR_OPEN");
	public static final SimpleSound BLOCK_WOOL_BREAK = new SimpleSound("BLOCK_WOOL_BREAK", "BLOCK_CLOTH_BREAK");
	public static final SimpleSound BLOCK_WOOL_FALL = new SimpleSound("BLOCK_WOOL_FALL", "BLOCK_CLOTH_FALL");
	public static final SimpleSound BLOCK_WOOL_HIT = new SimpleSound("BLOCK_WOOL_HIT", "BLOCK_CLOTH_HIT");
	public static final SimpleSound BLOCK_WOOL_PLACE = new SimpleSound("BLOCK_WOOL_PLACE", "BLOCK_CLOTH_PLACE");
	public static final SimpleSound BLOCK_WOOL_STEP = new SimpleSound("BLOCK_WOOL_STEP", "BLOCK_CLOTH_STEP");
	public static final SimpleSound ENCHANT_THORNS_HIT = new SimpleSound("ENCHANT_THORNS_HIT");
	public static final SimpleSound ENTITY_ARMOR_STAND_BREAK = new SimpleSound("ENTITY_ARMOR_STAND_BREAK", "ENTITY_ARMORSTAND_BREAK");
	public static final SimpleSound ENTITY_ARMOR_STAND_FALL = new SimpleSound("ENTITY_ARMOR_STAND_FALL", "ENTITY_ARMORSTAND_FALL");
	public static final SimpleSound ENTITY_ARMOR_STAND_HIT = new SimpleSound("ENTITY_ARMOR_STAND_HIT", "ENTITY_ARMORSTAND_HIT");
	public static final SimpleSound ENTITY_ARMOR_STAND_PLACE = new SimpleSound("ENTITY_ARMOR_STAND_PLACE", "ENTITY_ARMORSTAND_PLACE");
	public static final SimpleSound ENTITY_ARROW_HIT = new SimpleSound("ENTITY_ARROW_HIT");
	public static final SimpleSound ENTITY_ARROW_HIT_PLAYER = new SimpleSound("ENTITY_ARROW_HIT_PLAYER");
	public static final SimpleSound ENTITY_ARROW_SHOOT = new SimpleSound("ENTITY_ARROW_SHOOT");
	public static final SimpleSound ENTITY_BAT_AMBIENT = new SimpleSound("ENTITY_BAT_AMBIENT");
	public static final SimpleSound ENTITY_BAT_DEATH = new SimpleSound("ENTITY_BAT_DEATH");
	public static final SimpleSound ENTITY_BAT_HURT = new SimpleSound("ENTITY_BAT_HURT");
	public static final SimpleSound ENTITY_BAT_LOOP = new SimpleSound("ENTITY_BAT_LOOP");
	public static final SimpleSound ENTITY_BAT_TAKEOFF = new SimpleSound("ENTITY_BAT_TAKEOFF");
	public static final SimpleSound ENTITY_BLAZE_AMBIENT = new SimpleSound("ENTITY_BLAZE_AMBIENT");
	public static final SimpleSound ENTITY_BLAZE_BURN = new SimpleSound("ENTITY_BLAZE_BURN");
	public static final SimpleSound ENTITY_BLAZE_DEATH = new SimpleSound("ENTITY_BLAZE_DEATH");
	public static final SimpleSound ENTITY_BLAZE_HURT = new SimpleSound("ENTITY_BLAZE_HURT");
	public static final SimpleSound ENTITY_BLAZE_SHOOT = new SimpleSound("ENTITY_BLAZE_SHOOT");
	public static final SimpleSound ENTITY_BOAT_PADDLE_LAND = new SimpleSound("ENTITY_BOAT_PADDLE_LAND");
	public static final SimpleSound ENTITY_BOAT_PADDLE_WATER = new SimpleSound("ENTITY_BOAT_PADDLE_WATER");
	public static final SimpleSound ENTITY_CAT_AMBIENT = new SimpleSound("ENTITY_CAT_AMBIENT");
	public static final SimpleSound ENTITY_CAT_DEATH = new SimpleSound("ENTITY_CAT_DEATH");
	public static final SimpleSound ENTITY_CAT_HISS = new SimpleSound("ENTITY_CAT_HISS");
	public static final SimpleSound ENTITY_CAT_HURT = new SimpleSound("ENTITY_CAT_HURT");
	public static final SimpleSound ENTITY_CAT_PURR = new SimpleSound("ENTITY_CAT_PURR");
	public static final SimpleSound ENTITY_CAT_PURREOW = new SimpleSound("ENTITY_CAT_PURREOW");
	public static final SimpleSound ENTITY_CHICKEN_AMBIENT = new SimpleSound("ENTITY_CHICKEN_AMBIENT");
	public static final SimpleSound ENTITY_CHICKEN_DEATH = new SimpleSound("ENTITY_CHICKEN_DEATH");
	public static final SimpleSound ENTITY_CHICKEN_EGG = new SimpleSound("ENTITY_CHICKEN_EGG");
	public static final SimpleSound ENTITY_CHICKEN_HURT = new SimpleSound("ENTITY_CHICKEN_HURT");
	public static final SimpleSound ENTITY_CHICKEN_STEP = new SimpleSound("ENTITY_CHICKEN_STEP");
	public static final SimpleSound ENTITY_COW_AMBIENT = new SimpleSound("ENTITY_COW_AMBIENT");
	public static final SimpleSound ENTITY_COW_DEATH = new SimpleSound("ENTITY_COW_DEATH");
	public static final SimpleSound ENTITY_COW_HURT = new SimpleSound("ENTITY_COW_HURT");
	public static final SimpleSound ENTITY_COW_MILK = new SimpleSound("ENTITY_COW_MILK");
	public static final SimpleSound ENTITY_COW_STEP = new SimpleSound("ENTITY_COW_STEP");
	public static final SimpleSound ENTITY_CREEPER_DEATH = new SimpleSound("ENTITY_CREEPER_DEATH");
	public static final SimpleSound ENTITY_CREEPER_HURT = new SimpleSound("ENTITY_CREEPER_HURT");
	public static final SimpleSound ENTITY_CREEPER_PRIMED = new SimpleSound("ENTITY_CREEPER_PRIMED");
	public static final SimpleSound ENTITY_DONKEY_AMBIENT = new SimpleSound("ENTITY_DONKEY_AMBIENT");
	public static final SimpleSound ENTITY_DONKEY_ANGRY = new SimpleSound("ENTITY_DONKEY_ANGRY");
	public static final SimpleSound ENTITY_DONKEY_CHEST = new SimpleSound("ENTITY_DONKEY_CHEST");
	public static final SimpleSound ENTITY_DONKEY_DEATH = new SimpleSound("ENTITY_DONKEY_DEATH");
	public static final SimpleSound ENTITY_DONKEY_HURT = new SimpleSound("ENTITY_DONKEY_HURT");
	public static final SimpleSound ENTITY_DRAGON_FIREBALL_EXPLODE = new SimpleSound("ENTITY_DRAGON_FIREBALL_EXPLODE", "ENTITY_ENDERDRAGON_FIREBALL_EXPLODE");
	public static final SimpleSound ENTITY_EGG_THROW = new SimpleSound("ENTITY_EGG_THROW");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_AMBIENT = new SimpleSound("ENTITY_ELDER_GUARDIAN_AMBIENT");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_AMBIENT_LAND = new SimpleSound("ENTITY_ELDER_GUARDIAN_AMBIENT_LAND");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_CURSE = new SimpleSound("ENTITY_ELDER_GUARDIAN_CURSE");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_DEATH = new SimpleSound("ENTITY_ELDER_GUARDIAN_DEATH");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_DEATH_LAND = new SimpleSound("ENTITY_ELDER_GUARDIAN_DEATH_LAND");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_FLOP = new SimpleSound("ENTITY_ELDER_GUARDIAN_FLOP");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_HURT = new SimpleSound("ENTITY_ELDER_GUARDIAN_HURT");
	public static final SimpleSound ENTITY_ELDER_GUARDIAN_HURT_LAND = new SimpleSound("ENTITY_ELDER_GUARDIAN_HURT_LAND");
	public static final SimpleSound ENTITY_ENDER_DRAGON_AMBIENT = new SimpleSound("ENTITY_ENDER_DRAGON_AMBIENT", "ENTITY_ENDERDRAGON_AMBIENT");
	public static final SimpleSound ENTITY_ENDER_DRAGON_DEATH = new SimpleSound("ENTITY_ENDER_DRAGON_DEATH", "ENTITY_ENDERDRAGON_DEATH");
	public static final SimpleSound ENTITY_ENDER_DRAGON_FLAP = new SimpleSound("ENTITY_ENDER_DRAGON_FLAP", "ENTITY_ENDERDRAGON_FLAP");
	public static final SimpleSound ENTITY_ENDER_DRAGON_GROWL = new SimpleSound("ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL");
	public static final SimpleSound ENTITY_ENDER_DRAGON_HURT = new SimpleSound("ENTITY_ENDER_DRAGON_HURT", "ENTITY_ENDERDRAGON_HURT");
	public static final SimpleSound ENTITY_ENDER_DRAGON_SHOOT = new SimpleSound("ENTITY_ENDER_DRAGON_SHOOT", "ENTITY_ENDERDRAGON_SHOOT");
	public static final SimpleSound ENTITY_ENDER_EYE_DEATH = new SimpleSound("ENTITY_ENDER_EYE_DEATH", "ENTITY_ENDEREYE_DEATH");
	public static final SimpleSound ENTITY_ENDER_EYE_LAUNCH = new SimpleSound("ENTITY_ENDER_EYE_LAUNCH", "ENTITY_ENDEREYE_LAUNCH");
	public static final SimpleSound ENTITY_ENDER_PEARL_THROW = new SimpleSound("ENTITY_ENDER_PEARL_THROW", "ENTITY_ENDERPEARL_THROW");
	public static final SimpleSound ENTITY_ENDERMAN_AMBIENT = new SimpleSound("ENTITY_ENDERMAN_AMBIENT", "ENTITY_ENDERMEN_AMBIENT");
	public static final SimpleSound ENTITY_ENDERMAN_DEATH = new SimpleSound("ENTITY_ENDERMAN_DEATH", "ENTITY_ENDERMEN_DEATH");
	public static final SimpleSound ENTITY_ENDERMAN_HURT = new SimpleSound("ENTITY_ENDERMAN_HURT", "ENTITY_ENDERMEN_HURT");
	public static final SimpleSound ENTITY_ENDERMAN_SCREAM = new SimpleSound("ENTITY_ENDERMAN_SCREAM", "ENTITY_ENDERMEN_SCREAM");
	public static final SimpleSound ENTITY_ENDERMAN_STARE = new SimpleSound("ENTITY_ENDERMAN_STARE", "ENTITY_ENDERMEN_STARE");
	public static final SimpleSound ENTITY_ENDERMAN_TELEPORT = new SimpleSound("ENTITY_ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT");
	public static final SimpleSound ENTITY_ENDERMITE_AMBIENT = new SimpleSound("ENTITY_ENDERMITE_AMBIENT");
	public static final SimpleSound ENTITY_ENDERMITE_DEATH = new SimpleSound("ENTITY_ENDERMITE_DEATH");
	public static final SimpleSound ENTITY_ENDERMITE_HURT = new SimpleSound("ENTITY_ENDERMITE_HURT");
	public static final SimpleSound ENTITY_ENDERMITE_STEP = new SimpleSound("ENTITY_ENDERMITE_STEP");
	public static final SimpleSound ENTITY_EVOKER_AMBIENT = new SimpleSound("ENTITY_EVOKER_AMBIENT", "ENTITY_EVOCATION_ILLAGER_AMBIENT");
	public static final SimpleSound ENTITY_EVOKER_CAST_SPELL = new SimpleSound("ENTITY_EVOKER_CAST_SPELL", "ENTITY_EVOCATION_ILLAGER_CAST_SPELL");
	public static final SimpleSound ENTITY_EVOKER_DEATH = new SimpleSound("ENTITY_EVOKER_DEATH", "ENTITY_EVOCATION_ILLAGER_DEATH");
	public static final SimpleSound ENTITY_EVOKER_FANGS_ATTACK = new SimpleSound("ENTITY_EVOKER_FANGS_ATTACK", "ENTITY_EVOCATION_FANGS_ATTACK");
	public static final SimpleSound ENTITY_EVOKER_HURT = new SimpleSound("ENTITY_EVOKER_HURT", "ENTITY_EVOCATION_ILLAGER_HURT");
	public static final SimpleSound ENTITY_EVOKER_PREPARE_ATTACK = new SimpleSound("ENTITY_EVOKER_PREPARE_ATTACK", "ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK");
	public static final SimpleSound ENTITY_EVOKER_PREPARE_SUMMON = new SimpleSound("ENTITY_EVOKER_PREPARE_SUMMON", "ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON");
	public static final SimpleSound ENTITY_EVOKER_PREPARE_WOLOLO = new SimpleSound("ENTITY_EVOKER_PREPARE_WOLOLO", "ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO");
	public static final SimpleSound ENTITY_EXPERIENCE_BOTTLE_THROW = new SimpleSound("ENTITY_EXPERIENCE_BOTTLE_THROW");
	public static final SimpleSound ENTITY_EXPERIENCE_ORB_PICKUP = new SimpleSound("ENTITY_EXPERIENCE_ORB_PICKUP");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_BLAST = new SimpleSound("ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_BLAST");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_BLAST_FAR = new SimpleSound("ENTITY_FIREWORK_ROCKET_BLAST_FAR", "ENTITY_FIREWORK_BLAST_FAR");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_LARGE_BLAST = new SimpleSound("ENTITY_FIREWORK_ROCKET_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR = new SimpleSound("ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR", "ENTITY_FIREWORK_LARGE_BLAST_FAR");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_LAUNCH = new SimpleSound("ENTITY_FIREWORK_ROCKET_LAUNCH", "ENTITY_FIREWORK_LAUNCH");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_SHOOT = new SimpleSound("ENTITY_FIREWORK_ROCKET_SHOOT", "ENTITY_FIREWORK_SHOOT");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_TWINKLE = new SimpleSound("ENTITY_FIREWORK_ROCKET_TWINKLE", "ENTITY_FIREWORK_TWINKLE");
	public static final SimpleSound ENTITY_FIREWORK_ROCKET_TWINKLE_FAR = new SimpleSound("ENTITY_FIREWORK_ROCKET_TWINKLE_FAR", "ENTITY_FIREWORK_TWINKLE_FAR");
	public static final SimpleSound ENTITY_FISHING_BOBBER_RETRIEVE = new SimpleSound("ENTITY_FISHING_BOBBER_RETRIEVE", "ENTITY_BOBBER_RETRIEVE");
	public static final SimpleSound ENTITY_FISHING_BOBBER_SPLASH = new SimpleSound("ENTITY_FISHING_BOBBER_SPLASH", "ENTITY_BOBBER_SPLASH");
	public static final SimpleSound ENTITY_FISHING_BOBBER_THROW = new SimpleSound("ENTITY_FISHING_BOBBER_THROW", "ENTITY_BOBBER_THROW");
	public static final SimpleSound ENTITY_GENERIC_BIG_FALL = new SimpleSound("ENTITY_GENERIC_BIG_FALL");
	public static final SimpleSound ENTITY_GENERIC_BURN = new SimpleSound("ENTITY_GENERIC_BURN");
	public static final SimpleSound ENTITY_GENERIC_DEATH = new SimpleSound("ENTITY_GENERIC_DEATH");
	public static final SimpleSound ENTITY_GENERIC_DRINK = new SimpleSound("ENTITY_GENERIC_DRINK");
	public static final SimpleSound ENTITY_GENERIC_EAT = new SimpleSound("ENTITY_GENERIC_EAT");
	public static final SimpleSound ENTITY_GENERIC_EXPLODE = new SimpleSound("ENTITY_GENERIC_EXPLODE");
	public static final SimpleSound ENTITY_GENERIC_EXTINGUISH_FIRE = new SimpleSound("ENTITY_GENERIC_EXTINGUISH_FIRE");
	public static final SimpleSound ENTITY_GENERIC_HURT = new SimpleSound("ENTITY_GENERIC_HURT");
	public static final SimpleSound ENTITY_GENERIC_SMALL_FALL = new SimpleSound("ENTITY_GENERIC_SMALL_FALL");
	public static final SimpleSound ENTITY_GENERIC_SPLASH = new SimpleSound("ENTITY_GENERIC_SPLASH");
	public static final SimpleSound ENTITY_GENERIC_SWIM = new SimpleSound("ENTITY_GENERIC_SWIM");
	public static final SimpleSound ENTITY_GHAST_AMBIENT = new SimpleSound("ENTITY_GHAST_AMBIENT");
	public static final SimpleSound ENTITY_GHAST_DEATH = new SimpleSound("ENTITY_GHAST_DEATH");
	public static final SimpleSound ENTITY_GHAST_HURT = new SimpleSound("ENTITY_GHAST_HURT");
	public static final SimpleSound ENTITY_GHAST_SCREAM = new SimpleSound("ENTITY_GHAST_SCREAM");
	public static final SimpleSound ENTITY_GHAST_SHOOT = new SimpleSound("ENTITY_GHAST_SHOOT");
	public static final SimpleSound ENTITY_GHAST_WARN = new SimpleSound("ENTITY_GHAST_WARN");
	public static final SimpleSound ENTITY_GUARDIAN_AMBIENT = new SimpleSound("ENTITY_GUARDIAN_AMBIENT");
	public static final SimpleSound ENTITY_GUARDIAN_AMBIENT_LAND = new SimpleSound("ENTITY_GUARDIAN_AMBIENT_LAND");
	public static final SimpleSound ENTITY_GUARDIAN_ATTACK = new SimpleSound("ENTITY_GUARDIAN_ATTACK");
	public static final SimpleSound ENTITY_GUARDIAN_DEATH = new SimpleSound("ENTITY_GUARDIAN_DEATH");
	public static final SimpleSound ENTITY_GUARDIAN_DEATH_LAND = new SimpleSound("ENTITY_GUARDIAN_DEATH_LAND");
	public static final SimpleSound ENTITY_GUARDIAN_FLOP = new SimpleSound("ENTITY_GUARDIAN_FLOP");
	public static final SimpleSound ENTITY_GUARDIAN_HURT = new SimpleSound("ENTITY_GUARDIAN_HURT");
	public static final SimpleSound ENTITY_GUARDIAN_HURT_LAND = new SimpleSound("ENTITY_GUARDIAN_HURT_LAND");
	public static final SimpleSound ENTITY_HORSE_AMBIENT = new SimpleSound("ENTITY_HORSE_AMBIENT");
	public static final SimpleSound ENTITY_HORSE_ANGRY = new SimpleSound("ENTITY_HORSE_ANGRY");
	public static final SimpleSound ENTITY_HORSE_ARMOR = new SimpleSound("ENTITY_HORSE_ARMOR");
	public static final SimpleSound ENTITY_HORSE_BREATHE = new SimpleSound("ENTITY_HORSE_BREATHE");
	public static final SimpleSound ENTITY_HORSE_DEATH = new SimpleSound("ENTITY_HORSE_DEATH");
	public static final SimpleSound ENTITY_HORSE_EAT = new SimpleSound("ENTITY_HORSE_EAT");
	public static final SimpleSound ENTITY_HORSE_GALLOP = new SimpleSound("ENTITY_HORSE_GALLOP");
	public static final SimpleSound ENTITY_HORSE_HURT = new SimpleSound("ENTITY_HORSE_HURT");
	public static final SimpleSound ENTITY_HORSE_JUMP = new SimpleSound("ENTITY_HORSE_JUMP");
	public static final SimpleSound ENTITY_HORSE_LAND = new SimpleSound("ENTITY_HORSE_LAND");
	public static final SimpleSound ENTITY_HORSE_SADDLE = new SimpleSound("ENTITY_HORSE_SADDLE");
	public static final SimpleSound ENTITY_HORSE_STEP = new SimpleSound("ENTITY_HORSE_STEP");
	public static final SimpleSound ENTITY_HORSE_STEP_WOOD = new SimpleSound("ENTITY_HORSE_STEP_WOOD");
	public static final SimpleSound ENTITY_HOSTILE_BIG_FALL = new SimpleSound("ENTITY_HOSTILE_BIG_FALL");
	public static final SimpleSound ENTITY_HOSTILE_DEATH = new SimpleSound("ENTITY_HOSTILE_DEATH");
	public static final SimpleSound ENTITY_HOSTILE_HURT = new SimpleSound("ENTITY_HOSTILE_HURT");
	public static final SimpleSound ENTITY_HOSTILE_SMALL_FALL = new SimpleSound("ENTITY_HOSTILE_SMALL_FALL");
	public static final SimpleSound ENTITY_HOSTILE_SPLASH = new SimpleSound("ENTITY_HOSTILE_SPLASH");
	public static final SimpleSound ENTITY_HOSTILE_SWIM = new SimpleSound("ENTITY_HOSTILE_SWIM");
	public static final SimpleSound ENTITY_HUSK_AMBIENT = new SimpleSound("ENTITY_HUSK_AMBIENT");
	public static final SimpleSound ENTITY_HUSK_DEATH = new SimpleSound("ENTITY_HUSK_DEATH");
	public static final SimpleSound ENTITY_HUSK_HURT = new SimpleSound("ENTITY_HUSK_HURT");
	public static final SimpleSound ENTITY_HUSK_STEP = new SimpleSound("ENTITY_HUSK_STEP");
	public static final SimpleSound ENTITY_ILLUSIONER_AMBIENT = new SimpleSound("ENTITY_ILLUSIONER_AMBIENT", "ENTITY_ILLUSION_ILLAGER_AMBIENT");
	public static final SimpleSound ENTITY_ILLUSIONER_CAST_SPELL = new SimpleSound("ENTITY_ILLUSIONER_CAST_SPELL", "ENTITY_ILLUSION_ILLAGER_CAST_SPELL");
	public static final SimpleSound ENTITY_ILLUSIONER_DEATH = new SimpleSound("ENTITY_ILLUSIONER_DEATH", "ENTITY_ILLUSION_ILLAGER_DEATH");
	public static final SimpleSound ENTITY_ILLUSIONER_HURT = new SimpleSound("ENTITY_ILLUSIONER_HURT", "ENTITY_ILLUSION_ILLAGER_HURT");
	public static final SimpleSound ENTITY_ILLUSIONER_MIRROR_MOVE = new SimpleSound("ENTITY_ILLUSIONER_MIRROR_MOVE", "ENTITY_ILLUSION_ILLAGER_MIRROR_MOVE");
	public static final SimpleSound ENTITY_ILLUSIONER_PREPARE_BLINDNESS = new SimpleSound("ENTITY_ILLUSIONER_PREPARE_BLINDNESS", "ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS");
	public static final SimpleSound ENTITY_ILLUSIONER_PREPARE_MIRROR = new SimpleSound("ENTITY_ILLUSIONER_PREPARE_MIRROR", "ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR");
	public static final SimpleSound ENTITY_IRON_GOLEM_ATTACK = new SimpleSound("ENTITY_IRON_GOLEM_ATTACK", "ENTITY_IRONGOLEM_ATTACK");
	public static final SimpleSound ENTITY_IRON_GOLEM_DEATH = new SimpleSound("ENTITY_IRON_GOLEM_DEATH", "ENTITY_IRONGOLEM_DEATH");
	public static final SimpleSound ENTITY_IRON_GOLEM_HURT = new SimpleSound("ENTITY_IRON_GOLEM_HURT", "ENTITY_IRONGOLEM_HURT");
	public static final SimpleSound ENTITY_IRON_GOLEM_STEP = new SimpleSound("ENTITY_IRON_GOLEM_STEP", "ENTITY_IRONGOLEM_STEP");
	public static final SimpleSound ENTITY_ITEM_BREAK = new SimpleSound("ENTITY_ITEM_BREAK");
	public static final SimpleSound ENTITY_ITEM_FRAME_ADD_ITEM = new SimpleSound("ENTITY_ITEM_FRAME_ADD_ITEM", "ENTITY_ITEMFRAME_ADD_ITEM");
	public static final SimpleSound ENTITY_ITEM_FRAME_BREAK = new SimpleSound("ENTITY_ITEM_FRAME_BREAK", "ENTITY_ITEMFRAME_BREAK");
	public static final SimpleSound ENTITY_ITEM_FRAME_PLACE = new SimpleSound("ENTITY_ITEM_FRAME_PLACE", "ENTITY_ITEMFRAME_PLACE");
	public static final SimpleSound ENTITY_ITEM_FRAME_REMOVE_ITEM = new SimpleSound("ENTITY_ITEM_FRAME_REMOVE_ITEM", "ENTITY_ITEMFRAME_REMOVE_ITEM");
	public static final SimpleSound ENTITY_ITEM_FRAME_ROTATE_ITEM = new SimpleSound("ENTITY_ITEM_FRAME_ROTATE_ITEM", "ENTITY_ITEMFRAME_ROTATE_ITEM");
	public static final SimpleSound ENTITY_ITEM_PICKUP = new SimpleSound("ENTITY_ITEM_PICKUP");
	public static final SimpleSound ENTITY_LEASH_KNOT_BREAK = new SimpleSound("ENTITY_LEASH_KNOT_BREAK", "ENTITY_LEASHKNOT_BREAK");
	public static final SimpleSound ENTITY_LEASH_KNOT_PLACE = new SimpleSound("ENTITY_LEASH_KNOT_PLACE", "ENTITY_LEASHKNOT_PLACE");
	public static final SimpleSound ENTITY_LIGHTNING_BOLT_IMPACT = new SimpleSound("ENTITY_LIGHTNING_BOLT_IMPACT", "ENTITY_LIGHTNING_IMPACT");
	public static final SimpleSound ENTITY_LIGHTNING_BOLT_THUNDER = new SimpleSound("ENTITY_LIGHTNING_BOLT_THUNDER", "ENTITY_LIGHTNING_THUNDER");
	public static final SimpleSound ENTITY_LINGERING_POTION_THROW = new SimpleSound("ENTITY_LINGERING_POTION_THROW", "ENTITY_LINGERINGPOTION_THROW");
	public static final SimpleSound ENTITY_LLAMA_AMBIENT = new SimpleSound("ENTITY_LLAMA_AMBIENT");
	public static final SimpleSound ENTITY_LLAMA_ANGRY = new SimpleSound("ENTITY_LLAMA_ANGRY");
	public static final SimpleSound ENTITY_LLAMA_CHEST = new SimpleSound("ENTITY_LLAMA_CHEST");
	public static final SimpleSound ENTITY_LLAMA_DEATH = new SimpleSound("ENTITY_LLAMA_DEATH");
	public static final SimpleSound ENTITY_LLAMA_EAT = new SimpleSound("ENTITY_LLAMA_EAT");
	public static final SimpleSound ENTITY_LLAMA_HURT = new SimpleSound("ENTITY_LLAMA_HURT");
	public static final SimpleSound ENTITY_LLAMA_SPIT = new SimpleSound("ENTITY_LLAMA_SPIT");
	public static final SimpleSound ENTITY_LLAMA_STEP = new SimpleSound("ENTITY_LLAMA_STEP");
	public static final SimpleSound ENTITY_LLAMA_SWAG = new SimpleSound("ENTITY_LLAMA_SWAG");
	public static final SimpleSound ENTITY_MAGMA_CUBE_DEATH = new SimpleSound("ENTITY_MAGMA_CUBE_DEATH", "ENTITY_MAGMACUBE_DEATH");
	public static final SimpleSound ENTITY_MAGMA_CUBE_DEATH_SMALL = new SimpleSound("ENTITY_MAGMA_CUBE_DEATH_SMALL", "ENTITY_SMALL_MAGMACUBE_DEATH");
	public static final SimpleSound ENTITY_MAGMA_CUBE_HURT = new SimpleSound("ENTITY_MAGMA_CUBE_HURT", "ENTITY_MAGMACUBE_HURT");
	public static final SimpleSound ENTITY_MAGMA_CUBE_HURT_SMALL = new SimpleSound("ENTITY_MAGMA_CUBE_HURT_SMALL", "ENTITY_SMALL_MAGMACUBE_HURT");
	public static final SimpleSound ENTITY_MAGMA_CUBE_JUMP = new SimpleSound("ENTITY_MAGMA_CUBE_JUMP", "ENTITY_MAGMACUBE_JUMP");
	public static final SimpleSound ENTITY_MAGMA_CUBE_SQUISH = new SimpleSound("ENTITY_MAGMA_CUBE_SQUISH", "ENTITY_MAGMACUBE_SQUISH");
	public static final SimpleSound ENTITY_MAGMA_CUBE_SQUISH_SMALL = new SimpleSound("ENTITY_MAGMA_CUBE_SQUISH_SMALL", "ENTITY_SMALL_MAGMACUBE_SQUISH");
	public static final SimpleSound ENTITY_MINECART_INSIDE = new SimpleSound("ENTITY_MINECART_INSIDE");
	public static final SimpleSound ENTITY_MINECART_RIDING = new SimpleSound("ENTITY_MINECART_RIDING");
	public static final SimpleSound ENTITY_MOOSHROOM_SHEAR = new SimpleSound("ENTITY_MOOSHROOM_SHEAR");
	public static final SimpleSound ENTITY_MULE_AMBIENT = new SimpleSound("ENTITY_MULE_AMBIENT");
	public static final SimpleSound ENTITY_MULE_CHEST = new SimpleSound("ENTITY_MULE_CHEST");
	public static final SimpleSound ENTITY_MULE_DEATH = new SimpleSound("ENTITY_MULE_DEATH");
	public static final SimpleSound ENTITY_MULE_HURT = new SimpleSound("ENTITY_MULE_HURT");
	public static final SimpleSound ENTITY_PAINTING_BREAK = new SimpleSound("ENTITY_PAINTING_BREAK");
	public static final SimpleSound ENTITY_PAINTING_PLACE = new SimpleSound("ENTITY_PAINTING_PLACE");
	public static final SimpleSound ENTITY_PARROT_AMBIENT = new SimpleSound("ENTITY_PARROT_AMBIENT");
	public static final SimpleSound ENTITY_PARROT_DEATH = new SimpleSound("ENTITY_PARROT_DEATH");
	public static final SimpleSound ENTITY_PARROT_EAT = new SimpleSound("ENTITY_PARROT_EAT");
	public static final SimpleSound ENTITY_PARROT_FLY = new SimpleSound("ENTITY_PARROT_FLY");
	public static final SimpleSound ENTITY_PARROT_HURT = new SimpleSound("ENTITY_PARROT_HURT");
	public static final SimpleSound ENTITY_PARROT_IMITATE_BLAZE = new SimpleSound("ENTITY_PARROT_IMITATE_BLAZE");
	public static final SimpleSound ENTITY_PARROT_IMITATE_CREEPER = new SimpleSound("ENTITY_PARROT_IMITATE_CREEPER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ELDER_GUARDIAN = new SimpleSound("ENTITY_PARROT_IMITATE_ELDER_GUARDIAN");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ENDER_DRAGON = new SimpleSound("ENTITY_PARROT_IMITATE_ENDER_DRAGON", "ENTITY_PARROT_IMITATE_ENDERDRAGON");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ENDERMAN = new SimpleSound("ENTITY_PARROT_IMITATE_ENDERMAN");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ENDERMITE = new SimpleSound("ENTITY_PARROT_IMITATE_ENDERMITE");
	public static final SimpleSound ENTITY_PARROT_IMITATE_EVOKER = new SimpleSound("ENTITY_PARROT_IMITATE_EVOKER", "ENTITY_PARROT_IMITATE_EVOCATION_ILLAGER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_GHAST = new SimpleSound("ENTITY_PARROT_IMITATE_GHAST");
	public static final SimpleSound ENTITY_PARROT_IMITATE_HUSK = new SimpleSound("ENTITY_PARROT_IMITATE_HUSK");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ILLUSIONER = new SimpleSound("ENTITY_PARROT_IMITATE_ILLUSIONER", "ENTITY_PARROT_IMITATE_ILLUSION_ILLAGER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_MAGMA_CUBE = new SimpleSound("ENTITY_PARROT_IMITATE_MAGMA_CUBE", "ENTITY_PARROT_IMITATE_MAGMACUBE");
	public static final SimpleSound ENTITY_PARROT_IMITATE_POLAR_BEAR = new SimpleSound("ENTITY_PARROT_IMITATE_POLAR_BEAR");
	public static final SimpleSound ENTITY_PARROT_IMITATE_SHULKER = new SimpleSound("ENTITY_PARROT_IMITATE_SHULKER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_SILVERFISH = new SimpleSound("ENTITY_PARROT_IMITATE_SILVERFISH");
	public static final SimpleSound ENTITY_PARROT_IMITATE_SKELETON = new SimpleSound("ENTITY_PARROT_IMITATE_SKELETON");
	public static final SimpleSound ENTITY_PARROT_IMITATE_SLIME = new SimpleSound("ENTITY_PARROT_IMITATE_SLIME");
	public static final SimpleSound ENTITY_PARROT_IMITATE_SPIDER = new SimpleSound("ENTITY_PARROT_IMITATE_SPIDER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_STRAY = new SimpleSound("ENTITY_PARROT_IMITATE_STRAY");
	public static final SimpleSound ENTITY_PARROT_IMITATE_VEX = new SimpleSound("ENTITY_PARROT_IMITATE_VEX");
	public static final SimpleSound ENTITY_PARROT_IMITATE_VINDICATOR = new SimpleSound("ENTITY_PARROT_IMITATE_VINDICATOR", "ENTITY_PARROT_IMITATE_VINDICATION_ILLAGER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_WITCH = new SimpleSound("ENTITY_PARROT_IMITATE_WITCH");
	public static final SimpleSound ENTITY_PARROT_IMITATE_WITHER = new SimpleSound("ENTITY_PARROT_IMITATE_WITHER");
	public static final SimpleSound ENTITY_PARROT_IMITATE_WITHER_SKELETON = new SimpleSound("ENTITY_PARROT_IMITATE_WITHER_SKELETON");
	public static final SimpleSound ENTITY_PARROT_IMITATE_WOLF = new SimpleSound("ENTITY_PARROT_IMITATE_WOLF");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ZOMBIE = new SimpleSound("ENTITY_PARROT_IMITATE_ZOMBIE");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN = new SimpleSound("ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN");
	public static final SimpleSound ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER = new SimpleSound("ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER");
	public static final SimpleSound ENTITY_PARROT_STEP = new SimpleSound("ENTITY_PARROT_STEP");
	public static final SimpleSound ENTITY_PIG_AMBIENT = new SimpleSound("ENTITY_PIG_AMBIENT");
	public static final SimpleSound ENTITY_PIG_DEATH = new SimpleSound("ENTITY_PIG_DEATH");
	public static final SimpleSound ENTITY_PIG_HURT = new SimpleSound("ENTITY_PIG_HURT");
	public static final SimpleSound ENTITY_PIG_SADDLE = new SimpleSound("ENTITY_PIG_SADDLE");
	public static final SimpleSound ENTITY_PIG_STEP = new SimpleSound("ENTITY_PIG_STEP");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_CRIT = new SimpleSound("ENTITY_PLAYER_ATTACK_CRIT");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_KNOCKBACK = new SimpleSound("ENTITY_PLAYER_ATTACK_KNOCKBACK");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_NODAMAGE = new SimpleSound("ENTITY_PLAYER_ATTACK_NODAMAGE");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_STRONG = new SimpleSound("ENTITY_PLAYER_ATTACK_STRONG");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_SWEEP = new SimpleSound("ENTITY_PLAYER_ATTACK_SWEEP");
	public static final SimpleSound ENTITY_PLAYER_ATTACK_WEAK = new SimpleSound("ENTITY_PLAYER_ATTACK_WEAK");
	public static final SimpleSound ENTITY_PLAYER_BIG_FALL = new SimpleSound("ENTITY_PLAYER_BIG_FALL");
	public static final SimpleSound ENTITY_PLAYER_BREATH = new SimpleSound("ENTITY_PLAYER_BREATH");
	public static final SimpleSound ENTITY_PLAYER_BURP = new SimpleSound("ENTITY_PLAYER_BURP");
	public static final SimpleSound ENTITY_PLAYER_DEATH = new SimpleSound("ENTITY_PLAYER_DEATH");
	public static final SimpleSound ENTITY_PLAYER_HURT = new SimpleSound("ENTITY_PLAYER_HURT");
	public static final SimpleSound ENTITY_PLAYER_HURT_DROWN = new SimpleSound("ENTITY_PLAYER_HURT_DROWN");
	public static final SimpleSound ENTITY_PLAYER_HURT_ON_FIRE = new SimpleSound("ENTITY_PLAYER_HURT_ON_FIRE");
	public static final SimpleSound ENTITY_PLAYER_LEVELUP = new SimpleSound("ENTITY_PLAYER_LEVELUP");
	public static final SimpleSound ENTITY_PLAYER_SMALL_FALL = new SimpleSound("ENTITY_PLAYER_SMALL_FALL");
	public static final SimpleSound ENTITY_PLAYER_SPLASH = new SimpleSound("ENTITY_PLAYER_SPLASH");
	public static final SimpleSound ENTITY_PLAYER_SWIM = new SimpleSound("ENTITY_PLAYER_SWIM");
	public static final SimpleSound ENTITY_POLAR_BEAR_AMBIENT = new SimpleSound("ENTITY_POLAR_BEAR_AMBIENT");
	public static final SimpleSound ENTITY_POLAR_BEAR_AMBIENT_BABY = new SimpleSound("ENTITY_POLAR_BEAR_AMBIENT_BABY", "ENTITY_POLAR_BEAR_BABY_AMBIENT");
	public static final SimpleSound ENTITY_POLAR_BEAR_DEATH = new SimpleSound("ENTITY_POLAR_BEAR_DEATH");
	public static final SimpleSound ENTITY_POLAR_BEAR_HURT = new SimpleSound("ENTITY_POLAR_BEAR_HURT");
	public static final SimpleSound ENTITY_POLAR_BEAR_STEP = new SimpleSound("ENTITY_POLAR_BEAR_STEP");
	public static final SimpleSound ENTITY_POLAR_BEAR_WARNING = new SimpleSound("ENTITY_POLAR_BEAR_WARNING");
	public static final SimpleSound ENTITY_RABBIT_AMBIENT = new SimpleSound("ENTITY_RABBIT_AMBIENT");
	public static final SimpleSound ENTITY_RABBIT_ATTACK = new SimpleSound("ENTITY_RABBIT_ATTACK");
	public static final SimpleSound ENTITY_RABBIT_DEATH = new SimpleSound("ENTITY_RABBIT_DEATH");
	public static final SimpleSound ENTITY_RABBIT_HURT = new SimpleSound("ENTITY_RABBIT_HURT");
	public static final SimpleSound ENTITY_RABBIT_JUMP = new SimpleSound("ENTITY_RABBIT_JUMP");
	public static final SimpleSound ENTITY_SHEEP_AMBIENT = new SimpleSound("ENTITY_SHEEP_AMBIENT");
	public static final SimpleSound ENTITY_SHEEP_DEATH = new SimpleSound("ENTITY_SHEEP_DEATH");
	public static final SimpleSound ENTITY_SHEEP_HURT = new SimpleSound("ENTITY_SHEEP_HURT");
	public static final SimpleSound ENTITY_SHEEP_SHEAR = new SimpleSound("ENTITY_SHEEP_SHEAR");
	public static final SimpleSound ENTITY_SHEEP_STEP = new SimpleSound("ENTITY_SHEEP_STEP");
	public static final SimpleSound ENTITY_SHULKER_AMBIENT = new SimpleSound("ENTITY_SHULKER_AMBIENT");
	public static final SimpleSound ENTITY_SHULKER_BULLET_HIT = new SimpleSound("ENTITY_SHULKER_BULLET_HIT");
	public static final SimpleSound ENTITY_SHULKER_BULLET_HURT = new SimpleSound("ENTITY_SHULKER_BULLET_HURT");
	public static final SimpleSound ENTITY_SHULKER_CLOSE = new SimpleSound("ENTITY_SHULKER_CLOSE");
	public static final SimpleSound ENTITY_SHULKER_DEATH = new SimpleSound("ENTITY_SHULKER_DEATH");
	public static final SimpleSound ENTITY_SHULKER_HURT = new SimpleSound("ENTITY_SHULKER_HURT");
	public static final SimpleSound ENTITY_SHULKER_HURT_CLOSED = new SimpleSound("ENTITY_SHULKER_HURT_CLOSED");
	public static final SimpleSound ENTITY_SHULKER_OPEN = new SimpleSound("ENTITY_SHULKER_OPEN");
	public static final SimpleSound ENTITY_SHULKER_SHOOT = new SimpleSound("ENTITY_SHULKER_SHOOT");
	public static final SimpleSound ENTITY_SHULKER_TELEPORT = new SimpleSound("ENTITY_SHULKER_TELEPORT");
	public static final SimpleSound ENTITY_SILVERFISH_AMBIENT = new SimpleSound("ENTITY_SILVERFISH_AMBIENT");
	public static final SimpleSound ENTITY_SILVERFISH_DEATH = new SimpleSound("ENTITY_SILVERFISH_DEATH");
	public static final SimpleSound ENTITY_SILVERFISH_HURT = new SimpleSound("ENTITY_SILVERFISH_HURT");
	public static final SimpleSound ENTITY_SILVERFISH_STEP = new SimpleSound("ENTITY_SILVERFISH_STEP");
	public static final SimpleSound ENTITY_SKELETON_AMBIENT = new SimpleSound("ENTITY_SKELETON_AMBIENT");
	public static final SimpleSound ENTITY_SKELETON_DEATH = new SimpleSound("ENTITY_SKELETON_DEATH");
	public static final SimpleSound ENTITY_SKELETON_HORSE_AMBIENT = new SimpleSound("ENTITY_SKELETON_HORSE_AMBIENT");
	public static final SimpleSound ENTITY_SKELETON_HORSE_DEATH = new SimpleSound("ENTITY_SKELETON_HORSE_DEATH");
	public static final SimpleSound ENTITY_SKELETON_HORSE_HURT = new SimpleSound("ENTITY_SKELETON_HORSE_HURT");
	public static final SimpleSound ENTITY_SKELETON_HURT = new SimpleSound("ENTITY_SKELETON_HURT");
	public static final SimpleSound ENTITY_SKELETON_SHOOT = new SimpleSound("ENTITY_SKELETON_SHOOT");
	public static final SimpleSound ENTITY_SKELETON_STEP = new SimpleSound("ENTITY_SKELETON_STEP");
	public static final SimpleSound ENTITY_SLIME_ATTACK = new SimpleSound("ENTITY_SLIME_ATTACK");
	public static final SimpleSound ENTITY_SLIME_DEATH = new SimpleSound("ENTITY_SLIME_DEATH");
	public static final SimpleSound ENTITY_SLIME_DEATH_SMALL = new SimpleSound("ENTITY_SLIME_DEATH_SMALL", "ENTITY_SMALL_SLIME_DEATH");
	public static final SimpleSound ENTITY_SLIME_HURT = new SimpleSound("ENTITY_SLIME_HURT");
	public static final SimpleSound ENTITY_SLIME_HURT_SMALL = new SimpleSound("ENTITY_SLIME_HURT_SMALL", "ENTITY_SMALL_SLIME_HURT");
	public static final SimpleSound ENTITY_SLIME_JUMP = new SimpleSound("ENTITY_SLIME_JUMP");
	public static final SimpleSound ENTITY_SLIME_JUMP_SMALL = new SimpleSound("ENTITY_SLIME_JUMP_SMALL", "ENTITY_SMALL_SLIME_JUMP");
	public static final SimpleSound ENTITY_SLIME_SQUISH = new SimpleSound("ENTITY_SLIME_SQUISH");
	public static final SimpleSound ENTITY_SLIME_SQUISH_SMALL = new SimpleSound("ENTITY_SLIME_SQUISH_SMALL", "ENTITY_SMALL_SLIME_SQUISH");
	public static final SimpleSound ENTITY_SNOW_GOLEM_AMBIENT = new SimpleSound("ENTITY_SNOW_GOLEM_AMBIENT", "ENTITY_SNOWMAN_AMBIENT");
	public static final SimpleSound ENTITY_SNOW_GOLEM_DEATH = new SimpleSound("ENTITY_SNOW_GOLEM_DEATH", "ENTITY_SNOWMAN_DEATH");
	public static final SimpleSound ENTITY_SNOW_GOLEM_HURT = new SimpleSound("ENTITY_SNOW_GOLEM_HURT", "ENTITY_SNOWMAN_HURT");
	public static final SimpleSound ENTITY_SNOW_GOLEM_SHOOT = new SimpleSound("ENTITY_SNOW_GOLEM_SHOOT", "ENTITY_SNOWMAN_SHOOT");
	public static final SimpleSound ENTITY_SNOWBALL_THROW = new SimpleSound("ENTITY_SNOWBALL_THROW");
	public static final SimpleSound ENTITY_SPIDER_AMBIENT = new SimpleSound("ENTITY_SPIDER_AMBIENT");
	public static final SimpleSound ENTITY_SPIDER_DEATH = new SimpleSound("ENTITY_SPIDER_DEATH");
	public static final SimpleSound ENTITY_SPIDER_HURT = new SimpleSound("ENTITY_SPIDER_HURT");
	public static final SimpleSound ENTITY_SPIDER_STEP = new SimpleSound("ENTITY_SPIDER_STEP");
	public static final SimpleSound ENTITY_SPLASH_POTION_BREAK = new SimpleSound("ENTITY_SPLASH_POTION_BREAK");
	public static final SimpleSound ENTITY_SPLASH_POTION_THROW = new SimpleSound("ENTITY_SPLASH_POTION_THROW");
	public static final SimpleSound ENTITY_SQUID_AMBIENT = new SimpleSound("ENTITY_SQUID_AMBIENT");
	public static final SimpleSound ENTITY_SQUID_DEATH = new SimpleSound("ENTITY_SQUID_DEATH");
	public static final SimpleSound ENTITY_SQUID_HURT = new SimpleSound("ENTITY_SQUID_HURT");
	public static final SimpleSound ENTITY_STRAY_AMBIENT = new SimpleSound("ENTITY_STRAY_AMBIENT");
	public static final SimpleSound ENTITY_STRAY_DEATH = new SimpleSound("ENTITY_STRAY_DEATH");
	public static final SimpleSound ENTITY_STRAY_HURT = new SimpleSound("ENTITY_STRAY_HURT");
	public static final SimpleSound ENTITY_STRAY_STEP = new SimpleSound("ENTITY_STRAY_STEP");
	public static final SimpleSound ENTITY_TNT_PRIMED = new SimpleSound("ENTITY_TNT_PRIMED");
	public static final SimpleSound ENTITY_VEX_AMBIENT = new SimpleSound("ENTITY_VEX_AMBIENT");
	public static final SimpleSound ENTITY_VEX_CHARGE = new SimpleSound("ENTITY_VEX_CHARGE");
	public static final SimpleSound ENTITY_VEX_DEATH = new SimpleSound("ENTITY_VEX_DEATH");
	public static final SimpleSound ENTITY_VEX_HURT = new SimpleSound("ENTITY_VEX_HURT");
	public static final SimpleSound ENTITY_VILLAGER_AMBIENT = new SimpleSound("ENTITY_VILLAGER_AMBIENT");
	public static final SimpleSound ENTITY_VILLAGER_DEATH = new SimpleSound("ENTITY_VILLAGER_DEATH");
	public static final SimpleSound ENTITY_VILLAGER_HURT = new SimpleSound("ENTITY_VILLAGER_HURT");
	public static final SimpleSound ENTITY_VILLAGER_NO = new SimpleSound("ENTITY_VILLAGER_NO");
	public static final SimpleSound ENTITY_VILLAGER_TRADE = new SimpleSound("ENTITY_VILLAGER_TRADE", "ENTITY_VILLAGER_TRADING");
	public static final SimpleSound ENTITY_VILLAGER_YES = new SimpleSound("ENTITY_VILLAGER_YES");
	public static final SimpleSound ENTITY_VINDICATOR_AMBIENT = new SimpleSound("ENTITY_VINDICATOR_AMBIENT", "ENTITY_VINDICATION_ILLAGER_AMBIENT");
	public static final SimpleSound ENTITY_VINDICATOR_DEATH = new SimpleSound("ENTITY_VINDICATOR_DEATH", "ENTITY_VINDICATION_ILLAGER_DEATH");
	public static final SimpleSound ENTITY_VINDICATOR_HURT = new SimpleSound("ENTITY_VINDICATOR_HURT", "ENTITY_VINDICATION_ILLAGER_HURT");
	public static final SimpleSound ENTITY_WITCH_AMBIENT = new SimpleSound("ENTITY_WITCH_AMBIENT");
	public static final SimpleSound ENTITY_WITCH_DEATH = new SimpleSound("ENTITY_WITCH_DEATH");
	public static final SimpleSound ENTITY_WITCH_DRINK = new SimpleSound("ENTITY_WITCH_DRINK");
	public static final SimpleSound ENTITY_WITCH_HURT = new SimpleSound("ENTITY_WITCH_HURT");
	public static final SimpleSound ENTITY_WITCH_THROW = new SimpleSound("ENTITY_WITCH_THROW");
	public static final SimpleSound ENTITY_WITHER_AMBIENT = new SimpleSound("ENTITY_WITHER_AMBIENT");
	public static final SimpleSound ENTITY_WITHER_BREAK_BLOCK = new SimpleSound("ENTITY_WITHER_BREAK_BLOCK");
	public static final SimpleSound ENTITY_WITHER_DEATH = new SimpleSound("ENTITY_WITHER_DEATH");
	public static final SimpleSound ENTITY_WITHER_HURT = new SimpleSound("ENTITY_WITHER_HURT");
	public static final SimpleSound ENTITY_WITHER_SHOOT = new SimpleSound("ENTITY_WITHER_SHOOT");
	public static final SimpleSound ENTITY_WITHER_SKELETON_AMBIENT = new SimpleSound("ENTITY_WITHER_SKELETON_AMBIENT");
	public static final SimpleSound ENTITY_WITHER_SKELETON_DEATH = new SimpleSound("ENTITY_WITHER_SKELETON_DEATH");
	public static final SimpleSound ENTITY_WITHER_SKELETON_HURT = new SimpleSound("ENTITY_WITHER_SKELETON_HURT");
	public static final SimpleSound ENTITY_WITHER_SKELETON_STEP = new SimpleSound("ENTITY_WITHER_SKELETON_STEP");
	public static final SimpleSound ENTITY_WITHER_SPAWN = new SimpleSound("ENTITY_WITHER_SPAWN");
	public static final SimpleSound ENTITY_WOLF_AMBIENT = new SimpleSound("ENTITY_WOLF_AMBIENT");
	public static final SimpleSound ENTITY_WOLF_DEATH = new SimpleSound("ENTITY_WOLF_DEATH");
	public static final SimpleSound ENTITY_WOLF_GROWL = new SimpleSound("ENTITY_WOLF_GROWL");
	public static final SimpleSound ENTITY_WOLF_HOWL = new SimpleSound("ENTITY_WOLF_HOWL");
	public static final SimpleSound ENTITY_WOLF_HURT = new SimpleSound("ENTITY_WOLF_HURT");
	public static final SimpleSound ENTITY_WOLF_PANT = new SimpleSound("ENTITY_WOLF_PANT");
	public static final SimpleSound ENTITY_WOLF_SHAKE = new SimpleSound("ENTITY_WOLF_SHAKE");
	public static final SimpleSound ENTITY_WOLF_STEP = new SimpleSound("ENTITY_WOLF_STEP");
	public static final SimpleSound ENTITY_WOLF_WHINE = new SimpleSound("ENTITY_WOLF_WHINE");
	public static final SimpleSound ENTITY_ZOMBIE_AMBIENT = new SimpleSound("ENTITY_ZOMBIE_AMBIENT");
	public static final SimpleSound ENTITY_ZOMBIE_ATTACK_IRON_DOOR = new SimpleSound("ENTITY_ZOMBIE_ATTACK_IRON_DOOR");
	public static final SimpleSound ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR = new SimpleSound("ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
	public static final SimpleSound ENTITY_ZOMBIE_BREAK_WOODEN_DOOR = new SimpleSound("ENTITY_ZOMBIE_BREAK_WOODEN_DOOR", "ENTITY_ZOMBIE_BREAK_DOOR_WOOD");
	public static final SimpleSound ENTITY_ZOMBIE_DEATH = new SimpleSound("ENTITY_ZOMBIE_DEATH");
	public static final SimpleSound ENTITY_ZOMBIE_HORSE_AMBIENT = new SimpleSound("ENTITY_ZOMBIE_HORSE_AMBIENT");
	public static final SimpleSound ENTITY_ZOMBIE_HORSE_DEATH = new SimpleSound("ENTITY_ZOMBIE_HORSE_DEATH");
	public static final SimpleSound ENTITY_ZOMBIE_HORSE_HURT = new SimpleSound("ENTITY_ZOMBIE_HORSE_HURT");
	public static final SimpleSound ENTITY_ZOMBIE_HURT = new SimpleSound("ENTITY_ZOMBIE_HURT");
	public static final SimpleSound ENTITY_ZOMBIE_INFECT = new SimpleSound("ENTITY_ZOMBIE_INFECT");
	public static final SimpleSound ENTITY_ZOMBIE_PIGMAN_AMBIENT = new SimpleSound("ENTITY_ZOMBIE_PIGMAN_AMBIENT", "ENTITY_ZOMBIE_PIG_AMBIENT");
	public static final SimpleSound ENTITY_ZOMBIE_PIGMAN_ANGRY = new SimpleSound("ENTITY_ZOMBIE_PIGMAN_ANGRY", "ENTITY_ZOMBIE_PIG_ANGRY");
	public static final SimpleSound ENTITY_ZOMBIE_PIGMAN_DEATH = new SimpleSound("ENTITY_ZOMBIE_PIGMAN_DEATH", "ENTITY_ZOMBIE_PIG_DEATH");
	public static final SimpleSound ENTITY_ZOMBIE_PIGMAN_HURT = new SimpleSound("ENTITY_ZOMBIE_PIGMAN_HURT", "ENTITY_ZOMBIE_PIG_HURT");
	public static final SimpleSound ENTITY_ZOMBIE_STEP = new SimpleSound("ENTITY_ZOMBIE_STEP");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_AMBIENT = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_AMBIENT");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_CONVERTED = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_CONVERTED");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_CURE = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_CURE");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_DEATH = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_DEATH");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_HURT = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_HURT");
	public static final SimpleSound ENTITY_ZOMBIE_VILLAGER_STEP = new SimpleSound("ENTITY_ZOMBIE_VILLAGER_STEP");
	public static final SimpleSound ITEM_ARMOR_EQUIP_CHAIN = new SimpleSound("ITEM_ARMOR_EQUIP_CHAIN");
	public static final SimpleSound ITEM_ARMOR_EQUIP_DIAMOND = new SimpleSound("ITEM_ARMOR_EQUIP_DIAMOND");
	public static final SimpleSound ITEM_ARMOR_EQUIP_ELYTRA = new SimpleSound("ITEM_ARMOR_EQUIP_ELYTRA");
	public static final SimpleSound ITEM_ARMOR_EQUIP_GENERIC = new SimpleSound("ITEM_ARMOR_EQUIP_GENERIC");
	public static final SimpleSound ITEM_ARMOR_EQUIP_GOLD = new SimpleSound("ITEM_ARMOR_EQUIP_GOLD");
	public static final SimpleSound ITEM_ARMOR_EQUIP_IRON = new SimpleSound("ITEM_ARMOR_EQUIP_IRON");
	public static final SimpleSound ITEM_ARMOR_EQUIP_LEATHER = new SimpleSound("ITEM_ARMOR_EQUIP_LEATHER");
	public static final SimpleSound ITEM_BOTTLE_EMPTY = new SimpleSound("ITEM_BOTTLE_EMPTY");
	public static final SimpleSound ITEM_BOTTLE_FILL = new SimpleSound("ITEM_BOTTLE_FILL");
	public static final SimpleSound ITEM_BOTTLE_FILL_DRAGONBREATH = new SimpleSound("ITEM_BOTTLE_FILL_DRAGONBREATH");
	public static final SimpleSound ITEM_BUCKET_EMPTY = new SimpleSound("ITEM_BUCKET_EMPTY");
	public static final SimpleSound ITEM_BUCKET_EMPTY_LAVA = new SimpleSound("ITEM_BUCKET_EMPTY_LAVA");
	public static final SimpleSound ITEM_BUCKET_FILL = new SimpleSound("ITEM_BUCKET_FILL");
	public static final SimpleSound ITEM_BUCKET_FILL_LAVA = new SimpleSound("ITEM_BUCKET_FILL_LAVA");
	public static final SimpleSound ITEM_CHORUS_FRUIT_TELEPORT = new SimpleSound("ITEM_CHORUS_FRUIT_TELEPORT");
	public static final SimpleSound ITEM_ELYTRA_FLYING = new SimpleSound("ITEM_ELYTRA_FLYING");
	public static final SimpleSound ITEM_FIRECHARGE_USE = new SimpleSound("ITEM_FIRECHARGE_USE");
	public static final SimpleSound ITEM_FLINTANDSTEEL_USE = new SimpleSound("ITEM_FLINTANDSTEEL_USE");
	public static final SimpleSound ITEM_HOE_TILL = new SimpleSound("ITEM_HOE_TILL");
	public static final SimpleSound ITEM_SHIELD_BLOCK = new SimpleSound("ITEM_SHIELD_BLOCK");
	public static final SimpleSound ITEM_SHIELD_BREAK = new SimpleSound("ITEM_SHIELD_BREAK");
	public static final SimpleSound ITEM_SHOVEL_FLATTEN = new SimpleSound("ITEM_SHOVEL_FLATTEN");
	public static final SimpleSound ITEM_TOTEM_USE = new SimpleSound("ITEM_TOTEM_USE");
	public static final SimpleSound MUSIC_CREATIVE = new SimpleSound("MUSIC_CREATIVE");
	public static final SimpleSound MUSIC_CREDITS = new SimpleSound("MUSIC_CREDITS");
	public static final SimpleSound MUSIC_DISC_11 = new SimpleSound("MUSIC_DISC_11", "RECORD_11");
	public static final SimpleSound MUSIC_DISC_13 = new SimpleSound("MUSIC_DISC_13", "RECORD_13");
	public static final SimpleSound MUSIC_DISC_BLOCKS = new SimpleSound("MUSIC_DISC_BLOCKS", "RECORD_BLOCKS");
	public static final SimpleSound MUSIC_DISC_CAT = new SimpleSound("MUSIC_DISC_CAT", "RECORD_CAT");
	public static final SimpleSound MUSIC_DISC_CHIRP = new SimpleSound("MUSIC_DISC_CHIRP", "RECORD_CHIRP");
	public static final SimpleSound MUSIC_DISC_FAR = new SimpleSound("MUSIC_DISC_FAR", "RECORD_FAR");
	public static final SimpleSound MUSIC_DISC_MALL = new SimpleSound("MUSIC_DISC_MALL", "RECORD_MALL");
	public static final SimpleSound MUSIC_DISC_MELLOHI = new SimpleSound("MUSIC_DISC_MELLOHI", "RECORD_MELLOHI");
	public static final SimpleSound MUSIC_DISC_STAL = new SimpleSound("MUSIC_DISC_STAL", "RECORD_STAL");
	public static final SimpleSound MUSIC_DISC_STRAD = new SimpleSound("MUSIC_DISC_STRAD", "RECORD_STRAD");
	public static final SimpleSound MUSIC_DISC_WAIT = new SimpleSound("MUSIC_DISC_WAIT", "RECORD_WAIT");
	public static final SimpleSound MUSIC_DISC_WARD = new SimpleSound("MUSIC_DISC_WARD", "RECORD_WARD");
	public static final SimpleSound MUSIC_DRAGON = new SimpleSound("MUSIC_DRAGON");
	public static final SimpleSound MUSIC_END = new SimpleSound("MUSIC_END");
	public static final SimpleSound MUSIC_GAME = new SimpleSound("MUSIC_GAME");
	public static final SimpleSound MUSIC_MENU = new SimpleSound("MUSIC_MENU");
	public static final SimpleSound MUSIC_NETHER = new SimpleSound("MUSIC_NETHER");
	public static final SimpleSound UI_BUTTON_CLICK = new SimpleSound("UI_BUTTON_CLICK");
	public static final SimpleSound UI_TOAST_CHALLENGE_COMPLETE = new SimpleSound("UI_TOAST_CHALLENGE_COMPLETE");
	public static final SimpleSound UI_TOAST_IN = new SimpleSound("UI_TOAST_IN");
	public static final SimpleSound UI_TOAST_OUT = new SimpleSound("UI_TOAST_OUT");
	public static final SimpleSound WEATHER_RAIN = new SimpleSound("WEATHER_RAIN");
	public static final SimpleSound WEATHER_RAIN_ABOVE = new SimpleSound("WEATHER_RAIN_ABOVE");

	public static class SimpleSound {

		private final Sound sound;

		private SimpleSound(String name) {
			this.sound = Enums.getIfPresent(Sound.class, name).orNull();
		}

		private SimpleSound(String latestName, String oldName) {
			this.sound = Enums.getIfPresent(Sound.class, ServerVersion.getVersionNumber() >= 13 ? latestName : oldName).orNull();
		}

		public void playSound(Location location, float volume, float pitch) {
			if (this.sound != null) {
				if (!Sounds.isHandled()) {
					location.getWorld().playSound(location, this.sound, volume, pitch);
				} else {
					Sounds.playSound(sound.name(), location.getX(), location.getY(), location.getZ(), volume, pitch);
				}
			}
		}

		public void playSound(Location location) {
			this.playSound(location, 5, 1);
		}

		public void playSound(Player player, Location location, float volume, float pitch) {
			if (this.sound != null) {
				if (!Sounds.isHandled()) {
					player.playSound(location, this.sound, volume, pitch);
				} else {
					Sounds.playSound(player, sound.name(), location.getX(), location.getY(), location.getZ(), volume, pitch);
				}
			}
		}

		public void playSound(Player player, Location location) {
			this.playSound(player, location, 5, 1);
		}

		public void playSound(Player player, float volume, float pitch) {
			this.playSound(player, player.getLocation(), volume, pitch);
		}

		public void playSound(Player player) {
			this.playSound(player, 5, 1);
		}

		public void playSound(Collection<Player> players, Location location, float volume, float pitch) {
			for (Player player : players) {
				this.playSound(player, location, volume, pitch);
			}
		}

		public void playSound(Collection<Player> players, Location location) {
			for (Player player : players) {
				this.playSound(player, location, 5, 1);
			}
		}

		public void playSound(Collection<Player> players, float volume, float pitch) {
			for (Player player : players) {
				this.playSound(player, player.getLocation(), volume, pitch);
			}
		}

		public void playSound(Collection<Player> players) {
			for (Player player : players) {
				this.playSound(player);
			}
		}

		public void broadcastSound() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				this.playSound(player);
			}
		}

	}

	public static final SimpleInstrument BASS_DRUM = new SimpleInstrument(BLOCK_NOTE_BLOCK_BASEDRUM);
	public static final SimpleInstrument BASS_GUITAR = new SimpleInstrument(BLOCK_NOTE_BLOCK_BASS);
	public static final SimpleInstrument PIANO = new SimpleInstrument(BLOCK_NOTE_BLOCK_HARP);
	public static final SimpleInstrument SNARE_DRUM = new SimpleInstrument(BLOCK_NOTE_BLOCK_SNARE);
	public static final SimpleInstrument STICKS = new SimpleInstrument(BLOCK_NOTE_BLOCK_HAT);
	public static final SimpleInstrument BELL = new SimpleInstrument(BLOCK_NOTE_BLOCK_BELL);
	public static final SimpleInstrument CHIME = new SimpleInstrument(BLOCK_NOTE_BLOCK_CHIME);
	public static final SimpleInstrument FLUTE = new SimpleInstrument(BLOCK_NOTE_BLOCK_FLUTE);
	public static final SimpleInstrument GUITAR = new SimpleInstrument(BLOCK_NOTE_BLOCK_GUITAR);
	public static final SimpleInstrument XYLOPHONE = new SimpleInstrument(BLOCK_NOTE_BLOCK_XYLOPHONE);

	public static class SimpleInstrument {

		private final SimpleSound simpleSound;

		private SimpleInstrument(SimpleSound sound) {
			this.simpleSound = sound;
		}

		public void playInstrument(Player player, Location location, Note note) {
			simpleSound.playSound(player, location, 4, (float) Math.pow(2.0D, (note.getId() - 12.0D) / 12.0D));
		}

		public void playInstrument(Player player, Note note) {
			this.playInstrument(player, player.getLocation(), note);
		}

		public void playInstrument(Collection<Player> players, Location location, Note note) {
			for (Player player : players) {
				this.playInstrument(player, location, note);
			}
		}

		public void playInstrument(Collection<Player> players, Note note) {
			for (Player player : players) {
				this.playInstrument(player, note);
			}
		}

		public void broadcastInstrument(Note note) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				this.playInstrument(player, note);
			}
		}

	}

	public static class CustomSound {

		private final String sound;

		public CustomSound(String sound) {
			this.sound = sound;
		}

		public void playSound(Location location, float volume, float pitch) {
			if (this.sound != null) {
				location.getWorld().playSound(location, this.sound, volume, pitch);
			}
		}

		public void playSound(Location location) {
			this.playSound(location, 5, 1);
		}

		public void playSound(Player player, Location location, float volume, float pitch) {
			if (this.sound != null) {
				player.playSound(location, this.sound, volume, pitch);
			}
		}

		public void playSound(Player player, Location location) {
			this.playSound(player, location, 5, 1);
		}

		public void playSound(Player player, float volume, float pitch) {
			this.playSound(player, player.getLocation(), volume, pitch);
		}

		public void playSound(Player player) {
			this.playSound(player, 5, 1);
		}

		public void playSound(Collection<Player> players, Location location, float volume, float pitch) {
			for (Player player : players) {
				this.playSound(player, location, volume, pitch);
			}
		}

		public void playSound(Collection<Player> players, Location location) {
			for (Player player : players) {
				this.playSound(player, location, 5, 1);
			}
		}

		public void playSound(Collection<Player> players, float volume, float pitch) {
			for (Player player : players) {
				this.playSound(player, player.getLocation(), volume, pitch);
			}
		}

		public void playSound(Collection<Player> players) {
			for (Player player : players) {
				this.playSound(player);
			}
		}

		public void broadcastSound() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				this.playSound(player);
			}
		}

	}

}