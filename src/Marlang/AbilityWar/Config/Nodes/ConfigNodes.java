package Marlang.AbilityWar.Config.Nodes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Utils.FileManager;

public enum ConfigNodes {
	
	Game_Header("게임", "",
			"# 능력자 전쟁 게임 설정"),
	Game_Invincibility_Header("게임.초반무적", "",
			"# 초반 무적 설정"),
	Game_Invincibility_Enable("게임.초반무적.활성화", true,
			"# 초반 무적 활성화 여부"),
	Game_Invincibility_Duration("게임.초반무적.지속시간", 5,
			"# 초반 무적 활성화 지속 시간 (단위: 분)"),
	Game_Kit("게임.기본템", FileManager.getItemStackList(new ItemStack(Material.DIAMOND_SWORD)),
			"# 기본템 설정");
	
	String Path;
	Object Default;
	String[] Comments;
	
	ConfigNodes(String Path, Object Default, String... Comments) {
		
		this.Path = Path;
		this.Default = Default;
		this.Comments = Comments;
	}
	
	public String getPath() {
		return Path;
	}
	
	public Object getDefault() {
		return Default;
	}
	
	public String[] getComments() {
		return Comments;
	}
	
}
