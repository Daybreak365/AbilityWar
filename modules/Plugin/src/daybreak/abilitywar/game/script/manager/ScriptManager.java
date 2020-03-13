package daybreak.abilitywar.game.script.manager;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.script.AbstractScript;
import daybreak.abilitywar.game.script.list.ChangeAbilityScript;
import daybreak.abilitywar.game.script.list.LocationNoticeScript;
import daybreak.abilitywar.game.script.list.TeleportScript;
import daybreak.abilitywar.game.script.manager.ScriptException.State;
import daybreak.abilitywar.game.script.setter.Setter;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil.ClassUtil;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 스크립트 관리 클래스
 *
 * @author Daybreak 새벽
 */
public class ScriptManager {

	private ScriptManager() {
	}

	private static final Set<AbstractScript> scripts = new HashSet<>();

	/**
	 * 모든 스크립트를 시작시킵니다.
	 */
	public static void runAll(Game game) {
		if (AbilityWarThread.isGameTaskRunning()) {
			for (AbstractScript script : scripts) {
				script.start(game);
			}
		}
	}

	/**
	 * 스크립트를 추가합니다.
	 */
	public static boolean addScript(AbstractScript script) {
		return scripts.add(script);
	}

	/**
	 * 스크립트 폴더 안에 있는 모든 스크립트를 불러옵니다.
	 */
	public static void loadAll() {
		scripts.clear();

		for (File file : FileUtil.newDirectory("Script").listFiles()) {
			try {
				AbstractScript script = Load(file);
				scripts.add(script);
			} catch (ScriptException ignore) {
			}
		}
	}

	private static final Map<String, Class<? extends AbstractScript>> usedNames = new HashMap<>();
	private static Map<Class<? extends AbstractScript>, ScriptRegistration> scriptTypes = new HashMap<>();

	/**
	 * 스크립트 등록
	 *
	 * @throws IllegalArgumentException 등록하려는 스크립트 클래스의 이름이 다른 스크립트 클래스가 이미 사용하고 있는
	 *                                  이름일 경우, 이미 등록된 스크립트 클래스일 경우
	 */
	public static void registerScript(Class<? extends AbstractScript> clazz, RequiredData<?>... requiredDatas) {
		if (usedNames.containsKey(clazz.getSimpleName())) {
			Messager.sendConsoleMessage(clazz.getName() + " 스크립트는 겹치는 이름이 있어 등록되지 않았습니다.");
			return;
		}
		if (isRegistered(clazz)) {
			Messager.sendConsoleMessage(clazz.getName() + " 스크립트는 이미 등록되었습니다.");
			return;
		}

		usedNames.put(clazz.getSimpleName(), clazz);
		scriptTypes.put(clazz, new ScriptRegistration(clazz, requiredDatas));
	}

	static {
		ScriptManager.registerScript(TeleportScript.class, new RequiredData<>("텔레포트 위치", Location.class));
		ScriptManager.registerScript(ChangeAbilityScript.class, new RequiredData<>("능력 변경 대상", ChangeAbilityScript.ChangeTarget.class));
		ScriptManager.registerScript(LocationNoticeScript.class);
	}

	public static ScriptRegistration getRegistration(Class<? extends AbstractScript> clazz) throws IllegalArgumentException {
		if (isRegistered(clazz)) return scriptTypes.get(clazz);
		else throw new IllegalArgumentException("등록되지 않은 스크립트입니다.");
	}

	public static Class<? extends AbstractScript> getScriptClass(String name) {
		return usedNames.get(name);
	}

	public static Set<String> getRegisteredScriptNames() {
		return usedNames.keySet();
	}

	public static boolean isRegistered(Class<? extends AbstractScript> clazz) {
		return scriptTypes.containsKey(clazz);
	}

	public static class ScriptRegistration {

		private final Class<? extends AbstractScript> clazz;
		private final RequiredData<?>[] requiredDatas;

		public ScriptRegistration(Class<? extends AbstractScript> clazz, RequiredData<?>... requiredDatas) {
			this.clazz = clazz;
			this.requiredDatas = requiredDatas;
		}

		public Class<? extends AbstractScript> getClazz() {
			return clazz;
		}

		public RequiredData<?>[] getRequiredDatas() {
			return requiredDatas;
		}

	}

	public static class RequiredData<T> {

		private final String key;
		private final Class<T> clazz;
		private final T defaultVaule;
		private final Class<? extends Setter<T>> setterClass;

		public RequiredData(String key, Class<T> clazz, T defaultVaule, Class<? extends Setter<T>> setterClass) {
			this.key = key;
			this.clazz = clazz;
			this.defaultVaule = defaultVaule;
			this.setterClass = setterClass;
		}

		public RequiredData(String key, Class<T> clazz, Class<? extends Setter<T>> setterClass) {
			this(key, clazz, null, setterClass);
		}

		public RequiredData(String key, Class<T> clazz, T defaultVaule) {
			this(key, clazz, defaultVaule, null);
		}

		public RequiredData(String key, Class<T> clazz) {
			this(key, clazz, null, null);
		}

		public String getKey() {
			return key;
		}

		public Class<T> getClazz() {
			return clazz;
		}

		public T getDefault() {
			return defaultVaule;
		}

		public Class<? extends Setter<T>> getSetterClass() {
			return setterClass;
		}

	}

	private static final Gson gson = new Gson();

	/**
	 * {@link AbstractScript} 저장
	 */
	public static void Save(AbstractScript script) {
		try {
			if (isRegistered(script.getClass())) {
				FileUtil.newDirectory("Script");
				File f = FileUtil.newFile("Script/" + script.getName() + ".json");
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				gson.toJson(script, bw);
				bw.close();
			} else {
				Messager.sendConsoleErrorMessage("등록되지 않은 스크립트입니다.");
			}
		} catch (IOException ioException) {
			Messager.sendConsoleErrorMessage("스크립트를 저장하는 도중 오류가 발생하였습니다.");
		}
	}

	/**
	 * {@link AbstractScript} 불러오기
	 */
	public static AbstractScript Load(File file) throws ScriptException {
		try {
			if (file.exists()) {
				JsonObject object = Preconditions.checkNotNull(JsonParser.parseReader(new BufferedReader(new FileReader(file)))).getAsJsonObject();
				Class<?> typeClass = ClassUtil.forName(object.get("scriptType").getAsString());
				BufferedReader br = new BufferedReader(new FileReader(file));
				Object script = gson.fromJson(br, typeClass);
				br.close();

				if (script != null) {
					if (script instanceof AbstractScript) {
						return (AbstractScript) script;
					} else {
						throw new ScriptException(State.ILLEGAL_FILE);
					}
				} else {
					throw new NullPointerException();
				}
			} else {
				throw new IOException();
			}
		} catch (IOException | NullPointerException | ClassNotFoundException e) {
			Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + file.getName() + " &f스크립트를 불러오는 도중 오류가 발생하였습니다."));
			throw new ScriptException(State.NOT_LOADED);
		}
	}

}
