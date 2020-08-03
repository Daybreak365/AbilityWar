package daybreak.abilitywar.addon.installer.info;

import com.google.gson.JsonObject;
import java.io.IOException;

public enum InfoFormat {

	GITHUB {
		@Override
		public AddonInfo createInfo(JsonObject json) throws IOException, IllegalStateException {
			return new GithubAddonInfo(json);
		}
	},
	CUSTOM {
		@Override
		public AddonInfo createInfo(JsonObject json) throws IOException, IllegalStateException {
			return new CustomAddonInfo(json);
		}
	};

	public abstract AddonInfo createInfo(final JsonObject json) throws IOException, IllegalStateException;

}
