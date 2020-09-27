package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(name = "스왑", rank = Rank.B, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주변 $[DISTANCE_CONFIG]칸 이내에 있는 모든 플레이어의 핫바 슬롯을",
		"임의로 변경합니다. $[COOLDOWN_CONFIG]",
})
public class Swap extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "COOLDOWN", 35,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "DURATION", 4,
			"# 지속 시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DISTANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Swap.class, "DISTANCE", 8,
			"# 스킬 범위") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	private static final Random random = new Random();

	public Swap(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
					&& getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue();
		}
	};

	private final int distance = DISTANCE_CONFIG.getValue();
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration duration = new Duration(DURATION_CONFIG.getValue() * 10, cooldown) {
		@Override
		protected void onDurationProcess(int count) {
			for (final Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), distance, distance, ONLY_PARTICIPANTS)) {
				player.getInventory().setHeldItemSlot(random.nextInt(9));
				if (count % 3 == 0) {
					SoundLib.UI_BUTTON_CLICK.playSound(player);
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !duration.isDuration() && !cooldown.isCooldown()) {
			duration.start();
		}
		return false;
	}
}
