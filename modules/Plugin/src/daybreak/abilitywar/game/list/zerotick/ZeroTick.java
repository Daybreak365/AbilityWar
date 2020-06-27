package daybreak.abilitywar.game.list.zerotick;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.manager.object.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

/**
 * 게임 관리 클래스
 *
 * @author Daybreak 새벽
 */
@GameManifest(name = "제로틱", description = {"§f공격 쿨타임 따위는 존재하지 않는 게임 모드!", "§f공격 속도 제한 없이 시원하게 싸워보세요!"})
public class ZeroTick extends Game implements DefaultKitHandler {

	public ZeroTick() {
		super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
		setRestricted(Settings.InvincibilitySettings.isEnabled());
	}

	@Override
	protected void progressGame(int seconds) {
		switch (seconds) {
			case 1:
				List<String> lines = Messager.asList("§6==== §e게임 참여자 목록 §6====");
				int count = 0;
				for (Participant p : getParticipants()) {
					count++;
					lines.add("§a" + count + ". §f" + p.getPlayer().getName());
				}
				lines.add("§e총 인원수 : " + count + "명");
				lines.add("§6==========================");

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}

				if (getParticipants().size() < 1) {
					stop();
					Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
				}
				break;
			case 3:
				lines = Messager.asList(
						"§cZeroTick §f- §6제로틱",
						"§e플러그인 버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
						"§b모드 개발자 §7: §fDaybreak 새벽",
						"§9디스코드 §7: §f새벽§7#5908"
				);

				GameCreditEvent event = new GameCreditEvent();
				Bukkit.getPluginManager().callEvent(event);
				lines.addAll(event.getCreditList());

				for (String line : lines) {
					Bukkit.broadcastMessage(line);
				}
				break;
			case 5:
				if (Settings.getDrawAbility()) {
					for (String line : Messager.asList(
							"§f플러그인에 총 §b" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.",
							"§7능력을 무작위로 할당합니다...")) {
						Bukkit.broadcastMessage(line);
					}
					try {
						startAbilitySelect();
					} catch (OperationNotSupportedException ignored) {
					}
				}
				break;
			case 6:
				if (Settings.getDrawAbility()) {
					Bukkit.broadcastMessage("§f모든 참가자가 능력을 §b확정§f했습니다.");
				} else {
					Bukkit.broadcastMessage("§f능력자 게임 설정에 따라 §b능력§f을 추첨하지 않습니다.");
				}
				break;
			case 8:
				Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
				break;
			case 10:
				Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 11:
				Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 12:
				Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 13:
				Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 14:
				Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 15:
				for (String line : Messager.asList(
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
						"§f                §cZeroTick §f- §6제로틱       ",
						"§f                    게임 시작                ",
						"§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
					Bukkit.broadcastMessage(line);
				}

				giveDefaultKit(getParticipants());

				if (Settings.getSpawnEnable()) {
					Location spawn = Settings.getSpawnLocation();
					for (Participant participant : getParticipants()) {
						participant.getPlayer().teleport(spawn);
					}
				}

				if (Settings.getNoHunger()) {
					Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
				} else {
					Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getInfiniteDurability()) {
					attachObserver(new InfiniteDurability());
				} else {
					Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
				}

				if (Settings.getClearWeather()) {
					for (World w : Bukkit.getWorlds()) {
						w.setStorm(false);
					}
				}

				if (isRestricted()) {
					getInvincibility().start(false);
				} else {
					Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
					setRestricted(false);
				}

				new GameTimer(TaskType.INFINITE, -1) {
					@Override
					protected void run(int count) {
						for (World world : Bukkit.getWorlds()) {
							for (LivingEntity entity : world.getLivingEntities()) {
								entity.setNoDamageTicks(0);
							}
						}
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
				Bukkit.broadcastMessage("§a공격 쿨타임이 §f제로틱§a으로 변경되었습니다.");

				ScriptManager.runAll(this);

				startGame();
				break;
		}
	}

	/*
	private static final ItemStack ARROW = new ItemStack(Material.ARROW);
	@EventHandler
	private void onInteract(PlayerInteractEvent e) {
		if (e.getItem() != null && (e.getItem().getType().equals(Material.BOW))) {
			e.setCancelled(true);
			Projectile projectile = e.getPlayer().launchProjectile(Arrow.class, e.getPlayer().getLocation().getDirection().multiply(2));
			try {
				Constructor<EntityShootBowEvent> constructor = EntityShootBowEvent.class.getConstructor(LivingEntity.class, ItemStack.class, ItemStack.class, Projectile.class, float.class);
				EntityShootBowEvent event = constructor.newInstance(e.getPlayer(), e.getItem(), ARROW,  projectile, 5.0f);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) projectile.remove();
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
				ex.printStackTrace();
			}
		}
	}*/

}
