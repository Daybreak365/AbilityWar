package Marlang.AbilityWar.Utils.VersionCompat;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackCompat {
	
	@SuppressWarnings("deprecation")
	public static SkullMeta setOwner(SkullMeta meta, String Player) {
		if(ServerVersion.getVersion() >= 13) {
			meta.setOwningPlayer(new OfflinePlayer() {
				
				@Override
				public Map<String, Object> serialize() {
					return null;
				}
				
				@Override
				public void setOp(boolean arg0) {}
				
				@Override
				public boolean isOp() {
					return false;
				}
				
				@Override
				public void setWhitelisted(boolean arg0) {}
				
				@Override
				public boolean isWhitelisted() {
					return false;
				}
				
				@Override
				public boolean isOnline() {
					return false;
				}
				
				@Override
				public boolean isBanned() {
					return false;
				}
				
				@Override
				public boolean hasPlayedBefore() {
					return false;
				}
				
				@Override
				public UUID getUniqueId() {
					return null;
				}
				
				@Override
				public Player getPlayer() {
					return null;
				}
				
				@Override
				public String getName() {
					return Player;
				}
				
				@Override
				public long getLastPlayed() {
					return 0;
				}
				
				@Override
				public long getFirstPlayed() {
					return 0;
				}
				
				@Override
				public Location getBedSpawnLocation() {
					return null;
				}
				
			});
		} else {
			meta.setOwner(Player);
		}
		
		return meta;
	}
	
}
