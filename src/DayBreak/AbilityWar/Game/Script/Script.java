package DayBreak.AbilityWar.Game.Script;

import static DayBreak.AbilityWar.Utils.Validate.notNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import DayBreak.AbilityWar.Game.Games.Mode.AbstractGame;
import DayBreak.AbilityWar.Game.Script.ScriptException.State;
import DayBreak.AbilityWar.Game.Script.Objects.AbstractScript;
import DayBreak.AbilityWar.Game.Script.Objects.Setter.Setter;
import DayBreak.AbilityWar.Utils.Messager;
import DayBreak.AbilityWar.Utils.ReflectionUtil.ClassUtil;
import DayBreak.AbilityWar.Utils.Data.FileManager;
import DayBreak.AbilityWar.Utils.Thread.AbilityWarThread;

/**
 * 스크립트
 * @author DayBreak 새벽
 */
abstract public class Script {
	
	private static ArrayList<AbstractScript> Scripts = new ArrayList<AbstractScript>();
	
	/**
	 * 모든 스크립트를 시작시킵니다.
	 */
	public static void RunAll(AbstractGame game) {
		if(AbilityWarThread.isGameTaskRunning()) {
			for(AbstractScript script : Scripts) {
				script.Start(game);
			}
		}
	}
	
	/**
	 * 스크립트를 추가합니다.
	 */
	public static void AddScript(AbstractScript script) {
		if(!Scripts.contains(script)) {
			Scripts.add(script);
		}
	}
	
	/**
	 * 스크립트 폴더 안에 있는 모든 스크립트를 불러옵니다.
	 */
	public static void LoadAll() {
		Scripts = new ArrayList<>();
		
		for(File file : FileManager.getFolder("Script").listFiles()) {
			try {
				AbstractScript script = Load(file);
				Scripts.add(script);
			} catch (ScriptException e) {}
		}
	}
	
	private static ArrayList<ScriptRegisteration> ScriptTypes = new ArrayList<ScriptRegisteration>();
	
	/**
	 * 스크립트 등록
	 * @throws IllegalArgumentException 등록하려는 스크립트 클래스의 이름이 다른 스크립트 클래스가
	 *                                  이미 사용하고 있는 이름일 경우,
	 *                                  이미 등록된 스크립트 클래스일 경우
	 */
	public static void registerScript(Class<? extends AbstractScript> clazz, RequiredData<?>... requiredDatas) {
		for(ScriptRegisteration check : ScriptTypes) {
			if(check.getClazz().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
				Messager.sendMessage(clazz.getName() + " 스크립트는 겹치는 이름이 있어 등록되지 않았습니다.");
				return;
			}
		}
		
		if(isRegistered(clazz)) {
			Messager.sendMessage(clazz.getName() + " 스크립트는 이미 등록되었습니다.");
			return;
		}
		
		ScriptTypes.add(new ScriptRegisteration(clazz, requiredDatas));
	}
	
	public static ScriptRegisteration getRegisteration(Class<? extends AbstractScript> clazz) throws IllegalArgumentException, ScriptException {
		if(isRegistered(clazz)) {
			for(ScriptRegisteration sr : ScriptTypes) {
				if(sr.getClazz().equals(clazz)) {
					return sr;
				}
			}
			
			throw new ScriptException(State.Not_Found);
		} else {
			throw new IllegalArgumentException("등록되지 않은 스크립트입니다.");
		}
	}
	
	public static Class<? extends AbstractScript> getScriptClass(String className) throws ClassNotFoundException {
		for(ScriptRegisteration reg : ScriptTypes) {
			if(reg.getClazz().getSimpleName().equalsIgnoreCase(className)) {
				return reg.getClazz();
			}
		}
		
		throw new ClassNotFoundException();
	}
	
	public static List<String> getRegisteredScripts() {
		List<String> list = new ArrayList<String>();
		for(ScriptRegisteration reg : ScriptTypes) {
			list.add(reg.getClazz().getSimpleName());
		}
		
		return list;
	}
	
	public static boolean isRegistered(Class<? extends AbstractScript> clazz) {
		for(ScriptRegisteration check : ScriptTypes) {
			if(check.getClazz().equals(clazz)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static class ScriptRegisteration {
		
		private final Class<? extends AbstractScript> clazz;
		private final RequiredData<?>[] requiredDatas;
		
		public ScriptRegisteration(Class<? extends AbstractScript> clazz, RequiredData<?>... requiredDatas) {
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
		
		private final String Key;
		private final Class<T> clazz;
		private final T Default;
		private final Class<? extends Setter<T>> setterClass;
		
		public RequiredData(String Key, Class<T> clazz, T Default) {
			this.Key = Key;
			this.clazz = clazz;
			this.Default = Default;
			this.setterClass = null;
		}

		public RequiredData(String Key, Class<T> clazz) {
			this.Key = Key;
			this.clazz = clazz;
			this.Default = null;
			this.setterClass = null;
		}

		public RequiredData(String Key, Class<T> clazz, Class<? extends Setter<T>> setterClass) {
			this.Key = Key;
			this.clazz = clazz;
			this.Default = null;
			this.setterClass = setterClass;
		}

		public RequiredData(String Key, Class<T> clazz, T Default, Class<? extends Setter<T>> setterClass) {
			this.Key = Key;
			this.clazz = clazz;
			this.Default = Default;
			this.setterClass = setterClass;
		}
		
		public String getKey() {
			return Key;
		}
		
		public Class<T> getClazz() {
			return clazz;
		}
		
		public T getDefault() {
			return Default;
		}

		public Class<? extends Setter<T>> getSetterClass() {
			return setterClass;
		}
		
	}
	
	private static final Gson gson = new Gson();
	private static final JsonParser parser = new JsonParser();
	
	/**
	 * {@link AbstractScript} 저장
	 */
	public static void Save(AbstractScript script) {
		try {
			if(isRegistered(script.getClass())) {
				FileManager.getFolder("Script");
				File f = FileManager.getFile("Script/" + script.getName() + ".json");
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				gson.toJson(script, bw);
				bw.close();
			} else {
				Messager.sendErrorMessage("등록되지 않은 스크립트입니다.");
			}
		} catch (IOException ioException) {
			Messager.sendErrorMessage("스크립트를 저장하는 도중 오류가 발생하였습니다.");
		}
	}
	
	/**
	 * {@link AbstractScript} 불러오기
	 */
	public static AbstractScript Load(File file) throws ScriptException {
		try {
			if(file.exists()) {
				JsonObject object = notNull(parser.parse(new BufferedReader(new FileReader(file)))).getAsJsonObject();
				Class<?> typeClass = ClassUtil.forName(object.get("scriptType").getAsString());
				BufferedReader br = new BufferedReader(new FileReader(file));
				Object script = gson.fromJson(br, typeClass);
				br.close();
				
				if(script != null) {
					if(script instanceof AbstractScript) {
						return (AbstractScript) script;
					} else {
						throw new ScriptException(State.IllegalFile);
					}
				} else {
					throw new NullPointerException();
				}
			} else {
				throw new IOException();
			}
		} catch (IOException | NullPointerException | ClassNotFoundException Exception) {
			Messager.sendErrorMessage(ChatColor.translateAlternateColorCodes('&', "&e" + file.getName() + " &f스크립트를 불러오는 도중 오류가 발생하였습니다."));
			Exception.printStackTrace();
			throw new ScriptException(State.Not_Loaded);
		}
	}
	
}
