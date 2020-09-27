package daybreak.abilitywar.utils.base;

public class TimeUtil {

	public static String parseTimeAsString(int seconds) {
		int hour = seconds / 3600;
		seconds -= hour * 3600;
		int minute = seconds / 60;
		seconds -= minute * 60;

		return (hour != 0 ? hour + "시간 " : "") + (minute != 0 ? minute + "분 " : "") + (seconds >= 0 ? seconds + "초" : "");
	}

	public static int[] parseTime(int seconds) {
		int[] time = new int[2];
		time[0] = seconds / 60;
		seconds -= time[0] * 60;
		time[1] = seconds;

		return time;
	}

	public static class ParsedTime {

		public static ParsedTime parse(final int seconds) {
			return new ParsedTime(seconds);
		}

		public final int minutes, seconds;

		private ParsedTime(int seconds) {
			this.minutes = seconds / 60;
			seconds -= this.minutes * 60;
			this.seconds = seconds;
		}

	}

}
