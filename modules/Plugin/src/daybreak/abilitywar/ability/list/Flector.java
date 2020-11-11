package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
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
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@AbilityManifest(name = "플렉터", rank = Rank.S, species = Species.HUMAN, explain = {
		"발사체를 바라본 상태로 검을 휘둘러 해당 발사체를",
		"튕겨낼 수 있습니다. 본인이 발사한 발사체는 튕겨낼 수 없으며,",
		"다른 플레이어가가 발사한 발사체를 튕겨낼 경우 §e♥ §f만큼의",
		"§e추가 체력§f을 얻습니다. §e추가 체력§f은 5칸 이상 얻을 수 없습니다.",
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주변의 모든 투사체를 튕겨냅니다. $[COOLDOWN_CONFIG]"
})
@Tips(tip = {
		"멀리서 날아오는 화살, 더 이상 맞을 필요가 없습니다. 모두 튕겨내",
		"추가 체력으로 만들어버리세요. 화살 뿐만 아니라 관통 화살, 스나이퍼,",
		"로렘이 발사하는 파티클로 된 발사체도 튕겨낼 수 있습니다. 직접",
		"튕겨내기 귀찮다면 액티브 스킬을 이용해 일정 시간동안 자동으로",
		"튕겨낼 수도 있습니다."
}, strong = {
		@Description(subject = "발사체 돌려보내기", explain = {
				"똑똑, 화살 도착했습니다.",
				"원하지 않는 발사체는 모두 상대에게 다시 돌려보내세요."
		})
}, weak = {
		@Description(subject = "너무 빠른 발사체", explain = {
				"너무 빠른 발사체, 예를 들어 스나이퍼의 풀차지 공격은 막아내기",
				"어렵습니다."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.THREE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.SEVEN), difficulty = Difficulty.HARD)
public class Flector extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Flector.class, "cooldown", 40,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Flector.class, "duration", 7,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 2;
		}

	};

	public Flector(Participant participant) {
		super(participant);
	}

	private static final Set<Material> materials = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);

	private static final double particleRadius = 5;
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 20, cooldownTimer) {
		private int particle;
		@Override
		protected void onDurationStart() {
			particle = 0;
		}
		@Override
		protected void onDurationProcess(int count) {
			if (particle < 30) {
				if (count % 5 == 0) {
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
				}
				final double radians = 0.10471975511965977461542144610932 * particle, sin = FastMath.sin(radians), y = particleRadius * FastMath.cos(radians);
				final Location playerLocation = getPlayer().getLocation().clone().add(0, y, 0);
				for (double phi = 0; phi < 6.283185307179586476925286766559; phi += 0.25132741228718345907701147066236) {
					final double x = particleRadius * sin * FastMath.cos(phi), z = particleRadius * sin * FastMath.sin(phi);
					ParticleLib.SWEEP_ATTACK.spawnParticle(playerLocation.add(x, 0, z));
					playerLocation.subtract(x, 0, z);
				}
				particle++;
			}
			for (Entity entity : LocationUtil.getNearbyEntities(Entity.class, getPlayer().getLocation(), 8, 8, null)) {
				deflect(entity, false);
			}
			for (Deflectable deflectable : LocationUtil.getNearbyCustomEntities(Deflectable.class, getPlayer().getLocation(), 8, 8, null)) {
				deflect(deflectable, false);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getItem() != null && materials.contains(e.getItem().getType())) {
			if (!deflect(LocationUtil.getEntityLookingAt(Entity.class, getPlayer(), 5, .75, null), true)) {
				deflect(LocationUtil.getCustomEntityLookingAt(Deflectable.class, getGame(), getPlayer(), 5, .75,  null), true);
			}
		}
	}

	private boolean deflect(Entity entity, boolean playerDirection) {
		if (entity == null) return false;
		if (entity instanceof Projectile) {
			return deflect((Projectile) entity, playerDirection);
		} else {
			final List<MetadataValue> metadatas = entity.getMetadata("deflectable");
			if (!metadatas.isEmpty()) {
				final Object value = metadatas.get(0).value();
				if (value instanceof Deflectable) {
					return deflect((Deflectable) value, playerDirection);
				}
			}
		}
		return false;
	}

	private boolean deflect(Projectile projectile, boolean playerDirection) {
		if (projectile != null && !projectile.isOnGround() && projectile.isValid() && !getPlayer().equals(projectile.getShooter())) {
			if (projectile.hasMetadata("flector")) {
				if (getPlayer().getUniqueId().equals(projectile.getMetadata("flector").get(0).value())) {
					return false;
				} else {
					projectile.removeMetadata("flector", AbilityWar.getPlugin());
				}
			}
			projectile.setVelocity(playerDirection ? getPlayer().getLocation().getDirection().multiply(2.2 * NMS.getAttackCooldown(getPlayer())) : projectile.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().add(projectile.getVelocity().multiply(-1)));
			projectile.setMetadata("flector", new FixedMetadataValue(AbilityWar.getPlugin(), getPlayer().getUniqueId()));
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
			ParticleLib.SWEEP_ATTACK.spawnParticle(projectile.getLocation());
			if (projectile.getShooter() instanceof Player)
				NMS.setAbsorptionHearts(getPlayer(), Math.min(NMS.getAbsorptionHearts(getPlayer()) + 2, 10));
			return true;
		}
		return false;
	}

	private boolean deflect(Deflectable deflectable, boolean playerDirection) {
		if (deflectable != null && !getPlayer().equals(deflectable.getShooter())) {
			deflectable.onDeflect(getParticipant(), playerDirection ? getPlayer().getLocation().getDirection().multiply(2.2 * NMS.getAttackCooldown(getPlayer())) : deflectable.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().add(deflectable.getDirection().multiply(-1)));
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
			ParticleLib.SWEEP_ATTACK.spawnParticle(deflectable.getLocation());
			if (deflectable.getShooter() instanceof Player)
				NMS.setAbsorptionHearts(getPlayer(), Math.min(NMS.getAbsorptionHearts(getPlayer()) + 2, 10));
			return true;
		}
		return false;
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown() && !skill.isDuration()) {
			skill.start();
		}
		return false;
	}
}
