package daybreak.abilitywar.config.enums;

import org.bukkit.Bukkit;

import daybreak.abilitywar.game.games.defaultgame.DefaultGame;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.database.FileManager;

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
	Game_Death_Operation("게임.사망.작업", "탈락",
			"# 게임 진행 중 플레이어 사망 시 수행할 작업을 설정합니다.",
			"# 탈락		: 플레이어를 탈락시킵니다.",
			"# 관전모드	: 플레이어를 관전 모드로 전환합니다.",
			"# 없음		: 아무 작업도 하지 않습니다."),
	Game_Death_AbilityReveal("게임.사망.능력공개", true,
			"# 게임 진행 중 사망 시 능력 공개 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다."),
	Game_Death_AbilityRemoval("게임.사망.능력삭제", false,
			"# 게임 진행 중 사망 시 능력 삭제 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 삭제합니다."),
	Game_Death_ItemDrop("게임.사망.아이템드롭", true,
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
	Game_BlackList("게임.블랙리스트", Messager.asList(),
			"# 능력을 추첨할 때 사용하지 않을 능력을 설정합니다."),
	Game_WRECK("게임.WRECK", false,
			"# true로 설정하면",
			"# W onderful",
			"# R ollicking",
			"# E xciting",
			"# C hizzy",
			"# K icking",
			"# 모드를 활성화합니다.",
			"# 모든 능력의 쿨타임 90% 감소"),
	AbilityChangeGame_Period("체인지능력전쟁.주기", 20,
			"# 능력 변경 주기 (단위: 초)"),
	AbilityChangeGame_Life("체인지능력전쟁.생명", 3,
			"# 죽었을 때 다시 태어날 수 있는 횟수"),
	AbilityChangeGame_Eliminate("체인지능력전쟁.탈락", true,
			"# 생명이 다했을 경우 탈락 여부",
			"# 탈락한 유저는 게임이 끝날 때까지 서버에 접속할 수 없습니다.",
			"# 관리자 권한을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
	SummerVacation_Kill("신나는여름휴가.킬횟수", 10,
			"# 우승하기 위해 필요한 킬 횟수"),
	GameMode("게임모드", DefaultGame.class.getName(),
			"# 게임 모드 클래스");
	
	private final String Path;
	private final Object Default;
	private final String[] Comments;
	
	private ConfigNodes(final String Path, final Object Default, final String... Comments) {
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
