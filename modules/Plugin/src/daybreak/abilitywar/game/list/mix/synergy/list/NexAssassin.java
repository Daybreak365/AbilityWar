package daybreak.abilitywar.game.list.mix.synergy.list;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "암흑 암살자", rank = Rank.S, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 주변의 생명체들을 끌고 공중으로 올라가 각각 4번씩 공격한 후",
		"바라보는 방향으로 날아가 내려 찍으며 주변의 플레이어들에게",
		"대미지를 입히고 날려보냅니다. $[CooldownConfig]"
})
public class NexAssassin extends Synergy implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(NexAssassin.class, "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> NexDamageConfig = synergySettings.new SettingObject<Integer>(NexAssassin.class, "NexDamage", 20, "# 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DistanceConfig = synergySettings.new SettingObject<Integer>(NexAssassin.class, "Distance", 10,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value > 0;
		}

	};

	public static final SettingObject<Integer> TeleportCountConfig = synergySettings.new SettingObject<Integer>(NexAssassin.class, "TeleportCount", 6,
			"# 능력 사용 시 텔레포트 횟수") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public static final SettingObject<Integer> DamageConfig = synergySettings.new SettingObject<Integer>(NexAssassin.class, "AssassinDamage", 9,
			"# 스킬 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final int damage = DamageConfig.getValue();
	private final int distance = DistanceConfig.getValue();
	private final Timer fallBlockTimer = new Timer(5) {

		Location center;

		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run(int count) {
			int distance = 6 - count;

			if (ServerVersion.getVersionNumber() >= 13) {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, false)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			} else {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, false)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), block.getData(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			}

			for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if (!damageable.equals(getPlayer())) {
					damageable.setVelocity(center.toVector().subtract(damageable.getLocation().toVector()).multiply(-1).setY(0.6));
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 4);
	private Map<Damageable, Vector> entities = null;
	private final Timer follow = new Timer() {
		@Override
		protected void run(int count) {
			if (entities != null) {
				for (Entry<Damageable, Vector> entry : entities.entrySet()) {
					Damageable entity = entry.getKey();
					Vector diff = entry.getValue();
					entity.setVelocity(getPlayer().getLocation().toVector().add(diff).subtract(entity.getLocation().toVector()).multiply(0.7));
					entity.setFallDistance(0);
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	private boolean noFallDamage = false;
	private boolean skillEnabled = false;
	private final Timer assassinSkill = new Timer() {

		private LinkedList<Damageable> damageables;
		private int assassin;

		@Override
		public void onStart() {
			assassin = 3;
			if (entities != null) {
				damageables = new LinkedList<>(entities.keySet());
				if (ServerVersion.getVersionNumber() >= 10) {
					for (Damageable entity : entities.keySet()) {
						entity.setGravity(false);
					}
				}
			}
		}

		@Override
		public void run(int count) {
			if (damageables != null) {
				if (!damageables.isEmpty()) {
					Damageable e = damageables.remove();
					getPlayer().teleport(e.getLocation().clone().setDirection(getPlayer().getLocation().getDirection()));
					e.damage(damage, getPlayer());
					if (e instanceof LivingEntity) ((LivingEntity) e).setNoDamageTicks(0);
					SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer());
					SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
				} else {
					if (assassin != 0) {
						for (Damageable damageable : new ArrayList<>(entities.keySet())) {
							if (damageable.isDead()) {
								entities.remove(damageable);
							}
						}
						damageables = new LinkedList<>(entities.keySet());
						assassin--;
					} else {
						stop(false);
					}
				}
			}
		}

		@Override
		public void onEnd() {
			if (entities != null && ServerVersion.getVersionNumber() >= 10) {
				for (Damageable entity : entities.keySet()) {
					entity.setGravity(true);
				}
			}
			skillEnabled = true;
			Vector playerDirection = getPlayer().getLocation().getDirection();
			getPlayer().setVelocity(getPlayer().getVelocity().add(playerDirection.normalize().multiply(8).setY(-4)));
		}

		@Override
		public void onSilentEnd() {
			if (entities != null && ServerVersion.getVersionNumber() >= 10) {
				for (Damageable entity : entities.keySet()) {
					entity.setGravity(true);
				}
			}
			follow.stop(false);
		}

	}.setPeriod(TimeUnit.TICKS, 3);
	private final Timer nexSkill = new Timer(4) {

		@Override
		public void onStart() {
			follow.start();
			noFallDamage = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, 4, 0)));
		}

		@Override
		public void run(int count) {
		}

		@Override
		public void onEnd() {
			assassinSkill.start();
		}

		@Override
		public void onSilentEnd() {
			follow.stop(false);
		}

	}.setPeriod(TimeUnit.TICKS, 10);

	public NexAssassin(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!nexSkill.isRunning() && !assassinSkill.isRunning() && !cooldownTimer.isCooldown()) {
					this.entities = new HashMap<>();
					for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(getPlayer(), distance, distance)) {
						entities.put(damageable, damageable.getLocation().toVector().subtract(getPlayer().getLocation().toVector()));
					}
					if (entities.size() > 0) {
						for (Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
							SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
						}
						SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
						nexSkill.start();
						cooldownTimer.start();
						return true;
					} else {
						getPlayer().sendMessage("§f" + distance + "칸 이내에 §a엔티티§f가 존재하지 않습니다.");
					}
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getEntity().equals(getPlayer())) {
				if (noFallDamage) {
					if (e.getCause().equals(DamageCause.FALL)) {
						e.setCancelled(true);
						noFallDamage = false;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			if (skillEnabled) {
				Block b = getPlayer().getLocation().getBlock();
				Block db = getPlayer().getLocation().subtract(0, 1, 0).getBlock();

				if (!b.getType().equals(Material.AIR) || !db.getType().equals(Material.AIR)) {
					skillEnabled = false;
					final double damage = NexDamageConfig.getValue();
					for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer(), 5, 5)) {
						if (d instanceof Player) SoundLib.ENTITY_GENERIC_EXPLODE.playSound((Player) d);
						d.damage(damage, getPlayer());
					}
					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());

					fallBlockTimer.start();
					follow.stop(false);
				}
			}
		}
	}

}
