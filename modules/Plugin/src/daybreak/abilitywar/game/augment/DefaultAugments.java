package daybreak.abilitywar.game.augment;

import daybreak.abilitywar.game.augment.list.AbilityEcho;
import daybreak.abilitywar.game.augment.list.AbilityOverdrive;
import daybreak.abilitywar.game.augment.list.ArrowFocus;
import daybreak.abilitywar.game.augment.list.BattleTrance;
import daybreak.abilitywar.game.augment.list.Comeback;
import daybreak.abilitywar.game.augment.list.Executioner;
import daybreak.abilitywar.game.augment.list.ExtraHeart;
import daybreak.abilitywar.game.augment.list.GlassCannon;
import daybreak.abilitywar.game.augment.list.LastStand;
import daybreak.abilitywar.game.augment.list.PrismaticBody;
import daybreak.abilitywar.game.augment.list.RaidBoss;
import daybreak.abilitywar.game.augment.list.SwiftStart;
import daybreak.abilitywar.game.augment.list.Thornmail;
import daybreak.abilitywar.game.augment.list.VampiricBlade;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class DefaultAugments {

	private DefaultAugments() {}

	public static AugmentRegistry createRegistry() {
		final AugmentRegistry registry = new AugmentRegistry();
		registry.register(new ExtraHeart());
		registry.register(new SwiftStart());
		registry.register(new BattleTrance());
		registry.register(new PrismaticBody());
		registry.register(new GlassCannon());
		registry.register(new VampiricBlade());
		registry.register(new Executioner());
		registry.register(new Comeback());
		registry.register(new ArrowFocus());
		registry.register(new Thornmail());
		registry.register(new LastStand());
		registry.register(new RaidBoss());
		registry.register(new AbilityEcho());
		registry.register(new AbilityOverdrive());
		return registry;
	}

	public static String format(@NotNull Augment augment) {
		return augment.getRarity().format("[" + augment.getRarity().getDisplayName() + "] ") + ChatColor.WHITE + augment.getDisplayName() + ChatColor.GRAY + " - " + augment.getDescription();
	}
}
