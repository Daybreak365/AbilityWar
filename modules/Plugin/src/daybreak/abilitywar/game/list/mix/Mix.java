package daybreak.abilitywar.game.list.mix;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.ability.event.AbilityPreActiveSkillEvent;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.collect.SetUnion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;

@AbilityManifest(name = "믹스", rank = Rank.SPECIAL, species = Species.OTHERS, explain = "$(EXPLAIN)", summarize = "$(SUMMARIZE)")
public class Mix extends AbilityBase implements ActiveHandler, TargetHandler {

	private Synergy synergy = null;
	private AbilityBase first = null;
	private AbilityBase second = null;

	private final Object EXPLAIN = new Object() {
		@Override
		public String toString() {
			final StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§a---------------------------------");
			if (synergy != null) {
				final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
				joiner.add("§f시너지: §a" + base.getLeft().getManifest().name() + " §f+ §a" + base.getRight().getManifest().name());
				joiner.add("§a---------------------------------");
				joiner.add("§b" + synergy.getName() + " " + synergy.getRank().getRankName() + " " + synergy.getSpecies().getSpeciesName());
				for (final Iterator<String> iterator = synergy.getExplanation(); iterator.hasNext(); ) {
					joiner.add(ChatColor.RESET + iterator.next());
				}
			} else {
				formatInfo(joiner, first);
				joiner.add("§a---------------------------------");
				formatInfo(joiner, second);
			}
			return joiner.toString();
		}

		private void formatInfo(final StringJoiner joiner, final AbilityBase ability) {
			if (ability != null) {
				joiner.add("§b" + ability.getName() + " §f[" + (ability.isRestricted() ? "§7능력 비활성화됨" : "§a능력 활성화됨") + "§f] " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
				for (final Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
					joiner.add(ChatColor.RESET + iterator.next());
				}
			} else {
				joiner.add("§f능력이 없습니다.");
			}
		}
	};

	private final Object SUMMARIZE = new Object() {
		@Override
		public String toString() {
			final StringJoiner joiner = new StringJoiner("\n");
			joiner.add("§a---------------------------------");
			if (synergy != null) {
				final Pair<AbilityRegistration, AbilityRegistration> base = SynergyFactory.getSynergyBase(synergy.getRegistration());
				joiner.add("§f시너지: §a" + base.getLeft().getManifest().name() + " §f+ §a" + base.getRight().getManifest().name());
				joiner.add("§a---------------------------------");
				joiner.add("§b" + synergy.getName() + " " + synergy.getRank().getRankName() + " " + synergy.getSpecies().getSpeciesName());
				if (synergy.hasSummarize()) {
					for (final Iterator<String> iterator = synergy.getExplanation(); iterator.hasNext(); ) {
						joiner.add(ChatColor.RESET + iterator.next());
					}
				} else {
					joiner.add("§f요약이 작성되지 않은 능력입니다.");
				}
			} else {
				formatSummarize(joiner, first);
				joiner.add("§a---------------------------------");
				formatSummarize(joiner, second);
			}
			return joiner.toString();
		}

		private void formatSummarize(final StringJoiner joiner, final AbilityBase ability) {
			if (ability != null) {
				joiner.add("§b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
				if (ability.hasSummarize()) {
					for (final Iterator<String> iterator = ability.getSummarize(); iterator.hasNext(); ) {
						joiner.add(ChatColor.RESET + iterator.next());
					}
				} else {
					joiner.add("§f요약이 작성되지 않은 능력입니다.");
				}
			} else {
				joiner.add("§f능력이 없습니다.");
			}
		}
	};

	public Mix(Participant participant) {
		super(participant);
	}

	public Synergy getSynergy() {
		return synergy;
	}

	public AbilityBase getFirst() {
		return first;
	}

	public AbilityBase getSecond() {
		return second;
	}

	public void setAbility(final Class<? extends AbilityBase> first, final Class<? extends AbilityBase> second) throws ReflectiveOperationException {
		removeAbility();
		final AbilityRegistration synergyReg = SynergyFactory.getSynergy(first, second);
		if (synergyReg != null && !Settings.isBlacklisted(synergyReg.getManifest().name())) {
			this.synergy = (Synergy) create(synergyReg.getAbilityClass(), getParticipant());
			this.synergy.setRestricted(false);
		} else {
			this.first = create(first, getParticipant());
			this.first.setRestricted(false);
			this.second = create(second, getParticipant());
			this.second.setRestricted(false);
		}
	}

	public void setAbility(final AbilityRegistration first, final AbilityRegistration second) throws ReflectiveOperationException {
		setAbility(first.getAbilityClass(), second.getAbilityClass());
	}

	public void setFirst(final Class<? extends AbilityBase> first) throws ReflectiveOperationException {
		if (this.first != null) {
			this.first.destroy();
			this.first = null;
		}
		final AbilityRegistration synergyReg;
		if (hasSynergy()) {
			synergyReg = SynergyFactory.getSynergy(first, SynergyFactory.getSynergyBase(synergy.getRegistration()).getRight().getAbilityClass());
			this.synergy.destroy();
			this.synergy = null;
		} else {
			if (this.second != null) {
				synergyReg = SynergyFactory.getSynergy(first, this.second.getClass());
				if (synergyReg != null) {
					this.second.destroy();
					this.second = null;
				}
			} else {
				synergyReg = null;
			}
		}
		if (synergyReg != null && !Settings.isBlacklisted(synergyReg.getManifest().name())) {
			this.synergy = (Synergy) create(synergyReg.getAbilityClass(), getParticipant());
			this.synergy.setRestricted(false);
		} else {
			this.first = create(first, getParticipant());
			this.first.setRestricted(false);
		}
	}

	public void setSecond(final Class<? extends AbilityBase> second) throws ReflectiveOperationException {
		if (this.second != null) {
			this.second.destroy();
			this.second = null;
		}
		final AbilityRegistration synergyReg;
		if (hasSynergy()) {
			synergyReg = SynergyFactory.getSynergy(SynergyFactory.getSynergyBase(synergy.getRegistration()).getLeft().getAbilityClass(), second);
			this.synergy.destroy();
			this.synergy = null;
		} else {
			if (this.first != null) {
				synergyReg = SynergyFactory.getSynergy(this.first.getClass(), second);
				if (synergyReg != null) {
					this.first.destroy();
					this.first = null;
				}
			} else {
				synergyReg = null;
			}
		}
		if (synergyReg != null && !Settings.isBlacklisted(synergyReg.getManifest().name())) {
			this.synergy = (Synergy) create(synergyReg.getAbilityClass(), getParticipant());
			this.synergy.setRestricted(false);
		} else {
			this.second = create(second, getParticipant());
			this.second.setRestricted(false);
		}
	}

	public void setFirst(final AbilityRegistration first) throws ReflectiveOperationException {
		setFirst(first.getAbilityClass());
	}

	public void setSecond(final AbilityRegistration second) throws ReflectiveOperationException {
		setSecond(second.getAbilityClass());
	}

	public void setSynergy(final AbilityRegistration synergy) throws ReflectiveOperationException {
		if (!Synergy.class.isAssignableFrom(synergy.getAbilityClass())) {
			throw new IllegalArgumentException("시너지 능력이 아닙니다.");
		}
		if (Settings.isBlacklisted(synergy.getManifest().name())) return;
		removeAbility();
		this.synergy = (Synergy) create(synergy.getAbilityClass(), getParticipant());
		this.synergy.setRestricted(false);
	}

	@Override
	public boolean usesMaterial(Material material) {
		return (synergy != null && synergy.usesMaterial(material)) || (first != null && first.usesMaterial(material)) || (second != null && second.usesMaterial(material));
	}

	@Override
	public Set<GameTimer> getTimers() {
		if (hasAbility()) {
			if (synergy != null) {
				return SetUnion.union(synergy.getTimers(), super.getTimers());
			} else {
				if (first != null && second != null) {
					return SetUnion.union(first.getTimers(), second.getTimers(), super.getTimers());
				}
			}
		}
		return super.getTimers();
	}

	@Override
	public Set<GameTimer> getRunningTimers() {
		if (hasAbility()) {
			if (synergy != null) {
				return SetUnion.union(synergy.getRunningTimers(), super.getRunningTimers());
			} else {
				if (first != null && second != null) {
					return SetUnion.union(first.getRunningTimers(), second.getRunningTimers(), super.getRunningTimers());
				}
			}
		}
		return super.getRunningTimers();
	}

	public boolean hasSynergy() {
		return synergy != null;
	}

	public boolean hasAbility() {
		return synergy != null || (first != null && second != null);
	}

	public void removeAbility() {
		if (hasAbility()) {
			if (synergy != null) {
				synergy.destroy();
				this.synergy = null;
			}
			if (first != null) {
				first.destroy();
				this.first = null;
			}
			if (second != null) {
				second.destroy();
				this.second = null;
			}
		}
	}

	@Override
	public boolean ActiveSkill(@NotNull Material material, @NotNull AbilityBase.ClickType clickType) {
		if (hasAbility()) {
			if (synergy != null) {
				if (synergy instanceof ActiveHandler) {
					final AbilityPreActiveSkillEvent preEvent = new AbilityPreActiveSkillEvent(synergy, material, clickType);
					Bukkit.getPluginManager().callEvent(preEvent);
					if (preEvent.isCancelled()) return false;
					else return ((ActiveHandler) synergy).ActiveSkill(material, clickType);
				} else return false;
			} else {
				boolean abilityUsed = false;
				if (first instanceof ActiveHandler) {
					final AbilityPreActiveSkillEvent preEvent = new AbilityPreActiveSkillEvent(first, material, clickType);
					Bukkit.getPluginManager().callEvent(preEvent);
					if (!preEvent.isCancelled() && ((ActiveHandler) first).ActiveSkill(material, clickType)) {
						abilityUsed = true;
					}
				}
				if (second instanceof ActiveHandler) {
					final AbilityPreActiveSkillEvent preEvent = new AbilityPreActiveSkillEvent(second, material, clickType);
					Bukkit.getPluginManager().callEvent(preEvent);
					if (!preEvent.isCancelled() && ((ActiveHandler) second).ActiveSkill(material, clickType)) {
						abilityUsed = true;
					}
				}
				return abilityUsed;
			}
		}
		return false;
	}

	@Override
	public void TargetSkill(@NotNull Material material, @NotNull LivingEntity entity) {
		if (hasAbility()) {
			if (synergy != null) {
				if (synergy instanceof TargetHandler) ((TargetHandler) synergy).TargetSkill(material, entity);
			} else {
				if (first instanceof TargetHandler) ((TargetHandler) first).TargetSkill(material, entity);
				if (second instanceof TargetHandler) ((TargetHandler) second).TargetSkill(material, entity);
			}
		}
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (synergy != null) {
				synergy.setRestricted(false);
			}
			if (first != null) {
				first.setRestricted(false);
			}
			if (second != null) {
				second.setRestricted(false);
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (synergy != null) {
				synergy.setRestricted(true);
			}
			if (first != null) {
				first.setRestricted(true);
			}
			if (second != null) {
				second.setRestricted(true);
			}
		} else if (update == Update.ABILITY_DESTROY) {
			if (synergy != null) {
				synergy.destroy();
				this.synergy = null;
			}
			if (first != null) {
				first.destroy();
				this.first = null;
			}
			if (second != null) {
				second.destroy();
				this.second = null;
			}
		}
	}
}
