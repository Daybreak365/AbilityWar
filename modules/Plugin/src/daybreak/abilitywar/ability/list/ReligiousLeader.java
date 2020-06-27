package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

@AbilityManifest(name = "교주", rank = Rank.A, species = Species.HUMAN, explain = {
		"처음 시작하면 새로운 종교를 창시하며, 이름을 정할 수 있습니다.",
		"상대방을 철괴로 §6우클릭§f하면 신자로 영입할 수 있습니다.",
		"신자는 최대 게임에 참가중인 참가자 수의 §e1/2§f만큼 모을 수 있으며,",
		"신자가 참가자 수의 §e1/4 §f이상 모이면 철괴를 §6좌클릭§f해",
		"§c이단 심판§f을 시작할 수 있습니다. $[CooldownConfig]",
		"§c이단 심판§f이 진행중일 때 신자들과 교주는 서로 물리적으로 공격할 수 없으며,",
		"§f이 종교를 믿지 않는 참가자를 공격할 때 추가 대미지를 주며 심판합니다."
})
public class ReligiousLeader extends AbilityBase implements TargetHandler, ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(ReligiousLeader.class, "Cooldown", 150,
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

	public ReligiousLeader(Participant participant) {
		super(participant);
	}

	private final int maxBelivers = getGame().getParticipants().size() / 2;
	private final int minBelivers = getGame().getParticipants().size() / 4;
	private final Set<Participant> belivers = new HashSet<>();
	private boolean nameSelecting = false;
	private String religionName = null;

	private final Timer nameSelect = new Timer(30) {
		@Override
		protected void onStart() {
			sendMessage("창시할 종교의 이름을 채팅창에 입력하세요. (최대 10글자)");
			sendMessage("예시: §e'능력자'§f를 입력했다면, 종교의 이름은 §e'능력자교'§f가 됩니다.");
			nameSelecting = true;
		}

		@Override
		protected void run(int count) {
			actionbarChannel.update("§5종교§d의 이름을 정하세요: §e" + count + "");
		}

		@Override
		protected void onEnd() {
			nameSelecting = false;
			newReligion("능력자교");
			sendMessage("종교의 이름이 선택되지 않아 이름이 임의로 설정되었습니다.");
		}

	};

	private final ActionbarChannel actionbarChannel = newActionbarChannel();

	private void sendMessage(String message) {
		getPlayer().sendMessage("§5[§d교주§5] §f" + message);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(10, cooldownTimer) {
		@Override
		protected void onDurationStart() {
			Bukkit.broadcastMessage("§4" + religionName + " §c이단 심판이 시작되었습니다.");
			inquisition = true;
		}

		@Override
		protected void onDurationProcess(int seconds) {
		}

		@Override
		protected void onDurationEnd() {
			Bukkit.broadcastMessage("§4" + religionName + " §c이단 심판이 끝났습니다.");
			inquisition = false;
		}

		@Override
		protected void onDurationSilentEnd() {
			Bukkit.broadcastMessage("§4" + religionName + " §c이단 심판이 끝났습니다.");
			inquisition = false;
		}
	};

	private boolean inquisition = false;

	private void newReligion(String name) {
		religionName = name;
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage("§5" + name + "§f가 창시되었습니다.");
			}
		}.runTask(AbilityWar.getPlugin());
		actionbarChannel.update("§5" + religionName + " §d신자 수§f: §e" + belivers.size());
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		if (nameSelecting) {
			e.setCancelled(true);
			String name = e.getMessage();
			if (name.length() <= 10) {
				NewReligionEvent newReligionEvent = new NewReligionEvent(e.isAsynchronous(), name.concat("교"));
				Bukkit.getPluginManager().callEvent(newReligionEvent);
				if (!newReligionEvent.isCancelled()) {
					nameSelecting = false;
					nameSelect.stop(true);
					newReligion(name + "교");
				} else {
					getPlayer().sendMessage(String.valueOf(newReligionEvent.cancelMessage));
				}
			} else {
				sendMessage("종교 이름은 최대 10글자입니다.");
			}
		}
	}

	@SubscribeEvent
	private void onNewReligion(NewReligionEvent e) {
		if (!e.getLeader().equals(getParticipant()) && e.getName().equals(religionName)) {
			e.setCancelled(true);
			e.setCancelMessage("§c이미 다른 플레이어가 사용 중인 이름입니다.");
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR && religionName == null) {
			nameSelect.start();
		}
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Player damager = null;
		Player entity = null;
		if (e.getEntity() instanceof Player) {
			entity = (Player) e.getEntity();
		}
		if (e.getDamager() instanceof Player) {
			damager = (Player) e.getDamager();
		} else if (e.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) e.getDamager();
			if (projectile.getShooter() instanceof Player) {
				damager = (Player) projectile.getShooter();
			}
		}
		if (inquisition && entity != null && damager != null) {
			if (isReligious(damager)) {
				if (isReligious(entity)) {
					e.setCancelled(true);
					damager.sendMessage("§5이단 심판 §d중에 §f같은 종교§d의 신자를 때릴 수 없습니다.");
				} else {
					entity.getWorld().strikeLightningEffect(entity.getLocation());
					e.setDamage(e.getDamage() * 1.5);
				}
			}
		}
	}

	public boolean isReligious(Player player) {
		if (getGame().isParticipating(player)) {
			Participant participant = getGame().getParticipant(player);
			return belivers.contains(participant) || getParticipant().equals(participant);
		}
		return false;
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT) && clickType.equals(ClickType.LEFT_CLICK) && religionName != null && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			if (belivers.size() >= minBelivers) {
				skill.start();
			} else {
				sendMessage("신자 수가 부족합니다. (최소 " + minBelivers + "명)");
			}
		}
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {
		if (materialType.equals(Material.IRON_INGOT) && religionName != null && entity instanceof Player) {
			if (belivers.size() < maxBelivers) {
				Player target = (Player) entity;
				if (getGame().isParticipating(target) && belivers.add(getGame().getParticipant(target))) {
					sendMessage("§e" + target.getName() + "§f님은 이제 §5" + religionName + "§f를 믿습니다. §f( §5" + religionName + " §d신자 수§f: §e" + belivers.size() + " §f)");
					target.sendMessage("§e" + getPlayer().getName() + "§f님이 당신을 포섭했습니다: §5" + religionName + " §f만세!");
					actionbarChannel.update("§5" + religionName + " §d신자 수§f: §e" + belivers.size());
				}
			} else {
				sendMessage("신자 수가 최대치에 도달하여 더이상 모을 수 없습니다.");
			}
		}
	}

	public static class ReligionEvent extends Event {

		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		private final Participant leader;

		private ReligionEvent(boolean async, ReligiousLeader religiousLeader) {
			super(async);
			this.leader = religiousLeader.getParticipant();
		}

		public Participant getLeader() {
			return leader;
		}

	}

	public class NewReligionEvent extends ReligiousLeader.ReligionEvent implements Cancellable {

		private final String name;

		private NewReligionEvent(boolean async, String name) {
			super(async, ReligiousLeader.this);
			this.name = name;
		}

		public String getName() {
			return name;
		}

		private boolean cancelled = false;
		private String cancelMessage = null;

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		public void setCancelMessage(String cancelMessage) {
			this.cancelMessage = cancelMessage;
		}

	}

}
