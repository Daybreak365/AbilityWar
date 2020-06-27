package daybreak.abilitywar.game.list.mix.synergy;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.io.FileUtil;

public class Synergy extends AbilityBase {

	public static final AbilitySettings synergySettings = new AbilitySettings(FileUtil.newFile("Mix/synergysettings.yml"));

	protected Synergy(Participant participant) {
		super(participant);
	}

}
