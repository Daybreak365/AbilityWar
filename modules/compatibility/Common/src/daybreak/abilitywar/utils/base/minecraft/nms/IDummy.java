package daybreak.abilitywar.utils.base.minecraft.nms;

import daybreak.abilitywar.utils.base.minecraft.SkinInfo;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IDummy {

	String IDLE_MESSAGE = "피해를 입혀서 측정을 시작하세요.";
	SkinInfo DEFAULT_SKIN = new SkinInfo(
			"더미",
			"ewogICJ0aW1lc3RhbXAiIDogMTYwMTAzMzgzMDYzMiwKICAicHJvZmlsZUlkIiA6ICIxZDUyMzNkMzg4NjI0YmFmYjAwZTMxNTBhN2FhM2E4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICIwMDAwMDAwMDAwMDAwMDBKIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MzODMyY2U3ZmJmMDBkZDBlYWE1Mzc5MTg2MTJjZDJmMmU5YTJhYThiNmVjNTgyMTkzMmQzODc2OGY5NTVjZTkiCiAgICB9CiAgfQp9",
			"X/PgU9nANhQ3oh1tACmpOi3EbRWxC4yYWmyCRXl3GAYnQGEzPYLH9jfFSCjVx0OX9NzB2gJHBnqlpv7h9Kt6a38cbjs8WSKHVH1yfNNiaTg66QAx5wBz1JBwGYe831r+ucBNyRq6wlv+xfCmkVAlcDAq0D5xR9Taw6DRuFqknS4o6M1d2tD9BVBWzvLjzS4OyYb0OZjxero2vXnQAwg/SKZkVSml5ncT6JjiM/QB1VfNeREixEKemiKC9Sr7pLfrEK4a5rvYyBuyqwLPZZ1UoX5hkOa0Bv0dMaE88MdCMVb8s7m6YLyoMAz4CRgLIrOp0QVe5vS2ozS1J4kmcyt9JU+CkIlKLNoSqg88g8prr+oVeeFRmyamRF4WvsXagKiF6o+Sa/kaE1xS8iA1q5WP8eonpQcSIX8rPmirI6z8Ni4GEBCHgP0tax9s1+d3vtJXOTzyNytia6K95SmrkbcTP0//6F3IHuZzPa2mkzYJ4iWmacOoJ1UyW7ebslWjrCPuBWrUmgs6pYApPoUJ/T8X6uH0Vu36zly24t3jPvW1P6ruksDLwqdE0E5VkbZkIVt/kkH/C7V2u4UEirNichWzEHnGeEMoMn7+hVbsgdOoEqrw5UWCoQh2/ElHgs1RGHJc29fjgReMXDcEU0YOIozEtqQx2pSkoPy0hzIFTq0uAO8="
	);
	int TICKS_TO_RESET = 140;
	byte SKIN_BIT_LAYER = 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;

	void addDamage(final double damage);
	void display(final Player player);
	Player getBukkitEntity();
	UUID getUniqueID();
	void remove();
	boolean isAlive();

}
