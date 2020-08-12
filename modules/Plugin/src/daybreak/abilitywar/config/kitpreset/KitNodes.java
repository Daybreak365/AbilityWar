package daybreak.abilitywar.config.kitpreset;

import daybreak.abilitywar.config.interfaces.Cacher;
import daybreak.abilitywar.config.interfaces.Node;
import daybreak.abilitywar.config.serializable.AbilityKit;
import daybreak.abilitywar.config.serializable.KitPreset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.Nullable;

public enum KitNodes implements Node {

	KIT("기본아이템", Collections.emptyMap(), new Cacher() {
		@Override
		public Object toCache(Object object) {
			try {
				if (object instanceof MemorySection) {
					return new KitPreset((MemorySection) object);
				} else if (object instanceof Map<?, ?>) {
					return new KitPreset((Map<?, ?>) object);
				} else {
					return new KitPreset();
				}
			} catch (NullPointerException | ClassCastException e) {
				return new KitPreset();
			}
		}

		@Override
		public Object revertCache(Object object) {
			return ((KitPreset) object).toMap();
		}
	}, "# 게임 기본 아이템 설정"),
	KIT_PRESET("킷프리셋", new ArrayList<>(), new Cacher() {
		@SuppressWarnings("unchecked")
		@Override
		public Object toCache(Object object) {
			try {
				final List<KitPreset> kitPresets = new ArrayList<>();
				for (Map<?, ?> section : ((List<Map<?, ?>>) object)) {
					try {
						kitPresets.add(new KitPreset(section));
					} catch (NullPointerException | ClassCastException ignored) {}
				}
				return kitPresets;
			} catch (NullPointerException | ClassCastException e) {
				return new ArrayList<>();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object revertCache(Object object) {
			final List<Map<String, Object>> list = new ArrayList<>();
			for (KitPreset kitPreset : ((List<KitPreset>) object)) {
				list.add(kitPreset.toMap());
			}
			return list;
		}
	}, "# 기본 아이템 프리셋"),
	ABILITY_KIT("능력킷", new AbilityKit(), "# 능력 별 기본 아이템 설정");

	private final String path;
	private final Object defaultValue;
	private final Cacher cacher;
	private final String[] comments;

	KitNodes(final String path, final Object defaultValue, final Cacher cacher, final String... comments) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.cacher = cacher;
		this.comments = comments;
	}

	KitNodes(final String path, final Object defaultValue, final String... comments) {
		this(path, defaultValue, null, comments);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Object getDefault() {
		return defaultValue;
	}

	@Override
	public boolean hasCacher() {
		return cacher != null;
	}

	@Override
	@Nullable
	public Cacher getCacher() {
		return cacher;
	}

	@Override
	public String[] getComments() {
		return comments;
	}
}
