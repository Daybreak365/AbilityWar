package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.boundary.BoundingBox;
import daybreak.abilitywar.utils.base.minecraft.boundary.EntityBoundingBox;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@AbilityManifest(name = "고슴도치", rank = Rank.C, species = Species.ANIMAL, explain = {
		"가시로 가까이에 있는 모든 생명체를 찌릅니다.",
		"아야! 그것 참 아프겠네요."
})
@Tips(tip = {
		"아야! 상대를 당신의 뾰족한 가시로 찔러 아프게 하세요."
}, strong = {
		@Description(subject = "좁은 공간", explain = {
				"한두 칸의 좁은 공간에서는 고슴도치가 상대를 지속적으로 공격할 수 있기",
				"때문에, 그나마 강력해집니다."
		})
}, stats = @Stats(offense = Level.THREE, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)
public class Hedgehog extends AbilityBase {

	public static final SettingObject<Double> DAMAGE_CONFIG = abilitySettings.new SettingObject<Double>(Hedgehog.class, "damage", 2.0,
			"# 대미지") {

		@Override
		public boolean condition(Double value) {
			return value >= 0;
		}

	};

	public Hedgehog(Participant participant) {
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

	private final BoundingBox boundingBox = new EntityBoundingBox(getPlayer(), -0.600000012, 0, -0.600000012, 0.6500000012, 1.799999952, 0.6500000012);

	private final AbilityTimer passive = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (!getPlayer().isDead()) {
				final double damage = DAMAGE_CONFIG.getValue();
				for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, getPlayer().getWorld(), boundingBox, predicate)) {
					livingEntity.setNoDamageTicks(0);
					livingEntity.damage(damage, getPlayer());
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 7).register();

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
			NMS.setArrowsInBody(getPlayer(), 25);
		}
	}

}
