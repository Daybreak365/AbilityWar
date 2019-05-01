package DayBreak.AbilityWar.Development.PlaceholderAPI;

import org.bukkit.entity.Player;

import DayBreak.AbilityWar.AbilityWar;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PAPIExpansion extends PlaceholderExpansion {

	@Override
	public String getAuthor() {
		return "DayBreak";
	}

	@Override
	public String getIdentifier() {
		return "abilitywar";
	}

	@Override
	public String getVersion() {
		return AbilityWar.getPlugin().getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		for(Placeholders placeholder : Placeholders.values()) {
			if(placeholder.getIdentifier().equalsIgnoreCase(identifier)) {
				return placeholder.Request(p);
			}
		}
		
		return null;
	}
	
}
