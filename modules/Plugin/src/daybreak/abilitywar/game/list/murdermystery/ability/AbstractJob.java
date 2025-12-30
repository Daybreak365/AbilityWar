package daybreak.abilitywar.game.list.murdermystery.ability;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.murdermystery.MurderMystery;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.random.Random;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractJob extends AbilityBase {

	public static final AbilitySettings mmAbilitySettings = new AbilitySettings(FileUtil.newFile("murder-abilitysettings.yml"));

	protected final Random random = new Random();

	protected AbstractJob(Participant participant) {
		super(participant);
		if (!(getGame() instanceof MurderMystery)) {
			throw new IllegalStateException();
		}
	}

	public boolean hasBow() {
		ItemStack stack = getPlayer().getInventory().getItem(2);
		return stack != null && stack.getType() == Material.BOW;
	}

	public int getArrowCount() {
		ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			return stack.getAmount();
		} else return 0;
	}

	public boolean addArrow() {
		ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			if (stack.getAmount() < 64) {
				stack.setAmount(stack.getAmount() + 1);
				getPlayer().getInventory().setItem(3, stack);
				getPlayer().sendMessage("§8+ §f1 화살");
				return true;
			} else return false;
		} else {
			getPlayer().getInventory().setItem(3, new ItemStack(Material.ARROW));
			getPlayer().sendMessage("§8+ §f1 화살");
			return true;
		}
	}

	public void addArrow(final int amount, final boolean message) {
		final ItemStack stack = getPlayer().getInventory().getItem(3);
		if (stack != null && stack.getType() == Material.ARROW) {
			final int original = stack.getAmount();
			if (original < 64) {
				final int added = Math.min(64, original + amount);
				stack.setAmount(added);
				getPlayer().getInventory().setItem(3, stack);
				if (message) getPlayer().sendMessage("§8+ §f" + (added - original) + " 화살");
			}
		} else {
			final int a = Math.min(64, amount);
			getPlayer().getInventory().setItem(3, new ItemStack(Material.ARROW, a));
			if (message) getPlayer().sendMessage("§8+ §f" + a + " 화살");
		}
	}

}
