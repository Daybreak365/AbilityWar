package Marlang.AbilityWar.Game.Script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import Marlang.AbilityWar.Game.Games.AbstractGame;
import Marlang.AbilityWar.Game.Script.Objects.AbstractScript;
import Marlang.AbilityWar.Game.Script.Objects.Setter.Setter;
import Marlang.AbilityWar.Game.Script.ScriptException.State;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Data.FileManager;
import Marlang.AbilityWar.Utils.Thread.AbilityWarThread;

/**
 * 스크립트
 * @author _Marlang 말랑
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
		Scripts.clear();
		
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
	public static void registerScript(Class<? extends AbstractScript> clazz, RequiredData<?>... requiredDatas) throws IllegalArgumentException {
		for(ScriptRegisteration check : ScriptTypes) {
			if(check.getClazz().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
				throw new IllegalArgumentException("이미 사용중인 스크립트 이름입니다.");
			}
		}
		
		if(!isRegistered(clazz)) {
			ScriptTypes.add(new ScriptRegisteration(clazz, requiredDatas));
		} else {
			throw new IllegalArgumentException("이미 등록된 스크립트입니다.");
		}
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
	
	/**
	 * 스크립트 저장
	 */
	public static void Save(AbstractScript script) {
		try {
			if(isRegistered(script.getClass())) {
				FileManager.getFolder("Script");
				File f = FileManager.getFile("Script/" + script.getScriptName() + ".yml");
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(f));
				output.writeObject(script);
				output.flush();
				output.close();
			} else {
				throw new ClassNotFoundException();
			}
		} catch (IOException ioException) {
			Messager.sendErrorMessage("스크립트를 저장하는 도중 오류가 발생하였습니다.");
		} catch (ClassNotFoundException e) {
			Messager.sendErrorMessage("등록되지 않은 스크립트입니다.");
		}
	}
	
	/**
	 * 스크립트 불러오기
	 */
	public static AbstractScript Load(File file) throws ScriptException {
		try {
			if(file.exists()) {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
				Object obj = input.readObject();
				input.close();
				
				if(obj != null) {
					if(obj instanceof AbstractScript) {
						return (AbstractScript) obj;
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
			throw new ScriptException(State.Not_Loaded);
		}
	}
	
}
