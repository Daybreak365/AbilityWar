package daybreak.abilitywar.game.manager.effect.registry;

import com.google.common.base.Preconditions;
import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.event.ParticipantEffectApplyEvent;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EffectRegistry {

	private EffectRegistry() {}

	private static final Map<String, EffectRegistration<?>> usedNames = new LinkedHashMap<>();
	private static final Map<Class<? extends Effect>, EffectRegistration<?>> registered = new HashMap<>();

	public static <E extends Effect> EffectRegistration<E> registerEffect(final Class<E> clazz) {
		if (registered.containsKey(clazz)) throw new IllegalArgumentException(clazz.getName() + " 효과는 이미 등록되었습니다.");
		try {
			final EffectRegistration<E> registration = new EffectRegistration<>(clazz);
			final String name = registration.getManifest().name();
			if (usedNames.containsKey(name)) throw new IllegalArgumentException("이미 사용 중인 효과 이름: " + name);
			registered.put(clazz, registration);
			usedNames.put(name, registration);
			return registration;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("(Participant, TimeUnit, int) 생성자가 존재하지 않는 효과입니다.");
		}
	}

	@Nullable
	public static EffectRegistration<?> getRegistration(final Class<? extends Effect> clazz) {
		return registered.get(clazz);
	}

	public static class EffectRegistration<E extends Effect> {

		private final Class<E> clazz;
		private final @Nullable Constructor<E> constructor;
		private final Map<String, Constructor<E>> constructors = new HashMap<>();
		private final EffectManifest manifest;

		private EffectRegistration(Class<E> clazz) throws NoSuchMethodException {
			this.clazz = Preconditions.checkNotNull(clazz);
			{
				Constructor<E> constructor;
				try {
					constructor = clazz.getConstructor(Participant.class, TimeUnit.class, int.class);
				} catch (NoSuchMethodException e) {
					constructor = null;
				}
				this.constructor = constructor;
			}
			for (Constructor<?> wild : clazz.getConstructors()) {
				final EffectConstructor annotation = wild.getAnnotation(EffectConstructor.class);
				if (annotation == null) continue;
				try {
					final Constructor<E> constructor = (Constructor<E>) wild;
					final Class<?>[] types = constructor.getParameterTypes();
					if (types.length >= 4 && types[0] == Participant.class && types[1] == TimeUnit.class && types[2] == int.class) {
						constructors.put(annotation.name(), constructor);
					}
				} catch (ClassCastException ignored) {}
			}
			final EffectManifest manifest = clazz.getAnnotation(EffectManifest.class);
			if (manifest == null) throw new IllegalArgumentException("EffectManifest가 없는 효과입니다.");
			this.manifest = manifest;
			Preconditions.checkNotNull(manifest.name());
		}

		public Class<? extends Effect> getEffectClass() {
			return clazz;
		}

		public boolean isTypeOf(final Class<? extends Effect> effectClass) {
			return effectClass.isAssignableFrom(this.clazz);
		}

		public Constructor<? extends Effect> getConstructor() {
			return constructor;
		}

		public EffectManifest getManifest() {
			return manifest;
		}

		public E apply(final @NotNull Participant participant, final @NotNull TimeUnit timeUnit, final int duration) {
			final ParticipantEffectApplyEvent event = new ParticipantEffectApplyEvent(participant, this, timeUnit, duration);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return null;
			final E applied = manifest.method().tryApply(this, participant, TimeUnit.TICKS, event.getDuration());
			if (applied != null) return applied;
			try {
				if (constructor != null) {
					final E newEffect = constructor.newInstance(participant, TimeUnit.TICKS, event.getDuration());
					newEffect.start();
					return newEffect;
				} else return null;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		public E apply(final @NotNull Participant participant, final @NotNull TimeUnit timeUnit, final int duration, final String constructorName, final Object... args) {
			final ParticipantEffectApplyEvent event = new ParticipantEffectApplyEvent(participant, this, timeUnit, duration);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return null;
			final E applied = manifest.method().tryApply(this, participant, TimeUnit.TICKS, event.getDuration());
			if (applied != null) return applied;
			try {
				final Constructor<E> constructor = constructors.get(constructorName);
				if (constructor != null) {
					final Object[] array = new Object[3 + args.length];
					array[0] = participant;
					array[1] = TimeUnit.TICKS;
					array[2] = event.getDuration();
					System.arraycopy(args, 0, array, 3, args.length);
					final E newEffect = constructor.newInstance(array);
					newEffect.start();
					return newEffect;
				} else return null;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

}
