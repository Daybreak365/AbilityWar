package Marlang.AbilityWar.Config;

import java.util.ArrayList;

import Marlang.AbilityWar.Utils.Data.FileManager;

/**
 * 능력 세부 설정
 * @author _Marlang 말랑
 */
public class AbilitySettings {
	
	//TODO: 능력 세부 설정 편집기 만들기
	
	private static ArrayList<SettingObject<?>> Settings = new ArrayList<SettingObject<?>>();
	
	private static void registerSetting(SettingObject<?> object) {
		if(!Settings.contains(object)) {
			Settings.add(object);
		}
	}
	
	private static CommentedConfiguration Config = new CommentedConfiguration(FileManager.getFile("Ability.yml"));
	
	public static void Setup() {
		CommentedConfiguration newConfig = new CommentedConfiguration(FileManager.getFile("Ability.yml"));
		Config.load();
		
		for(SettingObject<?> Setting : Settings) {
			newConfig.set(Setting.getPath(), Setting.getValue());
			newConfig.addComment(Setting.getPath(), Setting.getComments());
		}
		
		Config = newConfig;
		
		Config.save();
		newConfig = null;
	}
	
	abstract public static class SettingObject<T> {
		
		private String Path;
		private T Default;
		private String[] Comments;
		
		public SettingObject(String AbilityName, String Path, T Default, String... Comments) {
			this.Path = "능력." + AbilityName + "." + Path;
			this.Default = Default;
			this.Comments = Comments;
			
			registerSetting(this);
		}
		
		public String getPath() {
			return Path;
		}
		
		public T getDefault() {
			return Default;
		}
		
		public String[] getComments() {
			return Comments;
		}
		
		abstract public boolean Condition(T value);
		
		@SuppressWarnings("unchecked")
		public T getValue() {
			Object o = Config.get(getPath());
			
			if(o != null && o.getClass().isAssignableFrom(getDefault().getClass())) {
				if(Condition((T) o)) {
					return (T) o;
				} else {
					return getDefault();
				}
			} else {
				return getDefault();
			}
		}
		
	}
	
	public static void Refresh() {
		Config.load();
		Setup();
	}
	
}
