package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.AbilityWarThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityManifest(Name = "유명 인사", Rank = Rank.D, Species = Species.HUMAN)
public class Celebrity extends AbilityBase {

	public static final SettingObject<Integer> CooldownConfig = new SettingObject<Integer>(Celebrity.class, "Cooldown", 25,
			"# 쿨타임") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 0;
		}
		
	};

	public Celebrity(Participant participant) {
		super(participant,
				ChatColor.translateAlternateColorCodes('&', "&f철괴를 우클릭하면 모든 플레이어가 자신의 방향을 바라봅니다. " + Messager.formatCooldown(CooldownConfig.getValue())));
	}
	
	private final CooldownTimer Cool = new CooldownTimer(CooldownConfig.getValue());
	
	@Override
	public boolean ActiveSkill(Material materialType, ClickType ct) {
		if(materialType.equals(Material.IRON_INGOT)) {
			if(ct.equals(ClickType.RIGHT_CLICK)) {
				if(!Cool.isCooldown()) {
					
					if(AbilityWarThread.isGameTaskRunning()) {
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&f안녕하세요, 여러분! 전 세계적으로 &c선풍적인 &f인기를 끌고있는 &e" + getPlayer().getName() + "&f입니다! @==(^o^)@"));
						for(Participant participant : AbilityWarThread.getGame().getParticipants()) {
							Player p = participant.getPlayer();
							if(!p.equals(getPlayer())) {
								Vector vector = getPlayer().getLocation().toVector().subtract(p.getLocation().toVector());
								p.teleport(p.getLocation().setDirection(vector));
							}
						}
					}
					
					Cool.startTimer();
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void TargetSkill(Material materialType, LivingEntity entity) {}
	
}
