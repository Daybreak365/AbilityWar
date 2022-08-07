package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

@AbilityManifest(name = "제논", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7패시브 §f- §3혈청 ✕§f: 매 12초마다 투여해 최대 체력의 50%만큼 §e흡수 체력§f을",
        " 회복합니다. §e흡수 체력§f이 없을 때 30%의 추가 피해를 입습니다.",
        "§7철괴 우클릭 §f- §3과다 투여§f: 다음 일곱 번의 투여 동안 투여 주기가 2초로 단축됩니다.",
        " 대신 회복하는 §e흡수 체력§f이 절반으로 감소하며, 능력 사용 시 즉시 투여합니다.",
        " 일곱 번의 투여가 끝난 후, 40초간 투여를 중단합니다."
})
@Beta
public class Xenon extends AbilityBase implements ActiveHandler {

    public Xenon(Participant participant) throws IllegalStateException {
        super(participant);
    }

    private final Serum serum = new Serum();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (!serum.overdose()) {
                getPlayer().sendMessage("§c투여 중단 상태입니다.");
            }
        }
        return false;
    }

    private class Serum extends AbilityTimer {

        private final ActionbarChannel actionbar = getParticipant().actionbar().newChannel();
        private int overdose = 0;
        private int noRegen = 12 * 20, cooldown = 0;

        private Serum() {
            super();
            setPeriod(TimeUnit.TICKS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            if (cooldown <= 0) {
                if (noRegen <= 0) {
                    actionbar.update("§3혈청 ✕ §7| §f주입");
                    final float absorption = NMS.getAbsorptionHearts(getPlayer()), max = getMaxAbsorptionHearts();
                    if (absorption == max) {
                        SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer(), 100f, .7757f);
                        if (overdose <= 0) {
                            this.noRegen = Math.max(12 * 20, this.noRegen);
                        } else {
                            this.noRegen = Math.max(2 * 20, this.noRegen);
                            if (--overdose <= 0) {
                                this.cooldown = 40 * 20;
                                this.noRegen = 0;
                            }
                        }
                        actionbar.update("§3혈청 ✕ §7| §f" + (noRegen / 2 / 10.0) + "초");
                    } else if (absorption < max) {
                        NMS.setAbsorptionHearts(getPlayer(), Math.min(max, absorption + 1));
                    }
                } else {
                    actionbar.update("§3혈청 ✕ §7| §f" + (noRegen / 2 / 10.0) + "초");
                    noRegen--;
                }
            } else {
                actionbar.update("§3혈청 ✕ §c중단 §7| §f" + (cooldown / 2 / 10.0) + "초");
                cooldown--;
            }
        }

        private boolean overdose() {
            if (cooldown > 0) return false;
            this.overdose = 7;
            NMS.setAbsorptionHearts(getPlayer(), 0);
            this.noRegen = 0;
            return true;
        }

        private float getMaxAbsorptionHearts() {
            final double maxHealth = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            final float max = (float) (maxHealth * .4);
            return overdose > 0 ? (max / 2) : max;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            actionbar.update(null);
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            serum.start();

        } else if (update == Update.ABILITY_DESTROY) {
            serum.actionbar.unregister();
            NMS.setAbsorptionHearts(getPlayer(), 0);
        }
    }
}
