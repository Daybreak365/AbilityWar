package daybreak.abilitywar.ability.event;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;

public class AbilityActiveSkillEvent extends AbilityEvent {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Material materialType;
	private final ClickType clickType;

	public AbilityActiveSkillEvent(AbilityBase ability, Material materialType, ClickType clickType) {
		super(ability);
		this.materialType = materialType;
		this.clickType = clickType;
	}

	public Material getMaterialType() {
		return materialType;
	}

	public ClickType getClickType() {
		return clickType;
	}

}
