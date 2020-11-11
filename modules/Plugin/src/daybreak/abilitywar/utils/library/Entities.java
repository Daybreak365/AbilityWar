package daybreak.abilitywar.utils.library;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class Entities {

	private static final EnumMap<EntityType, SimpleEntity> ENTITY_MAP = new EnumMap<>(EntityType.class);

	public static final SimpleEntity AREA_EFFECT_CLOUD = new SimpleEntity("AREA_EFFECT_CLOUD", "광역 효과 구름");
	public static final SimpleEntity ARMOR_STAND = new SimpleEntity("ARMOR_STAND", "갑옷 거치대");
	public static final SimpleEntity ARROW = new SimpleEntity("ARROW", "화살");
	public static final SimpleEntity BAT = new SimpleEntity("BAT", "박쥐");
	public static final SimpleEntity BEE = new SimpleEntity("BEE", "꿀벌");
	public static final SimpleEntity BLAZE = new SimpleEntity("BLAZE", "블레이즈");
	public static final SimpleEntity BOAT = new SimpleEntity("BOAT", "보트");
	public static final SimpleEntity CAT = new SimpleEntity("CAT", "고양이");
	public static final SimpleEntity CAVE_SPIDER = new SimpleEntity("CAVE_SPIDER", "동굴 거미");
	public static final SimpleEntity CHICKEN = new SimpleEntity("CHICKEN", "닭");
	public static final SimpleEntity COD = new SimpleEntity("COD", "대구");
	public static final SimpleEntity COW = new SimpleEntity("COW", "소");
	public static final SimpleEntity CREEPER = new SimpleEntity("CREEPER", "크리퍼");
	public static final SimpleEntity DOLPHIN = new SimpleEntity("DOLPHIN", "돌고래");
	public static final SimpleEntity DONKEY = new SimpleEntity("DONKEY", "당나귀");
	public static final SimpleEntity DRAGON_FIREBALL = new SimpleEntity("DRAGON_FIREBALL", "드래곤 화염구");
	public static final SimpleEntity DROPPED_ITEM = new SimpleEntity("DROPPED_ITEM", "아이템");
	public static final SimpleEntity DROWNED = new SimpleEntity("DROWNED", "드라운드");
	public static final SimpleEntity EGG = new SimpleEntity("EGG", "던져진 달걀");
	public static final SimpleEntity ELDER_GUARDIAN = new SimpleEntity("ELDER_GUARDIAN", "엘더 가디언");
	public static final SimpleEntity ENDERMAN = new SimpleEntity("ENDERMAN", "엔더맨");
	public static final SimpleEntity ENDERMITE = new SimpleEntity("ENDERMITE", "엔더마이트");
	public static final SimpleEntity ENDER_CRYSTAL = new SimpleEntity("ENDER_CRYSTAL", "엔드 수정");
	public static final SimpleEntity ENDER_DRAGON = new SimpleEntity("ENDER_DRAGON", "엔더 드래곤");
	public static final SimpleEntity ENDER_PEARL = new SimpleEntity("ENDER_PEARL", "던져진 엔더 진주");
	public static final SimpleEntity ENDER_SIGNAL = new SimpleEntity("ENDER_SIGNAL", "엔더의 눈");
	public static final SimpleEntity EVOKER = new SimpleEntity("EVOKER", "소환사");
	public static final SimpleEntity EVOKER_FANGS = new SimpleEntity("EVOKER_FANGS", "소환사 송곳니");
	public static final SimpleEntity EXPERIENCE_ORB = new SimpleEntity("EXPERIENCE_ORB", "경험 구슬");
	public static final SimpleEntity FALLING_BLOCK = new SimpleEntity("FALLING_BLOCK", "떨어지는 블록");
	public static final SimpleEntity FIREBALL = new SimpleEntity("FIREBALL", "화염구");
	public static final SimpleEntity FIREWORK = new SimpleEntity("FIREWORK", "폭죽 로켓");
	public static final SimpleEntity FOX = new SimpleEntity("FOX", "여우");
	public static final SimpleEntity GHAST = new SimpleEntity("GHAST", "가스트");
	public static final SimpleEntity GIANT = new SimpleEntity("GIANT", "거인");
	public static final SimpleEntity GUARDIAN = new SimpleEntity("GUARDIAN", "가디언");
	public static final SimpleEntity HOGLIN = new SimpleEntity("HOGLIN", "호글린");
	public static final SimpleEntity HORSE = new SimpleEntity("HORSE", "말");
	public static final SimpleEntity HUSK = new SimpleEntity("HUSK", "허스크");
	public static final SimpleEntity ILLUSIONER = new SimpleEntity("ILLUSIONER", "환술사");
	public static final SimpleEntity IRON_GOLEM = new SimpleEntity("IRON_GOLEM", "철 골렘");
	public static final SimpleEntity ITEM_FRAME = new SimpleEntity("ITEM_FRAME", "아이템 액자");
	public static final SimpleEntity LLAMA = new SimpleEntity("LLAMA", "라마");
	public static final SimpleEntity LLAMA_SPIT = new SimpleEntity("LLAMA_SPIT", "라마 침");
	public static final SimpleEntity MAGMA_CUBE = new SimpleEntity("MAGMA_CUBE", "마그마 큐브");
	public static final SimpleEntity MINECART = new SimpleEntity("MINECART", "광산 수레");
	public static final SimpleEntity MINECART_CHEST = new SimpleEntity("MINECART_CHEST", "상자가 실린 광산 수레");
	public static final SimpleEntity MINECART_COMMAND = new SimpleEntity("MINECART_COMMAND", "명령 블록이 실린 광산 수레");
	public static final SimpleEntity MINECART_FURNACE = new SimpleEntity("MINECART_FURNACE", "화로가 실린 광산 수레");
	public static final SimpleEntity MINECART_HOPPER = new SimpleEntity("MINECART_HOPPER", "호퍼가 실린 광산 수레");
	public static final SimpleEntity MINECART_MOB_SPAWNER = new SimpleEntity("MINECART_MOB_SPAWNER", "생성기가 실린 광산 수레");
	public static final SimpleEntity MINECART_TNT = new SimpleEntity("MINECART_TNT", "TNT가 실린 광산 수레");
	public static final SimpleEntity MULE = new SimpleEntity("MULE", "노새");
	public static final SimpleEntity MUSHROOM_COW = new SimpleEntity("MUSHROOM_COW", "무시룸");
	public static final SimpleEntity OCELOT = new SimpleEntity("OCELOT", "오실롯");
	public static final SimpleEntity PANDA = new SimpleEntity("PANDA", "판다");
	public static final SimpleEntity PARROT = new SimpleEntity("PARROT", "앵무새");
	public static final SimpleEntity PHANTOM = new SimpleEntity("PHANTOM", "팬텀");
	public static final SimpleEntity PIG = new SimpleEntity("PIG", "돼지");
	public static final SimpleEntity PIGLIN = new SimpleEntity("PIGLIN", "피글린");
	public static final SimpleEntity PIGLIN_BRUTE = new SimpleEntity("PIGLIN_BRUTE", "피글린 야수");
	public static final SimpleEntity PILLAGER = new SimpleEntity("PILLAGER", "약탈자");
	public static final SimpleEntity PLAYER = new SimpleEntity("PLAYER", "플레이어");
	public static final SimpleEntity POLAR_BEAR = new SimpleEntity("POLAR_BEAR", "북극곰");
	public static final SimpleEntity PRIMED_TNT = new SimpleEntity("PRIMED_TNT", "점화된 TNT");
	public static final SimpleEntity PUFFERFISH = new SimpleEntity("PUFFERFISH", "복어");
	public static final SimpleEntity RABBIT = new SimpleEntity("RABBIT", "토끼");
	public static final SimpleEntity RAVAGER = new SimpleEntity("RAVAGER", "파괴수");
	public static final SimpleEntity SALMON = new SimpleEntity("SALMON", "연어");
	public static final SimpleEntity SHEEP = new SimpleEntity("SHEEP", "양");
	public static final SimpleEntity SHULKER = new SimpleEntity("SHULKER", "셜커");
	public static final SimpleEntity SHULKER_BULLET = new SimpleEntity("SHULKER_BULLET", "셜커 탄환");
	public static final SimpleEntity SILVERFISH = new SimpleEntity("SILVERFISH", "좀벌레");
	public static final SimpleEntity SKELETON = new SimpleEntity("SKELETON", "스켈레톤");
	public static final SimpleEntity SKELETON_HORSE = new SimpleEntity("SKELETON_HORSE", "스켈레톤 말");
	public static final SimpleEntity SLIME = new SimpleEntity("SLIME", "슬라임");
	public static final SimpleEntity SMALL_FIREBALL = new SimpleEntity("SMALL_FIREBALL", "작은 화염구");
	public static final SimpleEntity SNOWBALL = new SimpleEntity("SNOWBALL", "눈덩이");
	public static final SimpleEntity SNOWMAN = new SimpleEntity("SNOWMAN", "눈 골렘");
	public static final SimpleEntity SPECTRAL_ARROW = new SimpleEntity("SPECTRAL_ARROW", "분광 화살");
	public static final SimpleEntity SPIDER = new SimpleEntity("SPIDER", "거미");
	public static final SimpleEntity SPLASH_POTION = new SimpleEntity("SPLASH_POTION", "물약");
	public static final SimpleEntity SQUID = new SimpleEntity("SQUID", "오징어");
	public static final SimpleEntity STRAY = new SimpleEntity("STRAY", "스트레이");
	public static final SimpleEntity STRIDER = new SimpleEntity("STRIDER", "스트라이더");
	public static final SimpleEntity THROWN_EXP_BOTTLE = new SimpleEntity("THROWN_EXP_BOTTLE", "던져진 경험치 병");
	public static final SimpleEntity TRADER_LLAMA = new SimpleEntity("TRADER_LLAMA", "상인 라마");
	public static final SimpleEntity TRIDENT = new SimpleEntity("TRIDENT", "삼지창");
	public static final SimpleTropicalFish TROPICAL_FISH = new SimpleTropicalFish();
	public static final SimpleEntity TURTLE = new SimpleEntity("TURTLE", "거북");
	public static final SimpleEntity VEX = new SimpleEntity("VEX", "벡스");
	public static final SimpleEntity VILLAGER = new SimpleEntity("VILLAGER", "주민");
	public static final SimpleEntity VINDICATOR = new SimpleEntity("VINDICATOR", "변명자");
	public static final SimpleEntity WANDERING_TRADER = new SimpleEntity("WANDERING_TRADER", "떠돌이 상인");
	public static final SimpleEntity WITCH = new SimpleEntity("WITCH", "마녀");
	public static final SimpleEntity WITHER = new SimpleEntity("WITHER", "위더");
	public static final SimpleEntity WITHER_SKELETON = new SimpleEntity("WITHER_SKELETON", "위더 스켈레톤");
	public static final SimpleEntity WITHER_SKULL = new SimpleEntity("WITHER_SKULL", "위더 해골");
	public static final SimpleEntity WOLF = new SimpleEntity("WOLF", "늑대");
	public static final SimpleEntity ZOGLIN = new SimpleEntity("ZOGLIN", "조글린");
	public static final SimpleEntity ZOMBIE = new SimpleEntity("ZOMBIE", "좀비");
	public static final SimpleEntity ZOMBIE_HORSE = new SimpleEntity("ZOMBIE_HORSE", "좀비 말");
	public static final SimpleEntity ZOMBIE_VILLAGER = new SimpleEntity("ZOMBIE_VILLAGER", "좀비 주민");
	public static final SimpleEntity ZOMBIFIED_PIGLIN = new SimpleEntity(new String[]{"PIG_ZOMBIE", "ZOMBIFIED_PIGLIN"}, "좀비화 피글린");
	public static final SimpleEntity UNKNOWN = new SimpleEntity();

	public static SimpleEntity of(final EntityType entityType) {
		return ENTITY_MAP.getOrDefault(entityType, UNKNOWN);
	}

	public static class SimpleEntity {

		private final EntityType entityType;
		private final String name;

		private SimpleEntity(final String[] typeNames, final String name) {
			EntityType entityType = EntityType.UNKNOWN;
			for (final String typeName : typeNames) {
				final Optional<EntityType> optional = Enums.getIfPresent(EntityType.class, typeName);
				if (optional.isPresent()) {
					entityType = optional.get();
					ENTITY_MAP.put(entityType, this);
					break;
				}
			}
			this.entityType = entityType;
			this.name = name;
		}

		private SimpleEntity(final String typeName, final String name) {
			final Optional<EntityType> optional = Enums.getIfPresent(EntityType.class, typeName);
			if (optional.isPresent()) {
				this.entityType = optional.get();
				ENTITY_MAP.put(this.entityType, this);
			} else this.entityType = EntityType.UNKNOWN;
			this.name = name;
		}

		private SimpleEntity() {
			this.entityType = EntityType.UNKNOWN;
			this.name = "알 수 없음";
		}

		public String getName() {
			return name;
		}

		public boolean isKnown() {
			return entityType != EntityType.UNKNOWN;
		}

		@NotNull
		public Entity spawnEntity(@NotNull final Location location) {
			if (!isKnown()) throw new IllegalStateException("Cannot spawn unknown entity.");
			return location.getWorld().spawnEntity(location, entityType);
		}

		public EntityType getEntityType() {
			return entityType;
		}

	}

	public static final class SimpleTropicalFish extends SimpleEntity {

		private SimpleTropicalFish() {
			super("TROPICAL_FISH", "열대어");
		}

		public String getName(final @NotNull SimpleTropicalFish.Pattern pattern) {
			return pattern.name;
		}

		public enum Pattern {
			KOB("보구치"),
			SUNSTREAK("볕금고기"),
			SNOOPER("서성이"),
			DASHER("날쌘돌이"),
			BRINELY("소금치"),
			SPOTTY("점박이"),
			FLOPPER("퍼덕이"),
			STRIPEY("줄무늬"),
			GLITTER("반짝이"),
			BLOCKFISH("사각고기"),
			BETTY("싸움고기"),
			CLAYFISH("점토고기");

			private final String name;

			Pattern(final String name) {
				this.name = name;
			}
		}
	}

}
