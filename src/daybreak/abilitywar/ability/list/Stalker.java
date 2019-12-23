package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityManifest(Name = "추적자", Rank = Rank.D, Species = Species.HUMAN)
public class Stalker extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Stalker.class, "Cooldown", 120,
			"# 쿨타임") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}

	};

	public static final SettingObject<Integer> DurationConfig = new SettingObject<Integer>(Stalker.class, "Duration", 30,
			"# 지속 시간 (단위: 틱)") {

		@Override
		public boolean Condition(Integer value) {
			return value >= 0 && value <= 20;
		}

	};

	public Stalker(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 다른 플레이어가 타게팅할 수 없고 벽을 통과할 수 있는"),
				ChatColor.translateAlternateColorCodes('&', "&f상태로 변하여 짧게 돌진합니다." + Messager.formatCooldown(CooldownConfig.getValue())));
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());
	private final DurationTimer skill = new DurationTimer(DurationConfig.getValue(), cooldownTimer) {
		GameMode originalMode;
		Vector originalVector;

		@Override
		protected void onDurationStart() {
			Player p = getPlayer();
			originalMode = p.getGameMode();
			originalVector = p.getVelocity();
			getParticipant().attributes().TARGETABLE.setValue(false);
			p.setGameMode(GameMode.SPECTATOR);
			p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(0.3)));
		}

		@Override
		protected void onDurationProcess(int count) {
		}

		@Override
		protected void onDurationEnd() {
			getPlayer().setGameMode(originalMode);
			getPlayer().setVelocity(originalVector);
			getParticipant().attributes().TARGETABLE.setValue(true);
		}
	}.setPeriod(1);

	@Override
	public boolean ActiveSkill(MaterialType mt, ClickType ct) {
		if (mt.equals(MaterialType.IRON_INGOT) && ct.equals(ClickType.RIGHT_CLICK) && !skill.isDuration() && !cooldownTimer.isCooldown()) {
			skill.startTimer();
			return true;
		}
		return false;
	}

	@Override
	public void TargetSkill(MaterialType mt, LivingEntity entity) {}

}
