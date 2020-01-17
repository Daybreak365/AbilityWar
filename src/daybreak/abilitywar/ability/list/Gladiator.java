package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.item.MaterialLib;
import daybreak.abilitywar.utils.math.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Random;

@AbilityManifest(Name = "글래디에이터", Rank = Rank.S, Species = Species.HUMAN)
public class Gladiator extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Gladiator.class, "Cooldown", 120,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public Gladiator(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f상대방을 철괴로 우클릭하면 투기장이 생성되며 그 안에서"),
				ChatColor.translateAlternateColorCodes('&', "&f1:1 대결을 하게 됩니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final HashMap<Block, BlockState> Saves = new HashMap<>();

	private final Timer FieldClear = new Timer(20) {

		@Override
		public void onStart() {
		}

		@Override
		public void onProcess(int count) {
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4[&c투기장&4] &f" + count + "초 후에 투기장이 삭제됩니다."));
			getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4[&c투기장&4] &f" + count + "초 후에 투기장이 삭제됩니다."));
		}

		@Override
		public void onEnd() {
			for (Block b : Saves.keySet()) {
				BlockState state = Saves.get(b);
				b.setType(state.getType());
			}

			Saves.clear();
		}

		@Override
		public void onSilentEnd() {
			for (Block b : Saves.keySet()) {
				BlockState state = Saves.get(b);
				b.setType(state.getType());
			}

			Saves.clear();
		}

	};

	private Player target = null;
	private final Random random = new Random();

	private final Timer Field = new Timer(26) {

		int buildCount;
		int TotalCount;
		Location center;

		@Override
		public void onStart() {
			buildCount = 1;
			TotalCount = 1;
			center = getPlayer().getLocation();
			Saves.putIfAbsent(center.clone().subtract(0, 1, 0).getBlock(), center.clone().subtract(0, 1, 0).getBlock().getState());
			center.subtract(0, 1, 0).getBlock().setType(MaterialLib.STONE_BRICKS.getMaterial());
		}

		@Override
		public void onProcess(int count) {
			if (TotalCount <= 10) {
				for (Block b : LocationUtil.getBlocks2D(center, buildCount, true, false)) {
					Saves.putIfAbsent(b, b.getState());
					if (random.nextInt(5) <= 1) {
						MaterialLib.STONE_BRICKS.setType(b);
					} else {
						MaterialLib.MOSSY_STONE_BRICKS.setType(b);
					}
				}
				for (LivingEntity livingEntity : LocationUtil.getNearbyEntities(LivingEntity.class, center, buildCount, 6)) {
					if (!getPlayer().equals(livingEntity) && !target.equals(livingEntity)) {
						livingEntity.setVelocity(livingEntity.getLocation().toVector().clone().subtract(center.toVector()).normalize());
					}
				}

				buildCount++;
			} else if (TotalCount <= 15) {
				for (Block b : LocationUtil.getBlocks2D(center, buildCount - 2, true, false)) {
					Location bLocation = b.getLocation();
					Saves.putIfAbsent(bLocation.clone().add(0, TotalCount - 10, 0).getBlock(), bLocation.clone().add(0, TotalCount - 10, 0).getBlock().getState());
					bLocation.add(0, TotalCount - 10, 0).getBlock().setType(MaterialLib.IRON_BARS.getMaterial());
				}

				for (Block b : LocationUtil.getBlocks2D(center, buildCount - 1, true, false)) {
					Location bLocation = b.getLocation();
					Saves.putIfAbsent(bLocation.clone().add(0, TotalCount - 10, 0).getBlock(), bLocation.clone().add(0, TotalCount - 10, 0).getBlock().getState());
					bLocation.add(0, TotalCount - 10, 0).getBlock().setType(MaterialLib.IRON_BARS.getMaterial());
				}
			} else if (TotalCount <= 26) {
				for (Block b : LocationUtil.getBlocks2D(center, buildCount, true, false)) {
					Location l = b.getLocation();
					Saves.putIfAbsent(l.clone().add(0, 6, 0).getBlock(), l.clone().add(0, 6, 0).getBlock().getState());
					if (random.nextInt(5) <= 1) {
						MaterialLib.STONE_BRICKS.setType(l.add(0, 6, 0).getBlock());
					} else {
						MaterialLib.MOSSY_STONE_BRICKS.setType(l.add(0, 6, 0).getBlock());
					}
				}

				buildCount--;
			}
			TotalCount++;
		}

		@Override
		public void onEnd() {
			Location check = center.clone().add(0, 6, 0);

			if (!check.getBlock().getType().equals(MaterialLib.STONE_BRICKS.getMaterial())) {
				Saves.putIfAbsent(check.getBlock(), check.getBlock().getState());
				check.getBlock().setType(MaterialLib.STONE_BRICKS.getMaterial());
			}

			Location teleport = center.clone().add(0, 1, 0);

			getPlayer().teleport(teleport);
			PotionEffects.ABSORPTION.addPotionEffect(getPlayer(), 400, 2, true);
			PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 400, 0, true);
			target.teleport(teleport);

			FieldClear.startTimer();
		}

	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		return false;
	}

	@SubscribeEvent
	public void onBlockBreak(BlockBreakEvent e) {
		if (Saves.keySet().contains(e.getBlock())) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c투기장&f은 부술 수 없습니다!"));
		}
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (entity != null) {
				if (entity instanceof Player) {
					if (!cooldownTimer.isCooldown()) {
						this.target = (Player) entity;
						Field.startTimer();

						cooldownTimer.startTimer();
					}
				}
			} else {
				cooldownTimer.isCooldown();
			}
		}
	}

}
