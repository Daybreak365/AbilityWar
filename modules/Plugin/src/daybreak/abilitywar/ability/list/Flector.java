package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

@AbilityManifest(name = "플렉터", rank = Rank.S, species = Species.HUMAN, explain = {
		"발사체를 바라본 상태로 검을 휘둘러 해당 발사체를",
		"튕겨낼 수 있습니다. 본인이 발사한 발사체는 튕겨낼 수 없으며,",
		"다른 플레이어가가 발사한 발사체를 튕겨낼 경우 §e♥ §f만큼의",
		"§e추가 체력§f을 얻습니다. §e추가 체력§f은 5칸 이상 얻을 수 없습니다.",
		"철괴를 우클릭하면 $[DURATION_CONFIG]초간 주변의 모든 투사체를 튕겨냅니다. $[COOLDOWN_CONFIG]"
})
public class Flector extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Flector.class, "Cool", 40,
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

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Flector.class, "Duration", 7,
			"# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Flector(Participant participant) {
		super(participant);
	}

	private static final Set<Material> materials = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);

	private final CenteredBoundingBox boundingBox = CenteredBoundingBox.of(getPlayer().getLocation(), -1.5, -1.5, -1.5, 1.5, 1.5, 1.5);
	private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
	private final Duration skill = new Duration(DURATION_CONFIG.getValue() * 20, cooldownTimer) {
		@Override
		protected void onDurationProcess(int count) {
			for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, getPlayer().getLocation(), 8, 8, null)) {
				deflect(projectile, false);
			}
			for (Deflectable deflectable : LocationUtil.getNearbyCustomEntities(Deflectable.class, getPlayer().getLocation(), 8, 8, null)) {
				deflect(deflectable, false);
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getItem() != null && materials.contains(e.getItem().getType())) {
			if (!deflect(LocationUtil.getEntityLookingAt(Projectile.class, boundingBox, getPlayer(), 5, null), true)) {
				deflect(LocationUtil.getCustomEntityLookingAt(Deflectable.class, getGame(), boundingBox, getPlayer(), 5, null), true);
			}
		}
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
