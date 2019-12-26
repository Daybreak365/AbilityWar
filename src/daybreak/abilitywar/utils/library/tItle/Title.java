package daybreak.abilitywar.utils.library.tItle;

import org.bukkit.entity.Player;

/**
 * 제목 메시지
 *
 * @author Daybreak 새벽
 */
public class Title extends AbstractTitle {

	private String Title;
	private String SubTitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;

	/**
	 * 제목 메시지
	 *
	 * @param Title    제목
	 * @param SubTitle 부제목
	 * @param fadeIn   FadeIn 시간 (틱 단위)
	 * @param stay     Stay 시간 (틱 단위)
	 * @param fadeOut  FadeOut 시간 (틱 단위)
	 */
	public Title(String Title, String SubTitle, int fadeIn, int stay, int fadeOut) {
		this.Title = Title;
		this.SubTitle = SubTitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	public void sendTo(Player p) {
		p.sendTitle(Title, SubTitle, fadeIn, stay, fadeOut);
	}

}
