package Marlang.AbilityWar.Game.Script;

public class ScriptException extends Exception {
	
	private static final long serialVersionUID = 1192403591954231786L;
	
	public ScriptException(State state) {
		super(state.getMessage());
	}
	
	protected enum State {
		
		Not_Loaded("스크립트를 불러오는 도중 오류가 발생하였습니다."),
		IllegalFile("스크립트 파일이 아닙니다."),
		Not_Found("스크립트를 찾을 수 없습니다.");
		
		String Message;
		
		private State(String msg) {
			this.Message = msg;
		}
		
		public String getMessage() {
			return Message;
		}
		
	}
}
