package DayBreak.AbilityWar.Ability.List;

import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import DayBreak.AbilityWar.Ability.AbilityBase;
import DayBreak.AbilityWar.Ability.AbilityManifest;
import DayBreak.AbilityWar.Ability.AbilityManifest.Rank;
import DayBreak.AbilityWar.Ability.Timer.CooldownTimer;
import DayBreak.AbilityWar.Config.AbilitySettings.SettingObject;
import DayBreak.AbilityWar.Game.Games.AbstractGame.Participant;
import DayBreak.AbilityWar.Utils.FallBlock;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.Library.ParticleLib;
import DayBreak.AbilityWar.Utils.Library.SoundLib;
import DayBreak.AbilityWar.Utils.Math.LocationUtil;
import DayBreak.AbilityWar.Utils.Thread.TimerBase;
import DayBreak.AbilityWar.Utils.VersionCompat.ServerVersion;

@AbilityManifest(Name = "�ؽ�", Rank = Rank.B)
public class Nex extends AbilityBase {

	public static SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Nex.class, "Cooldown", 120, "# ��Ÿ��") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static SettingObject<Integer> DamageConfig = new SettingObject<Integer>(Nex.class, "Damage", 8, "# ������") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}

	};
	
	public Nex(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&fö���� ��Ŭ���ϸ� �������� �ö󰬴ٰ� �ٴ����� ���� ������"),
				ChatColor.translateAlternateColorCodes('&', "�ֺ��� �÷��̾�鿡�� �������� �����ϴ�. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	CooldownTimer Cool = new CooldownTimer(this, CooldownConfig.getValue());

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.Iron_Ingot)) {
			if (ct.equals(ClickType.RightClick)) {
				if(!Cool.isCooldown()) {
					for(Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
					}
					SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
					Skill.StartTimer();
					
					Cool.StartTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	boolean NoFall = false;
	boolean RunSkill = false;

	TimerBase Skill = new TimerBase(4) {

		@Override
		public void onStart() {
			NoFall = true;
			Vector v = new Vector(0, 4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

		@Override
		public void TimerProcess(Integer Seconds) {
		}

		@Override
		public void onEnd() {
			RunSkill = true;
			Vector v = new Vector(0, -4, 0);

			getPlayer().setVelocity(getPlayer().getVelocity().add(v));
		}

	}.setPeriod(10);
	
	Integer Damage = DamageConfig.getValue();
	
	@Override
	public void PassiveSkill(Event event) {
		if (event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if (e.getEntity() instanceof Player) {
				if(e.getEntity().equals(getPlayer())) {
					if (NoFall) {
						if (e.getCause().equals(DamageCause.FALL)) {
							e.setCancelled(true);
							NoFall = false;
						}
					}
				}
			}
		} else if(event instanceof PlayerMoveEvent) {
			PlayerMoveEvent e = (PlayerMoveEvent) event;
			if(e.getPlayer().equals(getPlayer())) {
				if(RunSkill) {
					Block b = getPlayer().getLocation().getBlock();
					Block db = getPlayer().getLocation().subtract(0, 1, 0).getBlock();
					
					if(!b.getType().equals(Material.AIR) || !db.getType().equals(Material.AIR)) {
						RunSkill = false;
						for(Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
							SoundLib.ENTITY_GENERIC_EXPLODE.playSound(player);
							player.damage(Damage, getPlayer());
						}
						SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
						
						if(!db.getType().equals(Material.AIR)) {
							if(ServerVersion.getVersion() >= 13) {
								try {
									Method method = Material.class.getDeclaredMethod("createBlockData");
									Object BlockData = method.invoke(db.getType());
									ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, Class.forName("org.bukkit.block.data.BlockData").cast(BlockData));
								} catch(Exception ex) {}
							} else {
								ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, new MaterialData(db.getType()));
							}
						} else {
							if(ServerVersion.getVersion() >= 13) {
								try {
									Method method = Material.class.getDeclaredMethod("createBlockData");
									Object BlockData = method.invoke(b.getType());
									ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, Class.forName("org.bukkit.block.data.BlockData").cast(BlockData));
								} catch(Exception ex) {}
							} else {
								ParticleLib.BLOCK_CRACK.spawnParticle(getPlayer().getLocation(), 30, 2, 2, 2, new MaterialData(b.getType()));
							}
						}
						
						FallBlock.StartTimer();
					}
				}
			}
		}
	}
	
	TimerBase FallBlock = new TimerBase(5) {
		
		Location center;
		
		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			Integer Distance = 6 - Seconds;
			
			for(Block block : LocationUtil.getBlocks(center, Distance, true, true, false)) {
				FallBlock fb = new FallBlock(block.getType(), block.getLocation().add(0, 1, 0), new Vector(0, 0.5, 0)) {
					
					@Override
					public void onChangeBlock(FallingBlock block) {}
					
				};
				
				fb.Spawn(false);
			}
			
			for(Damageable e : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if(!e.equals(getPlayer())) {
					e.setVelocity(center.toVector().subtract(e.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}
		
		@Override
		public void onEnd() {}
		
	}.setPeriod(4);
	
	@Override
	public void onRestrictClear() {}

	@Override
	public void TargetSkill(MaterialType mt, Entity entity) {}
	
}