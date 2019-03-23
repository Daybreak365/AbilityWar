package Marlang.AbilityWar.Game.Games.Mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityList;
import Marlang.AbilityWar.Config.AbilityWarSettings;
import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Games.GameManifest;
import Marlang.AbilityWar.Game.Manager.InfiniteDurability;
import Marlang.AbilityWar.Game.Script.Script;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 게임 관리 클래스
 * @author _Marlang 말랑
 */
@GameManifest(Name = "게임", Description = { "§f능력자 전쟁 플러그인의 기본 게임입니다." })
public class DefaultGame extends AbstractGame {
	
	private final static List<String> messages = new ArrayList<String>();
	
	public static void registerMessage(String... msg) {
		for(String m : msg) {
			messages.add(m);
		}
	}
	
	public DefaultGame() {
		setRestricted(Invincible);
		registerEvent(EntityDamageEvent.class);
	}
	
	private boolean Invincible = AbilityWarSettings.getInvincibilityEnable();
	
	private final InfiniteDurability infiniteDurability = new InfiniteDurability();
	
	private final TimerBase NoHunger = new TimerBase() {
		
		@Override
		public void onStart() {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a배고픔 무제한이 적용됩니다."));
		}
		
		@Override
		public void TimerProcess(Integer Seconds) {
			for(Participant p : getParticipants()) {
				p.getPlayer().setFoodLevel(19);
			}
		}
		
		@Override
		public void onEnd() {}
	};
	
	@Override
	protected boolean gameCondition() {
		return true;
	}
	
	@Override
	protected void progressGame(Integer Seconds) {
		switch(Seconds) {
			case 1:
				broadcastPlayerList();
				if(getParticipants().size() < 1) {
					AbilityWarThread.stopGame();
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 참가자 수를 충족하지 못하여 게임을 중지합니다."));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				if(AbilityWarSettings.getDrawAbility()) {
					broadcastAbilityReady();
				} else {
					this.setSeconds(this.getSeconds() + 4);
				}
				break;
			case 13:
				if(AbilityWarSettings.getDrawAbility()) {
					//능력 할당 시작
					this.startAbilitySelect();
				}
				break;
			case 15:
				if(AbilityWarSettings.getDrawAbility()) {
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f모든 참가자가 능력을 &b확정&f했습니다."));
				} else {
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f능력자 게임 설정에 따라 &b능력&f을 추첨하지 않습니다."));
				}
				break;
			case 17:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e잠시 후 게임이 시작됩니다."));
				break;
			case 20:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c5&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 21:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c4&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 22:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c3&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 23:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c2&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 24:
				Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&e게임이 &c1&e초 후에 시작됩니다."));
				SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
				break;
			case 25:
				GameStart();
				break;
		}
	}
	
	@Override
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player Victim = e.getEntity();
		Player Killer = Victim.getKiller();
		if(Victim.getLastDamageCause() != null) {
			DamageCause Cause = Victim.getLastDamageCause().getCause();

			if(Killer != null) {
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&a" + Killer.getName() + "&f님이 &c" + Victim.getName() + "&f님을 죽였습니다."));
			} else {
				if(Cause.equals(DamageCause.CONTACT)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 찔려 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALL)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 떨어져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FALLING_BLOCK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 떨어지는 블록에 맞아 죽었습니다."));
				} else if(Cause.equals(DamageCause.SUFFOCATION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 끼여 죽었습니다."));
				} else if(Cause.equals(DamageCause.DROWNING)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 물에 빠져 죽었습니다."));
				} else if(Cause.equals(DamageCause.ENTITY_EXPLOSION)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 폭발하였습니다."));
				} else if(Cause.equals(DamageCause.LAVA)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 용암에 빠져 죽었습니다."));
				} else if(Cause.equals(DamageCause.FIRE) || Cause.equals(DamageCause.FIRE_TICK)) {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 노릇노릇하게 구워졌습니다."));
				} else {
					e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 죽었습니다."));
				}
			}
		} else {
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&c" + Victim.getName() + "&f님이 죽었습니다."));
		}

		if(this.isGameStarted()) {
			if(this.isParticipating(Victim)) {
				if(AbilityWarSettings.getAbilityReveal()) {
					Participant victim = this.getParticipant(Victim);
					if(victim.hasAbility()) {
						AbilityBase Ability = victim.getAbility();
						
						String name = Ability.getName();
						if(name != null) {
							Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + Victim.getName() + "&f님은 &e" + name + " &f능력이었습니다!"));
						}
					} else {
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + Victim.getName() + "&f님은 능력이 없습니다!"));
					}
				}
				
				if(AbilityWarSettings.getEliminate()) {
					getDeathManager().Eliminate(Victim);
				}
			}
		}
	}
	
	public void broadcastPlayerList() {
		int Count = 0;
		
        ArrayList<String> msg = new ArrayList<String>();
		
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==== &e게임 참여자 목록 &6===="));
		for(Participant p : getParticipants()) {
			Count++;
			msg.add(ChatColor.translateAlternateColorCodes('&', "&a" + Count + ". &f" + p.getPlayer().getName()));
		}
		msg.add(ChatColor.translateAlternateColorCodes('&', "&e총 인원수 : " + Count + "명"));
		msg.add(ChatColor.translateAlternateColorCodes('&', "&6==========================="));
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastPluginDescription() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&cAbilityWar &f- &6능력자 전쟁"),
				ChatColor.translateAlternateColorCodes('&', "&e버전 &7: &f" + AbilityWar.getPlugin().getDescription().getVersion()),
				ChatColor.translateAlternateColorCodes('&', "&b개발자 &7: &f_Marlang 말랑"),
				ChatColor.translateAlternateColorCodes('&', "&9디스코드 &7: &f말랑&7#5908"));
		
		for(String m : messages) {
			msg.add(m);
		}
		
		Messager.broadcastStringList(msg);
	}
	
	public void broadcastAbilityReady() {
		ArrayList<String> msg = Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &b" + AbilityList.nameValues().size() + "개&f의 능력이 등록되어 있습니다."),
				ChatColor.translateAlternateColorCodes('&', "&7능력을 무작위로 할당합니다..."));
		
		Messager.broadcastStringList(msg);
	}
	
	public void GameStart() {
		Messager.broadcastStringList(Messager.getStringList(
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"),
				ChatColor.translateAlternateColorCodes('&', "&f                            &cAbilityWar &f- &6능력자 전쟁              "),
				ChatColor.translateAlternateColorCodes('&', "&f                                   게임 시작                            "),
				ChatColor.translateAlternateColorCodes('&', "&e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")));
		
		GiveDefaultKit();
		
		for(Participant p : getParticipants()) {
			if(AbilityWarSettings.getSpawnEnable()) {
				p.getPlayer().teleport(AbilityWarSettings.getSpawnLocation());
			}
		}
		
		if(AbilityWarSettings.getNoHunger()) {
			NoHunger.setPeriod(1);
			NoHunger.StartTimer();
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4배고픔 무제한&c이 적용되지 않습니다."));
		}
		
		if(Invincible) {
			getInvincibility().Start(false);
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4초반 무적&c이 적용되지 않습니다."));
			for(Participant participant : this.getParticipants()) {
				if(participant.hasAbility()) {
					participant.getAbility().setRestricted(false);
				}
			}
		}
		
		if(AbilityWarSettings.getInfiniteDurability()) {
			Bukkit.getPluginManager().registerEvents(infiniteDurability, AbilityWar.getPlugin());
		} else {
			Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4내구도 무제한&c이 적용되지 않습니다."));
		}
		
		for(World w : Bukkit.getWorlds()) {
			if(AbilityWarSettings.getClearWeather()) {
				w.setStorm(false);
			}
			
			if(AbilityWarSettings.getItemDrop()) {
				w.setGameRuleValue("keepInventory", "false");
			} else {
				w.setGameRuleValue("keepInventory", "true");
			}
		}
		
		Script.RunAll(this);
		
		setGameStarted(true);
	}
	
	/**
	 * 기본 킷 유저 지급
	 */
	@Override
	public void GiveDefaultKit(Player p) {
		List<ItemStack> DefaultKit = AbilityWarSettings.getDefaultKit();

		if(AbilityWarSettings.getInventoryClear()) {
			p.getInventory().clear();
			p.updateInventory();
		}
		
		for(ItemStack is : DefaultKit) {
			p.getInventory().addItem(is);
		}
		
		p.setLevel(0);
		if(AbilityWarSettings.getStartLevel() > 0) {
			p.giveExpLevels(AbilityWarSettings.getStartLevel());
			SoundLib.ENTITY_PLAYER_LEVELUP.playSound(p);
		}
	}
	
	@Override
	protected List<Player> setupPlayers() {
		List<Player> Players = new ArrayList<Player>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!isSpectator(p.getName())) {
				Players.add(p);
			}
		}
		
		return Players;
	}
	
	@Override
	protected AbilitySelect setupAbilitySelect() {
		return new AbilitySelect() {
			
			@Override
			protected List<Participant> setupPlayers() {
				return DefaultGame.this.getParticipants();
			}
			
			private List<Class<? extends AbilityBase>> setupAbilities() {
				List<Class<? extends AbilityBase>> list = new ArrayList<>();
				for(String abilityName : AbilityList.nameValues()) {
					if(!AbilityWarSettings.isBlackListed(abilityName)) {
						list.add(AbilityList.getByString(abilityName));
					}
				}
				
				return list;
			}
			
			private List<Class<? extends AbilityBase>> abilities;
			
			@Override
			protected void drawAbility() {
				abilities = setupAbilities();
				
				if(getSelectors().size() <= abilities.size()) {
					Random random = new Random();
					
					for(Participant participant : getSelectors()) {
						Player p = participant.getPlayer();
						
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							participant.setAbility(abilityClass);
							abilities.remove(abilityClass);
							
							Messager.sendStringList(p, Messager.getStringList(
									ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 능력을 변경할 수 있습니다.")));
						} catch (Exception e) {
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."));
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력의 수가 참가자의 수보다 적어 게임을 종료합니다.");
					AbilityWarThread.stopGame();
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
				}
			}
			
			@Override
			protected boolean changeAbility(Participant participant) {
				Player p = participant.getPlayer();
				
				if(abilities.size() > 0) {
					Random random = new Random();
					
					if(participant.hasAbility()) {
						Class<? extends AbilityBase> oldAbilityClass = participant.getAbility().getClass();
						Class<? extends AbilityBase> abilityClass = abilities.get(random.nextInt(abilities.size()));
						try {
							abilities.remove(abilityClass);
							abilities.add(oldAbilityClass);
							
							participant.setAbility(abilityClass);
							
							return true;
						} catch (Exception e) {
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님의 능력을 변경하는 도중 오류가 발생하였습니다."));
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}
				
				return false;
			}

			@Override
			protected void onSelectEnd() {}

			@Override
			protected int getChangeCount() {
				return 1;
			}

		};
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			
			if(e.getEntity() instanceof Player) {
				if(this.getInvincibility().isInvincible()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(infiniteDurability);
	}
	
}
