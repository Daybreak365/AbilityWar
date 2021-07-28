package daybreak.abilitywar.ability.list;

import com.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Sleep;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "모르페우스", rank = Rank.S, species = Species.GOD, explain = {

})
@Beta
public class Morpheus extends AbilityBase implements ActiveHandler {

	private static final Note NOTE_F = new Note(0, Tone.F, false), DREAM = new Note(0, Tone.D, false), NIGHTMARE = new Note(0, Tone.C, true),
			NOTE_D_SHARP = new Note(0, Tone.D, true), NOTE_A_SHARP_DOWN = new Note(0, Tone.A, true),
			NOTE_A_SHARP_UP = new Note(1, Tone.A, true);
	private static final Set<Material> swords;

	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
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

	public Morpheus(Participant participant) {
		super(participant);
	}

	@Override
	public boolean usesMaterial(Material material) {
		return super.usesMaterial(material) || swords.contains(material);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
			getPlayer().setHealth(getPlayer().getHealth() - 1);
		}
		if (swords.contains(material) && clickType == ClickType.RIGHT_CLICK) {
			new SoundEffect(false).start();
			for (Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 8, 8, predicate)) {
				Sleep.apply(getGame().getParticipant(player), TimeUnit.SECONDS, 6);
			}
		}
		return false;
	}

	private class SoundEffect extends AbilityTimer {

		private final Note note;

		private SoundEffect(boolean nightmare) {
			super(TaskType.NORMAL, 6);
			setPeriod(TimeUnit.TICKS, 2);
			this.note = nightmare ? NIGHTMARE : DREAM;
		}

		@Override
		protected void run(int count) {
			switch (count) {
				case 1: {
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, NOTE_F);
				}
				break;
				case 2: {
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, NOTE_D_SHARP);
				}
				break;
				case 3: {
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, note);
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, NOTE_A_SHARP_DOWN);
				}
				break;
				case 6: {
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, note);
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, NOTE_F);
					SoundLib.PIANO.playInstrument(getPlayer().getLocation(), .5f, NOTE_A_SHARP_UP);
				}
				break;
			}
		}
	}
}
