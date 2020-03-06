package daybreak.abilitywar.game.script.manager;

public class ScriptException extends Exception {

	private static final long serialVersionUID = 1192403591954231786L;

	ScriptException(State state) {
		super(state.getMessage());
	}

	enum State {

		NOT_LOADED("스크립트를 불러오는 도중 오류가 발생하였습니다."),
		ILLEGAL_FILE("스크립트 파일이 아닙니다."),
		NOT_FOUND("스크립트를 찾을 수 없습니다.");

		String Message;

		State(String msg) {
			this.Message = msg;
		}

		public String getMessage() {
			return Message;
		}

	}
}
