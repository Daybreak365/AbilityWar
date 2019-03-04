package Marlang.AbilityWar.GameManager.Object;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.EventExecutor;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Ability.AbilityBase.ClickType;
import Marlang.AbilityWar.Ability.AbilityBase.MaterialType;
import Marlang.AbilityWar.GameManager.Game.AbstractGame;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Validate;
import Marlang.AbilityWar.Utils.VersionCompat.PlayerCompat;

public class Participant implements EventExecutor {

	private static HashMap<String, Participant> participants = new HashMap<String, Participant>();
	private static AbstractGame lastGame;

	/**
	 * 참가자 객체를 반환합니다. 해당 게임에서 이미 대상 플레이어를 기반으로 만들어진 참가자 객체가 있을 경우 반환하고, 없을 경우에는 새로
	 * 참가자 객체를 만들어 반환합니다.
	 * 
	 * @param game   진행중인 게임
	 * @param player 기반 플레이어
	 * @return 참가자 객체
	 */
	public static Participant Construct(AbstractGame game, Player player) {
		Validate.NotNull(game, player);

		if (lastGame != null && !lastGame.equals(game)) {
			participants.clear();
		}

		if (participants.containsKey(player.getUniqueId().toString())) {
			return participants.get(player.getUniqueId().toString());
		} else {
			Participant participant = new Participant(game, player);
			participants.put(player.getUniqueId().toString(), participant);
			return participant;
		}
	}

	/**
	 * 플레이어를 기반으로 만들어진 참가자 객체가 있는지 확인합니다.
	 * 
	 * @param player 확인할 플레이어
	 * @return 참가자 객체 존재 여부
	 */
	public static boolean checkParticipant(Player player) {
		return participants.containsKey(player.getUniqueId().toString());
	}

	private Player player;
	private final AbstractGame game;

	private Participant(AbstractGame game, Player player) {
		this.game = game;
		this.player = player;

		Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
		Bukkit.getPluginManager().registerEvent(EntityDamageByEntityEvent.class, game, EventPriority.HIGH, this, AbilityWar.getPlugin());
	}

	private Instant lastClick = Instant.now();

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerLoginEvent) {
			PlayerLoginEvent e = (PlayerLoginEvent) event;
			if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				this.setPlayer(e.getPlayer());
			}
		} else if (event instanceof PlayerInteractEvent) {
			PlayerInteractEvent e = (PlayerInteractEvent) event;

			Player p = e.getPlayer();
			if (p.equals(getPlayer())) {
				MaterialType mt = parseMaterialType(PlayerCompat.getItemInHand(p).getType());
				ClickType ct = e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? ClickType.RightClick : ClickType.LeftClick;
				if (mt != null) {
					if (hasAbility()) {
						if (!getAbility().isRestricted()) {
							Instant Now = Instant.now();
							long Duration = java.time.Duration.between(lastClick, Now).toMillis();
							if (Duration >= 250) {
								this.lastClick = Now;
								ActiveSkill(getAbility(), mt, ct);

								if (ct.equals(ClickType.LeftClick)) {
									getAbility().TargetSkill(mt, null);
								}
							}
						}
					}
				}
			}
		} else if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			
			if(e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				if (p.equals(getPlayer())) {
					MaterialType mt = parseMaterialType(PlayerCompat.getItemInHand(p).getType());
					if(mt != null) {
						if(!e.isCancelled()) {
							if(this.hasAbility()) {
								AbilityBase Ability = this.getAbility();
								if(!Ability.isRestricted()) {
									Instant Now = Instant.now();
									long Duration = java.time.Duration.between(lastClick, Now).toMillis();
									if (Duration >= 250) {
										this.lastClick = Now;
										Ability.TargetSkill(mt, e.getEntity());
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void ActiveSkill(AbilityBase Ability, MaterialType mt, ClickType ct) {
		boolean Executed = Ability.ActiveSkill(mt, ct);

		if (Executed) {
			Messager.sendMessage(Ability.getPlayer(), ChatColor.translateAlternateColorCodes('&', "&d능력을 사용하였습니다."));
		}
	}

	private MaterialType parseMaterialType(Material m) {
		for (MaterialType Type : MaterialType.values()) {
			if (Type.getMaterial().equals(m)) {
				return Type;
			}
		}

		return null;
	}

	private AbilityBase ability;

	/**
	 * 플레이어에게 해당 능력을 부여합니다.
	 * @param p            	능력을 부여할 플레이어
	 * @param abilityClass 	부여할 능력의 종류 (능력 클래스)
	 * @throws Exception 	능력을 부여하는 도중 오류가 발생하였을 경우
	 */
	public void setAbility(Class<? extends AbilityBase> abilityClass) throws Exception {
		if(hasAbility()) {
			removeAbility();
		}
		
		Constructor<? extends AbilityBase> constructor = abilityClass.getConstructor(Participant.class);
		AbilityBase ability = constructor.newInstance(this);
		
		if(this.game.isRestricted()) {
			ability.setRestricted(true);
		} else {
			if(this.game.isGameStarted()) {
				ability.setRestricted(false);
			} else {
				ability.setRestricted(true);
			}
		}

		this.ability = ability;
	}

	/**
	 * 플레이어에게 해당 능력을 부여합니다.
	 * @param ability	부여할 능력
	 */
	public void setAbility(AbilityBase ability) {
		if(hasAbility()) {
			removeAbility();
		}
		
		if(this.game.isRestricted()) {
			ability.setRestricted(true);
		} else {
			if(this.game.isGameStarted()) {
				ability.setRestricted(false);
			} else {
				ability.setRestricted(true);
			}
		}
		
		this.ability = ability;
	}

	public boolean hasAbility() {
		return ability != null;
	}

	public AbilityBase getAbility() {
		return ability;
	}
	
	public void removeAbility() {
		if(getAbility() != null) {
			getAbility().DeleteAbility();
			ability = null;
		}
	}
	
	/**
	 * 이 플레이어의 능력을 to에게 옮깁니다.
	 * to가 게임에 참여하고 있지 않거나 이 플레이어에게 능력이 없을 경우 아무 작업도 하지 않습니다.
	 */
	public void transferAbility(Player to) {
		if(hasAbility() && this.game.isParticipating(to) && Participant.checkParticipant(to)) {
			AbilityBase Ability = getAbility();
			removeAbility();
			
			Participant target = Participant.Construct(this.game, to);
			
			Ability.updateParticipant(target);
			
			target.setAbility(Ability);
		}
	}
	
	/**
	 * one.getPlayer()와 two.getPlayer()의 능력을 서로 뒤바꿉니다.
	 * @param one	첫번째 플레이어
	 * @param two	두번째 플레이어
	 */
	public void swapAbility(Participant target) {
		if(hasAbility() && target.hasAbility()) {
			AbilityBase first = getAbility();
			AbilityBase second = target.getAbility();
			
			removeAbility();
			target.removeAbility();
			
			first.updateParticipant(target);
			second.updateParticipant(this);
			
			this.setAbility(second);
			target.setAbility(first);
		}
	}
	
	public Player getPlayer() {
		return player;
	}

	private void setPlayer(Player player) {
		this.player = player;
	}

}
