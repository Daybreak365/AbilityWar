package Marlang.AbilityWar.GameManager.Game;

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
import Marlang.AbilityWar.GameManager.Manager.AbilitySelect;
import Marlang.AbilityWar.GameManager.Manager.InfiniteDurability;
import Marlang.AbilityWar.GameManager.Manager.Invincibility;
import Marlang.AbilityWar.GameManager.Object.Participant;
import Marlang.AbilityWar.GameManager.Script.Script;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;
import Marlang.AbilityWar.Utils.Thread.TimerBase;

/**
 * 게임 관리 클래스
 * @author _Marlang 말랑
 */
public class Game extends AbstractGame {
	
	private static List<String> messages = new ArrayList<String>();
	
	public static void registerMessage(String... msg) {
		for(String m : msg) {
			messages.add(m);
		}
	}
	
	public Game() {
		setRestricted(Invincible);
		registerEvent(EntityDamageEvent.class);
	}
	
	private boolean Invincible = AbilityWarSettings.getInvincibilityEnable();
	
	private InfiniteDurability infiniteDurability = new InfiniteDurability();
	
	private Invincibility invincibility = new Invincibility(this);
	
	TimerBase NoHunger = new TimerBase() {
		
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
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c최소 플레이어 수를 충족하지 못하여 게임을 중지합니다."));
				}
				break;
			case 5:
				broadcastPluginDescription();
				break;
			case 10:
				if(AbilityWarSettings.getDrawAbility()) {
					broadcastAbilityReady();
				} else {
					this.Seconds += 4;
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
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f모든 플레이어가 능력을 &b확정&f했습니다."));
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
			if(Participant.checkParticipant(Victim) && this.isParticipating(Victim)) {
				if(AbilityWarSettings.getAbilityReveal()) {
					Participant participant = Participant.Construct(this, Victim);
					if(participant.hasAbility()) {
						AbilityBase Ability = participant.getAbility();
						Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f[&c능력&f] &c" + Victim.getName() + "&f님은 &e" + Ability.getAbilityName() + " &f능력이었습니다!"));
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
				ChatColor.translateAlternateColorCodes('&', "&f플러그인에 총 &b" + AbilityList.values().size() + "개&f의 능력이 등록되어 있습니다."),
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
			invincibility.StartTimer();
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
		
		Script.RunAll();
		
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
	
	public Invincibility getInvincibility() {
		return invincibility;
	}
	
	@Override
	protected List<Participant> setupParticipants() {
		List<Participant> Participants = new ArrayList<Participant>();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!getSpectators().contains(p.getName())) {
				Participants.add(Participant.Construct(this, p));
			}
		}
		
		return Participants;
	}
	
	@Override
	protected AbilitySelect setupAbilitySelect() {
		return new AbilitySelect() {
			
			@Override
			protected List<Participant> setupPlayers() {
				return Game.this.getParticipants();
			}
			
			@Override
			protected List<Class<? extends AbilityBase>> setupAbilities() {
				List<Class<? extends AbilityBase>> list = new ArrayList<>();
				for(String name : AbilityList.values()) {
					if(!AbilityWarSettings.getBlackList().contains(name)) {
						list.add(AbilityList.getByString(name));
					}
				}
				
				return list;
			}
			
			@Override
			protected void drawAbility() {
				if(getSelectors().size() <= Abilities.size()) {
					Random random = new Random();
					
					for(Participant participant : getSelectors()) {
						Player p = participant.getPlayer();
						
						Class<? extends AbilityBase> abilityClass = Abilities.get(random.nextInt(Abilities.size()));
						try {
							participant.setAbility(abilityClass);
							Abilities.remove(abilityClass);
							
							Messager.sendStringList(p, Messager.getStringList(
									ChatColor.translateAlternateColorCodes('&', "&a당신에게 능력이 할당되었습니다. &e/ability check&f로 확인 할 수 있습니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability yes &f명령어를 사용하면 능력을 확정합니다."),
									ChatColor.translateAlternateColorCodes('&', "&e/ability no &f명령어를 사용하면 1회에 한해 능력을 변경할 수 있습니다.")));
						} catch (Exception e) {
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 할당하는 도중 오류가 발생하였습니다."));
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.broadcastErrorMessage("사용 가능한 능력의 수가 플레이어의 수보다 적어 게임을 종료합니다.");
					AbilityWarThread.stopGame();
					Messager.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7게임이 초기화되었습니다."));
				}
			}
			
			@Override
			public void changeAbility(Participant participant) {
				Player p = participant.getPlayer();
				
				if(Abilities.size() > 0) {
					Random random = new Random();
					
					if(participant.hasAbility()) {
						Class<? extends AbilityBase> oldAbilityClass = participant.getAbility().getClass();
						Class<? extends AbilityBase> abilityClass = Abilities.get(random.nextInt(Abilities.size()));
						try {
							Abilities.remove(abilityClass);
							Abilities.add(oldAbilityClass);
							
							participant.setAbility(abilityClass);
							
							Messager.sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&a당신의 능력이 변경되었습니다. &e/ability check&f로 확인 할 수 있습니다."));
							
							decideAbility(participant, false);
						} catch (Exception e) {
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + p.getName() + "&f님에게 능력을 변경하는 도중 오류가 발생하였습니다."));
							Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f문제가 발생한 능력: &b" + abilityClass.getName()));
						}
					}
				} else {
					Messager.sendErrorMessage(p, "능력을 변경할 수 없습니다.");
				}
			}

			@Override
			protected boolean endCondition() {
				for(Participant Key : getSelectors()) {
					if(!hasDecided(Key)) {
						return false;
					}
				}
				
				return true;
			}
			
		};
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if(event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			
			if(e.getEntity() instanceof Player) {
				if(this.getInvincibility().isTimerRunning()) {
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
