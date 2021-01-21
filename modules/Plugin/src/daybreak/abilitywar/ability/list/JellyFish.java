package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "해파리", rank = Rank.A, species = Species.ANIMAL, explain = {
		"플레이어를 근접 공격하면 대상을 $[DURATION_CONFIG]초간 기절시켜 움직이지 못하게 합니다."
})
public class JellyFish extends AbilityBase {

	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(JellyFish.class, "duration", 3,
			"# 지속 시간 (틱 단위)") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return String.valueOf(getValue() / 20.0);
		}

	};

	public JellyFish(Participant participant) {
		super(participant);
	}

	private final int stunDuration = DURATION_CONFIG.getValue();

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer())) {
			final Entity entity = e.getEntity();
			if (getGame().isParticipating(entity.getUniqueId())) {
				final Participant target = getGame().getParticipant(entity.getUniqueId());
				SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
				SoundLib.ENTITY_ITEM_PICKUP.playSound(target.getPlayer());
				Stun.apply(target, TimeUnit.TICKS, stunDuration);
			}
		}
	}

}
