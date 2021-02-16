package daybreak.abilitywar.ability.list.hermit;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

@AbilityManifest(name = "헤르밋", rank = Rank.A, species = Species.HUMAN, explain = {
		"비전투 상태로 $[HIDE_PREPARE_TIME]초가 지날 경우 은신합니다. 은신 상태에서는 갑옷과 들고 있는",
		"아이템이 상대에게 보이지 않으며, 몸이 투명해지고 타게팅의 대상이 되지 않습니다.",
		"대미지를 입거나 공격을 할 경우 발각되고, 은신 상태가 해제됩니다."
})
public abstract class AbstractHermit extends AbilityBase {

	public static final SettingObject<Integer> HIDE_PREPARE_TIME = abilitySettings.new SettingObject<Integer>(AbstractHermit.class, "hide-prepare-time", 20, "# 은신 준비 시간") {
		@Override
		public boolean condition(Integer value) {
			return value >= 3;
		}
	};

	protected AbstractHermit(Participant participant) {
		super(participant);
	}

	protected abstract void hide0();
	protected abstract void show0();

	private void hide() {
		if (hiding) return;
		this.hiding = true;
		actionbarChannel.update("§7은신 중");
		getParticipant().attributes().TARGETABLE.setValue(false);
		hide0();
	}

	private void show() {
		if (!hiding) return;
		this.hiding = false;
		actionbarChannel.update("§6발각됨");
		getParticipant().attributes().TARGETABLE.setValue(true);
		show0();
	}

	private final int hidePrepareTime = Math.max(3, (int) (Wreck.calculateDecreasedAmount(25) * HIDE_PREPARE_TIME.getValue()));
	private final ActionbarChannel cooldownActionbarChannel = newActionbarChannel(), actionbarChannel = newActionbarChannel();
	private final AbilityTimer cooldown = new AbilityTimer(TaskType.REVERSE, hidePrepareTime) {
		@Override
		protected void onStart() {
			show();
		}

		@Override
		protected void run(int count) {
			cooldownActionbarChannel.update(ChatColor.RED.toString() + "은신까지 " + ChatColor.WHITE.toString() + ": " + ChatColor.GOLD + TimeUtil.parseTimeAsString(getCount()));
		}

		@Override
		protected void onCountSet() {
			cooldownActionbarChannel.update(ChatColor.RED.toString() + "은신까지 " + ChatColor.WHITE.toString() + ": " + ChatColor.GOLD + TimeUtil.parseTimeAsString(getCount()));
		}

		@Override
		protected void onEnd() {
			cooldownActionbarChannel.update(null);
			hide();
		}
	}.setBehavior(RestrictionBehavior.PAUSE_RESUME).register();

	private boolean hiding = false;

	public boolean isHiding() {
		return hiding;
	}

	@SubscribeEvent
	private void onEntityTarget(EntityTargetLivingEntityEvent e) {
		if (getPlayer().equals(e.getTarget()) && hiding) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityDamage(EntityDamageEvent e) {
		if (!cooldown.start()) {
			if (cooldown.getCount() < 2) {
				cooldown.setCount(2);
			}
		} else {
			cooldown.setCount(2);
		}
	}

	@SubscribeEvent(onlyRelevant = true, ignoreCancelled = true)
	private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getEntity()) || getPlayer().equals(e.getDamager()) || (e.getDamager() instanceof Arrow && getPlayer().equals(((Arrow) e.getDamager()).getShooter()))) {
			if (!cooldown.start()) {
				cooldown.setCount(hidePrepareTime);
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (!cooldown.isRunning()) {
				hide();
			}
		} else if (update == Update.RESTRICTION_SET || update == Update.ABILITY_DESTROY) {
			show();
			actionbarChannel.update(null);
		}
	}
}
