package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AbilityActiveSkillEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	private final Material material;
	private final ClickType clickType;

	public AbilityActiveSkillEvent(AbilityBase ability, Material material, ClickType clickType) {
		super(ability);
		this.material = material;
		this.clickType = clickType;
	}

	public Material getMaterial() {
		return material;
	}

	public ClickType getClickType() {
		return clickType;
	}
}
