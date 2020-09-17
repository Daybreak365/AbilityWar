package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.function.Predicate;

@AbilityManifest(name = "낙법의 달인", rank = Rank.C, species = Species.HUMAN, explain = {
		"수십년간의 고된 수련으로 낙법과 하나가 된 낙법의 달인.",
		"낙하해 땅에 닿았을 때 자동으로 물낙법을 하며, 낙하 거리에 비례해",
		"주변 4칸 내의 생명체들에게 대미지를 줍니다."
})
@Tips(tip = {
		"예측하지 못한 낙하 대미지를 걱정할 필요도 없으며, 능력을 이용해",
		"상대에게 대미지를 주기 위해 직접 올라가 낙하할 수도 있습니다.",
		"낙하한 거리에서 가까운 상대일 수록 큰 대미지를 주기 때문에, 대상을",
		"정확히 맞추는 것이 가장 좋습니다."
}, strong = {
		@Description(subject = "낙하 대미지 무시", explain = {
				"낙하 대미지를 완전히 무시하기 때문에 이를 이용해 큰 대미지를 넣는",
				"능력에 강합니다."
		})
}, weak = {
		@Description(subject = "빠른 상대", explain = {
				"떨어지며 상대를 맞추기 위해서는 상대의 다음 이동을 대략 예측해야 합니다.",
				"하지만 이동이 빠른 상대는 예측이 쉽지 않기 때문에 낙법의 달인으로 공격하기",
				"어렵습니다."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.THREE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class ExpertOfFall extends AbilityBase {

	public ExpertOfFall(Participant participant) {
		super(participant);
	}

	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}
	};

	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.FALL)) {
				e.setCancelled(true);
				Block block = getPlayer().getLocation().getBlock();
				Material blockType = block.getType();
				new AbilityTimer(1) {
					@Override
					protected void onStart() {
						block.setType(Material.WATER);
					}

					@Override
					protected void run(int count) {
					}

					@Override
					protected void onEnd() {
						block.setType(blockType);
					}
				}.setPeriod(TimeUnit.TICKS, 10).start();
				SoundLib.ENTITY_PLAYER_SPLASH.playSound(getPlayer());

				Block belowBlock = block.getRelative(BlockFace.DOWN);
				for (int i = 0; i < 3; i++) {
					FallingBlocks.spawnFallingBlock(belowBlock.getLocation().add(0, 1, 0), belowBlock.getType(), false, getPlayer().getLocation().toVector().subtract(belowBlock.getLocation().toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
				for (Damageable damageable : LocationUtil.getNearbyEntities(Damageable.class, getPlayer().getLocation(), 4, 4, predicate)) {
					damageable.damage(Math.min(getPlayer().getFallDistance() / 0.85, 25) * (1 - (getPlayer().getLocation().distanceSquared(damageable.getLocation()) / 16)), getPlayer());
				}
			}
		}
	}

}
