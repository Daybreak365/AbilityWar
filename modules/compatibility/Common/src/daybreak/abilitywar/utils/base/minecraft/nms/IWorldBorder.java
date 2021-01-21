package daybreak.abilitywar.utils.base.minecraft.nms;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface IWorldBorder {

	void setWorld(@NotNull World world);
	void setCenter(double x, double z);
	void setDamageAmount(double amount);
	void setDamageBuffer(double buffer);
	void setSize(double size);
	void setWarningDistance(int distance);
	void setWarningTime(int time);

}
