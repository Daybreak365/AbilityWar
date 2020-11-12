package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.item.Skulls;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.RGB;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AbilityManifest(name = "컬스", rank = Rank.A, species = Species.OTHERS, explain = {
		"주위 13칸 안에 있는 상대를 원거리에서 철괴 우클릭으로 타게팅해 $[DURATION_CONFIG]초간",
		"지속되는 저주 인형을 내 위치에 만들어내며, 저주 인형이 대미지를 입을 경우",
		"대미지의 일부가 상대에게 전이됩니다. $[COOLDOWN_CONFIG]",
		"대상의 체력이 적을수록 더욱 큰 대미지를 입힐 수 있습니다."
})
@Tips(tip = {
		"저주 인형을 만들어 상대를 고통스럽게 하십시오. 한 번 저주 인형이",
		"생겨난 이상 상대가 아무리 멀리 가더라도 공격할 수 있기 때문에,",
		"많은 상황에서 유용하게 사용될 수 있습니다. 또한 대상의 체력이 적을",
		"수록 대미지가 강해지기 때문에, 체력이 적은 상태에서 도망가는 적을",
		"죽이기에도 적합합니다."
}, strong = {
		@Description(subject = "원거리 공격", explain = {
				"저주 인형을 만들어낸 후, 멀리서 적을 공격하세요."
		})
}, weak = {
		@Description(subject = "저주 인형", explain = {
				"저주 인형은 고정되지 않습니다. 납치 등의 능력으로 이동시킬 수 있으며",
				"이로 인해 능력이 의미가 없어질 수 있습니다. 또한, 능력 사용자를 저주",
				"인형으로부터 멀리 떨어뜨릴 수 있는 능력에게도 취약합니다."
		})
}, stats = @Stats(offense = Level.SIX, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Curse extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Curse.class, "cooldown", 100,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Curse.class, "duration", 10,
			"# 지속시간") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};

	public Curse(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

	private static final RGB BLACK = RGB.of(1, 1, 1);

	private Player target = null;
	private ArmorStand armorStand = null;

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			Player player = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 13, predicate);
			if (player != null) {
				target = player;
				skill.start();
			}
		}
		return false;
	}
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 10, cooldownTimer) {
		private int particle;

		@Override
		protected void onDurationStart() {
			armorStand = target.getWorld().spawn(getPlayer().getLocation(), ArmorStand.class);
			if (ServerVersion.getVersion() >= 10 && ServerVersion.getVersion() < 15)
				armorStand.setInvulnerable(true);
			armorStand.setCustomName(target.getName());
			armorStand.setCustomNameVisible(true);
			armorStand.setBasePlate(false);
			armorStand.setArms(true);
			armorStand.setGravity(false);
			EntityEquipment equipment = armorStand.getEquipment();
			equipment.setArmorContents(target.getInventory().getArmorContents());
			equipment.setHelmet(Skulls.createSkull(target));
			this.particle = 0;
		}

		@Override
		protected void onDurationProcess(int seconds) {
			if (++particle >= 10) {
				showHelix(armorStand.getLocation());
				particle = 0;
			}
			Location location = armorStand.getLocation();
			location.setYaw(location.getYaw() + 5);
			armorStand.teleport(location);
		}

		@Override
		protected void onDurationEnd() {
			target = null;
			armorStand.remove();
			armorStand = null;
		}

		@Override
		protected void onDurationSilentEnd() {
			target = null;
			armorStand.remove();
			armorStand = null;
		}
	}.setPeriod(TimeUnit.TICKS, 2);

	private static final int particleCount = 20;
	private static final double yDiff = 0.6 / particleCount;
	private static final Circle helixCircle = Circle.of(0.5, particleCount);

	private void showHelix(Location target) {
		new AbilityTimer((particleCount * 3) / 2) {
			int count = 0;

			@Override
			protected void run(int a) {
				for (int i = 0; i < 2; i++) {
					ParticleLib.REDSTONE.spawnParticle(target.clone().add(helixCircle.get(count % 20)).add(0, count * yDiff, 0), Curse.BLACK);
					count++;
				}
			}
		}.setPeriod(TimeUnit.TICKS, 1).start();
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (skill.isRunning() && e.getEntity().equals(armorStand)) {
			e.setCancelled(true);
			target.damage(e.getDamage() * (2.3 * (1 / Math.max(target.getHealth(), 0.01))), armorStand);
			if (e.getDamager() instanceof Player) {
				SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound((Player) e.getDamager());
			}
		}
	}

	@SubscribeEvent
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (skill.isRunning() && e.getEntity().equals(armorStand)) {
			e.setCancelled(true);
			target.damage(e.getDamage() * (2.3 * (1 / Math.max(target.getHealth(), 0.01))), armorStand);
		}
	}

	@SubscribeEvent
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().equals(armorStand)) e.setCancelled(true);
	}

}
