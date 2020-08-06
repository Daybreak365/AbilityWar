package daybreak.abilitywar.config.enums;

import daybreak.abilitywar.config.Cacher;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.SpawnLocation;
import daybreak.abilitywar.config.serializable.team.PresetContainer;
import daybreak.abilitywar.game.list.standard.StandardGame;
import daybreak.abilitywar.utils.base.Messager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;

public enum ConfigNodes {

	GAME_NO_HUNGER("게임.배고픔", true,
			"# 배고픔 무제한 활성화 여부"),
	GAME_START_LEVEL("게임.레벨", 180,
			"# 게임 시작시 지급할 레벨",
			"# 0으로 설정하면 지급되지 않음"),
	GAME_INVINCIBILITY_ENABLE("게임.초반무적.활성화", true,
			"# 초반 무적 활성화 여부"),
	GAME_INVINCIBILITY_DURATION("게임.초반무적.지속시간", 300,
			"# 초반 무적 활성화 지속 시간 (단위: 초)"),
	GAME_INVINCIBILITY_BOSSBAR_ENABLE("게임.초반무적.보스바.활성화", false,
			"# 무적 시간을 보스바로 표시합니다.",
			"# 기본값: false"),
	GAME_INVINCIBILITY_BOSSBAR_MESSAGE("게임.초반무적.보스바.메시지", "무적 %d분 %d초 남음",
			"# 무적 제한시간이 정해져있는 경우에 보스바에 표시될 메시지를 설정합니다.",
			"# %d가 반드시 2개 들어가야합니다.",
			"# %d는 순서대로 분, 초로 치환됩니다."),
	GAME_INVINCIBILITY_BOSSBAR_INFINITE_MESSAGE("게임.초반무적.보스바.무한메시지", "무적이 적용되었습니다.",
			"# 무적 제한시간이 정해져있지 않은 경우에 보스바에 표시될 메시지를 설정합니다."),
	GAME_KIT("게임.기본템", Collections.emptyList(),
			"# 기본템 설정"),
	GAME_ABILITY_KIT("게임.능력기본템", new AbilityKit(),
			"# 능력 별 기본템 설정"),
	GAME_INVENTORY_CLEAR("게임.인벤토리초기화", true,
			"# 게임 시작시 인벤토리 초기화 여부"),
	GAME_DRAW_ABILITY("게임.능력추첨", true,
			"# 게임 시작시 능력 추첨 여부"),
	GAME_INFINITE_DURABILITY("게임.내구도무한", false,
			"# 내구도 무한 여부"),
	GAME_FIREWALL("게임.방화벽", true,
			"# 방화벽 활성화 여부",
			"# true로 설정하면 게임이 시작되고 난 후 참여자 또는 관전자가 아닌 유저는 접속할 수 없습니다.",
			"# 관리자 권한을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
	GAME_DEATH_HEADER("게임.사망", "",
			"# 플레이어 사망 콘피그"),
	GAME_DEATH_OPERATION("게임.사망.작업", OnDeath.탈락.name(),
			"# 게임 진행 중 플레이어 사망 시 수행할 작업을 설정합니다.",
			"# 탈락		: 플레이어를 탈락시킵니다.",
			"# 관전모드	: 플레이어를 관전 모드로 전환합니다.",
			"# 없음		: 아무 작업도 하지 않습니다."),
	GAME_DEATH_ABILITY_REVEAL("게임.사망.능력공개", true,
			"# 게임 진행 중 사망 시 능력 공개 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 플레이어의 능력을 공개합니다."),
	GAME_DEATH_AUTO_RESPAWN("게임.사망.자동리스폰", true,
			"# 게임 진행 중 사망 시 자동 리스폰 여부",
			"# true로 설정하면 게임이 시작되고 난 후 사망할 경우 플레이어를 자동으로 리스폰시킵니다."),
	GAME_CLEAR_WEATHER("게임.맑은날씨", false,
			"# 맑은 날씨 고정 여부",
			"# true로 설정하면 게임이 진행되는 동안 맑은 날씨로 고정됩니다."),
	GAME_ARROW_DISTANCE_PROPORTIONAL_DAMAGE("게임.원거리.거리비례대미지", false,
			"# true로 설정하면 게임이 진행되는 동안 화살의 대미지가 거리 비례 대미지로 변경됩니다.",
			"# 가까울수록 대미지가 감소하며, 대미지가 원 대미지보다 증가하지는 않습니다."),
	GAME_SPAWN_LOCATION("게임.스폰.위치", new SpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation()).toMap(), new Cacher() {
		@Override
		public Object toCache(Object object) {
			try {
				return new SpawnLocation((MemorySection) object);
			} catch (NullPointerException | ClassCastException e) {
				return new SpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
			}
		}

		@Override
		public Object revertCache(Object object) {
			return ((SpawnLocation) object).toMap();
		}
	}, "# 스폰 위치 설정"),
	GAME_SPAWN_ENABLE("게임.스폰.이동", true,
			"# 초반 스폰 이동 활성화 여부"),
	GAME_VISUAL_EFFECT("게임.시각효과", true,
			"# 파티클 활성화 여부"),
	GAME_BLACKLIST("게임.블랙리스트", Messager.asList(), new Cacher() {
		@Override
		public Object toCache(Object object) {
			return new HashSet<>((List<?>) object);
		}

		@Override
		public Object revertCache(Object object) {
			return new ArrayList<>((Set<?>) object);
		}
	}, "# 능력을 추첨할 때 사용하지 않을 능력을 설정합니다."),
	GAME_WRECK_ENABLE("게임.WRECK.활성화", false,
			"# true로 설정하면 쿨타임 감소 모드를 활성화합니다."),
	GAME_WRECK_DECREASE("게임.WRECK.감소율", CooldownDecrease._90.name(),
			"# WRECK 활성화 시 쿨타임을 얼마나 감소시킬 지 설정합니다.",
			"# _25 : 25%",
			"# _50 : 50%",
			"# _75 : 75%",
			"# _90 : 90%",
			"# _100 : 100%",
			"# 중 하나로 설정하면 됩니다."),
	GAME_ZEROTICK("게임.제로틱", false,
			"# true로 설정하면 게임 중 공격 딜레이 없이 타격할 수 있습니다."),
	GAME_TEAMGAME("게임.팀게임", false,
			"# true로 설정하면 일부 게임을 팀 게임으로 플레이할 수 있습니다."),
	GAME_TEAM_PRESETS("팀게임.팀프리셋", new PresetContainer(), "#"),
	GAME_DEFAULT_MAX_HEALTH_ENABLE("게임.최대체력.활성화", false,
			"# 게임 시작시 모든 플레이어에게 기본 최대 체력 설정 여부"),
	GAME_DEFAULT_MAX_HEALTH_VALUE("게임.최대체력.값", 20,
			"# 게임 시작시 모든 플레이어에게 설정될 최대 체력",
			"# 1 이상의 값으로 설정되어야 합니다."),
	GAME_DURATION_TIMER_BEHAVIOR("게임.지속타이머.작업", false,
			"# 게임 중 능력이 비활성화 되었을 때 지속 시간 타이머를 어떻게 처리할지 설정합니다.",
			"# false: 타이머 종료",
			"# true: 타이머 일시 정지, 능력 활성화시 타이머 재개"),
	ABILITY_CHANGE_GAME_PERIOD("체인지능력전쟁.주기", 20,
			"# 능력 변경 주기 (단위: 초)"),
	ABILITY_CHANGE_GAME_LIFE("체인지능력전쟁.생명", 3,
			"# 죽었을 때 다시 태어날 수 있는 횟수"),
	ABILITY_CHANGE_GAME_ELIMINATE("체인지능력전쟁.탈락", true,
			"# 생명이 다했을 경우 탈락 여부",
			"# 탈락한 유저는 게임이 끝날 때까지 서버에 접속할 수 없습니다.",
			"# 관리자 권한을 가지고 있을 경우 이를 무시하고 접속할 수 있습니다."),
	SUMMER_VACATION_KILL("신나는여름휴가.킬횟수", 10,
			"# 우승하기 위해 필요한 킬 횟수"),
	GAME_MODE("게임모드", StandardGame.class.getName(),
			"# 게임 모드 클래스"),
	DEVELOPER("개발자", false,
			"# 개발자 모드 활성화 여부",
			"# 디버그 게임 모드가 활성화됩니다.",
			"# 베타 능력 및 게임 모드를 사용할 수 있습니다.",
			"# 디버그 메시지가 출력됩니다.");

	private final String path;
	private final Object defaultValue;
	private final Cacher nodeHandler;
	private final String[] comments;

	ConfigNodes(String path, Object defaultValue, Cacher nodeHandler, String... comments) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.nodeHandler = nodeHandler;
		this.comments = comments;
	}

	ConfigNodes(String path, Object defaultValue, String... comments) {
		this(path, defaultValue, null, comments);
	}

	public String getPath() {
		return path;
	}

	public Object getDefault() {
		return defaultValue;
	}

	public boolean hasCacher() {
		return nodeHandler != null;
	}

	public Cacher getCacher() {
		return nodeHandler;
	}

	public String[] getComments() {
		return comments;
	}

}
