package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.PushingList;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import java.util.Collection;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

@AbilityManifest(name = "시간 역행", rank = Rank.S, species = Species.HUMAN, explain = {
		"철괴를 우클릭하면 시간을 역행해 $[TimeConfig]초 전으로 돌아갑니다. $[COOLDOWN_CONFIG]"
})
public class TimeRewind extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(TimeRewind.class, "Cooldown", 100,
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

	public static final SettingObject<Integer> TimeConfig = abilitySettings.new SettingObject<Integer>(TimeRewind.class, "Time", 5,
			"# 능력을 사용했을 때 몇초 전으로 돌아갈지 설정합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public TimeRewind(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(COOLDOWN_CONFIG.getValue());

	private final int time = TimeConfig.getValue();

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					Skill.start();

					return true;
				}
			}
		}

		return false;
	}

	@SubscribeEvent
	public void onPlayerDeath(ParticipantDeathEvent e) {
		if (e.getParticipant().equals(getParticipant())) {
			playerDatas = new PushingList<>(time * 20);
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && rewinding) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && rewinding) {
			e.setCancelled(true);
		}
	}

	private boolean rewinding = false;

	private PushingList<PlayerData> playerDatas = new PushingList<>(time * 20);

	private final DurationTimer Skill = new DurationTimer(time * 10, cooldownTimer) {

		private LinkedList<PlayerData> datas;

		@Override
		public void onDurationStart() {
			rewinding = true;
			this.datas = playerDatas;
			playerDatas = new PushingList<>(time * 20);
		}

		@Override
		public void onDurationProcess(int seconds) {
			PlayerData data = datas.pollLast();
			if (data != null && !getPlayer().isDead()) {
				data.apply();
			}
		}

		@Override
		public void onDurationEnd() {
			rewinding = false;
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			SoundLib.BELL.playInstrument(getPlayer(), Note.sharp(0, Tone.F));
			SoundLib.BELL.playInstrument(getPlayer(), Note.natural(1, Tone.A));
		}

	}.setPeriod(TimeUnit.TICKS, 1);

	private final Timer save = new Timer() {

		@Override
		public void run(int count) {
			playerDatas.add(new PlayerData());
		}

	}.setPeriod(TimeUnit.TICKS, 2);

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			save.start();
		}
	}

	private class PlayerData {

		private final Player player;
		private final Location location;
		private final double health;
		private final int fireTicks;
		private final float fallDistance;
		private final Collection<PotionEffect> potionEffects;

		private PlayerData() {
			this.player = getPlayer();
			this.location = player.getLocation();
			this.health = player.getHealth();
			this.fireTicks = player.getFireTicks();
			this.fallDistance = player.getFallDistance();
			this.potionEffects = player.getActivePotionEffects();
		}

		private void apply() {
			player.teleport(location);
			if (health > 0.0) {
				player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), health));
			}
			player.setFireTicks(fireTicks);
			player.setFallDistance(fallDistance);
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			player.addPotionEffects(potionEffects);
		}

	}

}
