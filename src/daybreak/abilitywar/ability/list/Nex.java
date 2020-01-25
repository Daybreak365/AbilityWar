package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.FallBlock;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import daybreak.abilitywar.utils.versioncompat.ServerVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@AbilityManifest(Name = "넥스", Rank = Rank.B, Species = Species.GOD)
public class Nex extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Nex.class, "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Nex.class, "Damage", 20, "# 대미지") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};

	public Nex(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 공중으로 올라갔다가 바닥으로 내려 찍으며"),
				ChatColor.translateAlternateColorCodes('&', "주변의 플레이어들에게 대미지를 입힙니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (ct.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					for (Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
					}
					SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
					Skill.startTimer();
					cooldownTimer.startTimer();
					return true;
				}
			}
		}

		return false;
	}

	private boolean noFallDamage = false;
	private boolean skillEnabled = false;

	private final Timer Skill = new Timer(4) {

		@Override
		public void onStart() {
			noFallDamage = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, 4, 0)));
		}

		@Override
		public void onProcess(int count) {
		}

		@Override
		public void onEnd() {
			skillEnabled = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, -4, 0)));
		}

	}.setPeriod(10);

	private final int damage = DamageConfig.getValue();

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
					for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer(), 5, 5)) {
						if (d instanceof Player) SoundLib.ENTITY_GENERIC_EXPLODE.playSound((Player) d);
						d.damage(damage, getPlayer());
					}
					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());

					fallBlockTimer.startTimer();
				}
			}
		}
	}

	private final Timer fallBlockTimer = ServerVersion.getVersion() >= 13 ? new Timer(5) {

		Location center;

		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onProcess(int count) {
			int distance = 6 - count;

			for (Block block : LocationUtil.getBlocks2D(center, distance, true, true)) {
				Location location = block.getLocation().add(0, 1, 0);
				new FallBlock(block.getType(), location, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random())) {
					@Override
					public void onChangeBlock(FallingBlock block) {
					}
				}.Spawn();
			}

			for (Damageable e : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if (!e.equals(getPlayer())) {
					e.setVelocity(center.toVector().subtract(e.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}

	}.setPeriod(4) :
			new Timer(5) {

				Location center;

				@Override
				public void onStart() {
					this.center = getPlayer().getLocation();
				}

				@SuppressWarnings("deprecation")
				@Override
				public void onProcess(int count) {
					int distance = 6 - count;

					for (Block block : LocationUtil.getBlocks2D(center, distance, true, true)) {
						Location location = block.getLocation().add(0, 1, 0);
						new FallBlock(block.getType(), location, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random())) {
							@Override
							public void onChangeBlock(FallingBlock block) {
							}
						}.setByteData(block.getData()).Spawn();
					}

					for (Damageable e : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
						if (!e.equals(getPlayer())) {
							e.setVelocity(center.toVector().subtract(e.getLocation().toVector()).multiply(-1).setY(1.2));
						}
					}
				}

			}.setPeriod(4);

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
	}

}
