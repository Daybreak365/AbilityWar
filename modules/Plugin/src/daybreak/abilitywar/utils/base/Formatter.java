package daybreak.abilitywar.utils.base;

import com.google.common.base.Strings;
import daybreak.abilitywar.Provider;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration.Tip;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.team.interfaces.Members;
import daybreak.abilitywar.utils.base.color.RainbowText;
import daybreak.abilitywar.utils.installer.Installer.VersionObject;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class Formatter {

	private Formatter() {
	}

	/**
	 * @param bracketColor 괄호 색
	 * @param titleColor   제목 색
	 * @param title        제목
	 */
	public static String formatTitle(int length, ChatColor bracketColor, ChatColor titleColor, String title) {
		String base = Strings.repeat("_", length);
		int pivot = base.length() / 2;
		String center = "[ " + titleColor + title + bracketColor + " ]§m§l";
		String result = bracketColor + "§m§l" + base.substring(0, Math.max(0, (pivot - center.length() / 2))) + "§r" + bracketColor;
		result += center + base.substring(pivot + center.length() / 2);
		return result;
	}

	/**
	 * @param bracketColor 괄호 색
	 * @param titleColor   제목 색
	 * @param title        제목
	 */
	public static String formatTitle(ChatColor bracketColor, ChatColor titleColor, String title) {
		return formatTitle(49, bracketColor, titleColor, title);
	}

	/**
	 * 설치 설명을 구성합니다.
	 */
	public static List<String> formatVersionInfo(VersionObject update) {
		List<String> info = new ArrayList<>();
		info.add(formatTitle(ChatColor.DARK_GREEN, ChatColor.GREEN, "버전 정보"));
		info.add("§b" + update.getTag() + " §f릴리즈 §f(§7v" + update.getVersion() + "§f) " + "(§7" + (update.getFileSize() / 1024) + "KB§f)");
		Collections.addAll(info, update.getUpdates());
		info.add("§2-----------------------------------------------------");
		return info;
	}

	/**
	 * 능력 설명을 구성합니다.
	 */
	public static List<String> formatAbilityInfo(AbilityBase ability) {
		final Provider provider = ability.getRegistration().getProvider();
		final String providerName = provider instanceof Addon ? ((Addon) provider).getDisplayName() : null;
		final List<String> list = Messager.asList(
				formatTitle(32, ChatColor.GREEN, ChatColor.YELLOW, "능력 정보"),
				"§b" + ability.getName() + " " + (ability.isRestricted() ? "§f[§7능력 비활성화됨§f]" : "§f[§a능력 활성화됨§f]") + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName() + (providerName != null ? " §7| §f" + providerName : ""));
		for (Iterator<String> iterator = ability.getExplanation(); iterator.hasNext(); ) {
			if (Seasons.isAprilFools()) {
				list.add(new RainbowText(ChatColor.stripColor(iterator.next())).getText());
			} else {
				list.add(iterator.next());
			}
		}
		list.add("§a---------------------------------");
		return list;
	}

	public static List<String> formatSummarize(AbilityBase ability) {
		final Provider provider = ability.getRegistration().getProvider();
		final String providerName = provider instanceof Addon ? ((Addon) provider).getDisplayName() : null;
		final List<String> list = Messager.asList(
				formatTitle(32, ChatColor.GREEN, ChatColor.YELLOW, "능력 요약"),
				"§b" + ability.getName() + " " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName() + (providerName != null ? " §7| §f" + providerName : ""));
		for (Iterator<String> iterator = ability.getSummarize(); iterator.hasNext(); ) {
			list.add(iterator.next());
		}
		list.add("§a---------------------------------");
		return list;
	}

	public static List<String> formatTip(AbilityRegistration registration) {
		final Tip tip = registration.getTip();
		final List<String> tips = tip != null ? tip.tips : Collections.emptyList();
		final AbilityManifest manifest = registration.getManifest();
		final List<String> messages = new ArrayList<>(3 + (tips.isEmpty() ? 1 : tips.size()));
		messages.add(formatTitle(32, ChatColor.DARK_GREEN, ChatColor.GREEN, "능력 팁"));
		messages.add("§b" + manifest.name() + " §r" + manifest.rank().getRankName() + " §r" + manifest.species().getSpeciesName());
		if (tips.isEmpty()) {
			messages.add("§f작성된 팁이 없습니다.");
		} else {
			messages.addAll(tips);
		}
		messages.add("§2---------------------------------");
		return messages;
	}

	public static List<String> formatInfo(AbilityRegistration registration) {
		final Provider provider = registration.getProvider();
		final String providerName = provider instanceof Addon ? ((Addon) provider).getDisplayName() : null;
		final AbilityManifest manifest = registration.getManifest();
		final List<String> info = new ArrayList<>(3 + manifest.explain().length);
		info.add(formatTitle(32, ChatColor.DARK_GREEN, ChatColor.GREEN, "능력 정보"));
		info.add("§b" + manifest.name() + " §r" + manifest.rank().getRankName() + " §r" + manifest.species().getSpeciesName() + (providerName != null ? " §7| §f" + providerName : ""));
		for (final Iterator<String> iterator = AbilityBase.getExplanation(registration); iterator.hasNext();) {
			info.add(ChatColor.WHITE.toString().concat(iterator.next()));
		}
		info.add("§2---------------------------------");
		return info;
	}

	/**
	 * 팀 설명을 구성합니다.
	 */
	public static List<String> formatTeamInfo(Members team) {
		List<String> info = new ArrayList<>();
		info.add(formatTitle(32, ChatColor.DARK_PURPLE, ChatColor.WHITE, "팀 정보"));
		info.add("§5팀 이름§f: §r" + team.getDisplayName() + " §r(" + team.getName() + ")");
		StringJoiner joiner = new StringJoiner(ChatColor.WHITE + ", ", ChatColor.DARK_PURPLE + "팀원" + ChatColor.WHITE + ": " + ChatColor.RESET, ChatColor.WHITE + ".");
		for (AbstractGame.Participant participant : team.getMembers()) {
			joiner.add(ChatColor.YELLOW + participant.getPlayer().getName());
		}
		info.add(joiner.toString());
		info.add("§5---------------------------------");
		return info;
	}

	/**
	 * 쿨타임 설명을 구성합니다.
	 */
	public static String formatCooldown(int cooldown) {
		return ChatColor.RED + "쿨타임 " + ChatColor.GRAY + ": " + ChatColor.WHITE + cooldown + "초";
	}

	/**
	 * 명령어 도움말을 구성합니다.
	 */
	public static String formatCommand(String label, String command, String help, boolean admin) {
		return (admin ? ChatColor.RED + "관리자: " : (ChatColor.GREEN + "유  저: ")) + ChatColor.GOLD + "/" + label + " " + ChatColor.YELLOW + command + " " + ChatColor.GRAY + ": " + ChatColor.WHITE + help;
	}

}
