package daybreak.abilitywar.addon.installer.info;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface AddonInfo {

	ImmutableList<AddonVersion> getVersions();
	AddonVersion getVersion(final String tag);
	AddonVersion getLatest();
	String getName();
	String getDisplayName();
	String getDeveloper();
	List<Link> getDeveloperLinks();
	String getIcon();
	List<String> getDescription();

	class Link {
		private final String name, link;

		Link(final JsonObject json) {
			this.name = json.get("name").getAsString();
			this.link = json.get("link").getAsString();
		}

		public String getName() {
			return name;
		}

		public String getLink() {
			return link;
		}
	}

	interface AddonVersion {
		String getName();
		String getTag();
		boolean isPrerelease();
		URL getFileURL();
		int getFileSize();
		String[] getUpdates();
		void install() throws IOException;
	}

}
