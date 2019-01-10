package Marlang.AbilityWar.Config.Nodes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Utils.FileManager;

public enum ConfigNodes {
	
	Game_Header("게임", "",
			"# 모든 콘피그는 인게임에서 /va config 명령어로",
			"# 변경할 수 있습니다."),
	Game_NoHunger("게임.배고픔", true,
			"",
			"# 배고픔 무제한 활성화 여부"),
	Game_StartLevel("게임.레벨", 180,
			"",
			"# 게임 시작시 지급할 레벨",
			"# 0으로 설정하면 지급되지 않음"),
	Game_Invincibility_Enable("게임.초반무적.활성화", true,
			"# 초반 무적 활성화 여부"),
	Game_Invincibility_Duration("게임.초반무적.지속시간", 5,
			"# 초반 무적 활성화 지속 시간 (단위: 분)"),
	Game_Kit("게임.기본템", FileManager.getItemStackList(new ItemStack(Material.DIAMOND_SWORD)),
			"# 기본템 설정"),
	Game_Spawn_Location("게임.스폰.위치", Bukkit.getWorlds().get(0).getSpawnLocation(),
			"# 스폰 위치 설정"),
	Game_Spawn_Enable("게임.스폰.이동", true,
			"# 초반 스폰 이동 활성화 여부"),
	Game_OldMechanics_Header("게임.구버전", "",
			"",
			"# 마치 구 버전에서 플레이하고 있는 것처럼 느끼게 해줍니다.",
			"# 구 버전 설정은 능력자 전쟁이 시작되면 적용됩니다."),
	Game_OldMechanics_Enchant("게임.구버전.마법부여", false,
			"# 마법 부여를 청금석 없이 할 수 있게 해줍니다.");
	
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
