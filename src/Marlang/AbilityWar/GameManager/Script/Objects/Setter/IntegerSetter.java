package Marlang.AbilityWar.GameManager.Script.Objects.Setter;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.GameManager.Script.ScriptWizard;

public class IntegerSetter extends Setter<Integer> {

	public IntegerSetter(String Key, Integer Default, ScriptWizard Wizard) {
		super(Key, Default, Wizard);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(ClickType click) {
		if(click.equals(ClickType.RIGHT)) {
			this.setValue(this.getValue() + 1);
		} else if(click.equals(ClickType.SHIFT_RIGHT)) {
			this.setValue(this.getValue() + 10);
		} else if(click.equals(ClickType.LEFT)) {
			if(getValue() > 0) {
				if(this.getValue() >= 2) {
					this.setValue(this.getValue() - 1);
				} else {
					this.setValue(1);
				}
			}
		} else if(click.equals(ClickType.SHIFT_LEFT)) {
			if(getValue() > 0) {
				if(this.getValue() >= 11) {
					this.setValue(this.getValue() - 10);
				} else {
					this.setValue(1);
				}
			}
		}
	}

	@Override
	public ItemStack getItem() {
		// TODO Auto-generated method stub
		return null;
	}

}
