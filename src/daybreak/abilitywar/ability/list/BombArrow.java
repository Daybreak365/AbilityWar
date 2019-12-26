package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.ArrayList;
import java.util.Random;

@AbilityManifest(Name = "폭발화살", Rank = Rank.S, Species = Species.HUMAN)
public class BombArrow extends AbilityBase {

    public static final SettingObject<Integer> ChanceConfig = new SettingObject<Integer>(BombArrow.class, "Chance", 40,
            "# 화살을 맞췄을 때 몇 퍼센트 확률로 폭발을 일으킬지 설정합니다.",
            "# 40은 40%를 의미합니다.") {

        @Override
        public boolean Condition(Integer value) {
            return value >= 1 && value <= 100;
        }

    };

    public static final SettingObject<Integer> SizeConfig = new SettingObject<Integer>(BombArrow.class, "Size", 2,
            "# 화살을 맞췄을 때 얼마나 큰 폭발을 일으킬지 설정합니다.") {

        @Override
        public boolean Condition(Integer value) {
            return value >= 1;
        }

    };

    public BombArrow(Participant participant) {
        super(participant,
                ChatColor.translateAlternateColorCodes('&', "&f화살을 맞췄을 때 " + ChanceConfig.getValue() + "% 확률로 폭발을 일으킵니다."));
    }

    @Override
    public boolean ActiveSkill(Material materialType, ClickType ct) {
        return false;
    }

    private final int Chance = ChanceConfig.getValue();
    private final int Size = SizeConfig.getValue();

    private final ArrayList<Arrow> ArrowList = new ArrayList<>();

    @SubscribeEvent
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter().equals(getPlayer())) {
            if (e.getEntity() instanceof Arrow) {
                ArrowList.add((Arrow) e.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter().equals(getPlayer())) {
            if (e.getEntity() instanceof Arrow) {
                Arrow arrow = (Arrow) e.getEntity();

                if (ArrowList.contains(arrow)) {
                    ArrowList.remove(arrow);
                    Random r = new Random();

                    if ((r.nextInt(100) + 1) <= Chance) {
                        SoundLib.BLOCK_NOTE_BLOCK_BELL.playSound(getPlayer());
                        Location l = arrow.getLocation();
                        l.getWorld().createExplosion(l, Size, false);
                    }
                }
            }
        }
    }

    @Override
    public void TargetSkill(Material materialType, LivingEntity entity) {
    }

}
