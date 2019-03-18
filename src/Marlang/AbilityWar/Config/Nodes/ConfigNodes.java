package Marlang.AbilityWar.Config.Nodes;

import org.bukkit.Bukkit;

import Marlang.AbilityWar.Game.Games.Game;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Data.FileManager;

public enum ConfigNodes {
	
	Game_NoHunger("게임.배고픔", true,
			"# 배고픔 무제한 활성화 여부"),
	Game_StartLevel("게임.레벨", 180,
			"# 게임 시작시 지급할 레벨",
			"# 0으로 설정하면 지급되지 않음"),
	Game_Invincibility_Enable("게임.초반무적.활성화", true,
			"# 초반 무적 활성화 여부"),
	Game_Invincibility_Duration("게임.초반무적.지속시간", 5,
			"# 초반 무적 활성화 지속 시간 (단위: 분)"),
	Game_Kit("게임.기본템", FileManager.getItemStackList(),
			"# 기본템 설정"),
	Game_InventoryClear("게임.인벤토리초기화", true,
			"# 게임 시작시 인벤토리 초기화 여부"),
	Game_DrawAbility("게임.능력추첨", true,
			"# 게임 시작시 능력 추첨 여부"),
	Game_InfiniteDurability("게임.내구도무한", false,
			"# 내구도 무한 여부"),
	Game_Firewall("게임.방화벽", true,
			"# 방화벽 활성화 여부",
			"# true로 설정하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다.",
			"# 관리자 권한을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
	Game_Death_Header("게임.사망", "",
			"# 플레이어 사망 콘피그"),
	Game_Deaeth_Eliminate("게임.사망.탈락", true,
			"# 게임 진행 중 사망 시 탈락 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 탈락합니다.",
			"# 탈락한 유저는 게임이 끝날 때까지 서버에 접속할 수 없습니다.",
			"# 관리자 권한을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
	Game_Deaeth_AbilityReveal("게임.사망.능력공개", true,
			"# 게임 진행 중 사망 시 능력 공개 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다."),
	Game_Deaeth_ItemDrop("게임.사망.아이템드롭", true,
			"# 게임 진행 중 사망 시 아이템 드롭 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 아이템을 드롭합니다."),
	Game_ClearWeather("게임.맑은날씨", false,
			"# 맑은 날씨 고정 여부",
			"# true로 설정하면 게임이 진행되는 동안 맑은 날씨로 고정됩니다."),
	Game_Spawn_Location("게임.스폰.위치", Bukkit.getWorlds().get(0).getSpawnLocation(),
			"# 스폰 위치 설정"),
	Game_Spawn_Enable("게임.스폰.이동", true,
			"# 초반 스폰 이동 활성화 여부"),
	Game_VisualEffect("게임.시각효과", true,
			"# 파티클 활성화 여부"),
	Game_BlackList("게임.블랙리스트", Messager.getStringList(),
			"# 능력을 추첨할 때 사용하지 않을 능력을 설정합니다."),
	Game_Mode("게임.모드", Game.class.getName(),
			"# 게임 모드 클래스");
	
	private String Path;
	private Object Default;
	private String[] Comments;
	
	private ConfigNodes(String Path, Object Default, String... Comments) {
		
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
