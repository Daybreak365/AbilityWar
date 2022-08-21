package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@AbilityManifest(name = "테미스", rank = Rank.A, species = Species.GOD, explain = {
		"정의의 여신 테미스.",
		"다른 플레이어를 살해한 플레이어에게 죄 스택을 1씩 부여하며, 죄 스택이 쌓인",
		"플레이어를 공격할 때 §c스택 §7x §c$[multiplier]§f만큼의 추가 대미지를 줍니다."
}, summarize = {
		"적이 §c죽인 플레이어 수§f에 비례해 추가 피해를 입힙니다."
})
public class Themis extends AbilityBase {

	private final Map<UUID, Kills> killsMap = new HashMap<>();

	public Themis(Participant participant) {
		super(participant);
	}

	private final double multiplier = Math.floor(Math.max(.5, (1d - (Math.max(0, getGame().getParticipants().size() - 4) / 50d)) * 2) * 100) / 100;
	private final Predicate<Entity> ONLY_PARTICIPANTS = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			return getGame().isParticipating(entity.getUniqueId())
					&& (!(getGame() instanceof DeathManager.Handler) || !((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()));
		}
	};

	@SubscribeEvent
	private void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		final Kills kills = this.killsMap.get(e.getEntity().getUniqueId());
		if (kills != null && getPlayer().equals(e.getDamager())) {
			e.setDamage(e.getDamage() + (multiplier * kills.kills));
		}
	}

	@SubscribeEvent
	private void onEntityDeath(final EntityDeathEvent e) {
		if (killsMap.containsKey(e.getEntity().getUniqueId())) killsMap.get(e.getEntity().getUniqueId()).stop(true);
	}

	@SubscribeEvent
	private void onPlayerDeath(final PlayerDeathEvent e) {
		final Player entity = e.getEntity();
		if (killsMap.containsKey(entity.getUniqueId())) killsMap.get(entity.getUniqueId()).stop(true);
		if (!ONLY_PARTICIPANTS.test(entity)) return;
		final Player killer = entity.getKiller();
		if (killer != null && !getPlayer().equals(killer) && !killer.equals(entity)) {
			if (!ONLY_PARTICIPANTS.test(killer)) return;
			if (killsMap.containsKey(killer.getUniqueId())) {
				killsMap.get(killer.getUniqueId()).addKills();
			} else new Kills(killer).start();
		}
	}

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerJoin(PlayerJoinEvent e) {
		for (Kills kills : killsMap.values()) {
			kills.hid = true;
		}
	}

	private class Kills extends AbilityTimer {

		private final Player entity;
		private final IHologram hologram;
		private boolean hid = false;
		private int kills = 0;

		private Kills(Player entity) {
			super();
			setPeriod(TimeUnit.TICKS, 2);
			this.entity = entity;
			this.hologram = NMS.newHologram(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), "");
			hologram.display(getPlayer());
			Themis.this.killsMap.put(entity.getUniqueId(), this);
			addKills();
		}

		@Override
		protected void run(int count) {
			if (NMS.isInvisible(entity)) {
				if (!hid) {
					this.hid = true;
					hologram.hide(getPlayer());
				}
			} else {
				if (hid) {
					this.hid = false;
					hologram.display(getPlayer());
				}
				hologram.teleport(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY() + entity.getEyeHeight() + 0.6, entity.getLocation().getZ(), entity.getLocation().getYaw(), 0);
			}
		}

		private void addKills() {
			kills++;
			hologram.setText("§4" + kills + "§c명 죽임");
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			hologram.unregister();
			Themis.this.killsMap.remove(entity.getUniqueId());
		}
	}

}
