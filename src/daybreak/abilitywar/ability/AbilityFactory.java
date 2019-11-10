package daybreak.abilitywar.ability;

import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.config.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.games.mode.AbstractGame.Participant;
import daybreak.abilitywar.game.games.squirtgunfight.SquirtGun;
import daybreak.abilitywar.utils.Messager;
import daybreak.abilitywar.utils.thread.TimerBase;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

/**
 * {@link AbilityBase}를 기반으로 하는 모든 능력을 관리하는 클래스입니다.
 */
public class AbilityFactory {

    private AbilityFactory() {}

    private static final HashMap<String, Class<? extends AbilityBase>> usedNames = new HashMap<>();
    private static final HashMap<Class<? extends AbilityBase>, AbilityRegistration<? extends AbilityBase>> registeredAbilities = new HashMap<>();

    /**
     * 능력을 등록합니다.
     * <p>
     * 능력을 등록하기 전, AbilityManifest 어노테이션이 클래스에 존재하는지, 겹치는 이름은 없는지, 생성자는 올바른지 확인해주시길
     * 바랍니다.
     * <p>
     * 이미 등록된 능력일 경우 다시 등록이 되지 않습니다.
     *
     * @param abilityClass 능력 클래스
     */
    public static void registerAbility(Class<? extends AbilityBase> abilityClass) {
        if (!registeredAbilities.containsKey(abilityClass)) {
            try {
                AbilityRegistration<?> registeration = new AbilityRegistration<>(abilityClass);
                String name = registeration.getManifest().Name();
                if (!usedNames.containsKey(name)) {
                    usedNames.put(name, abilityClass);
                    registeredAbilities.put(abilityClass, registeration);

                    for (Field field : abilityClass.getFields()) {
                        if (field.getType().equals(SettingObject.class) && Modifier.isStatic(field.getModifiers())) {
                            field.get(null);
                        }
                    }
                } else {
                    Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
                            "&e" + abilityClass.getName() + " &f능력은 겹치는 이름이 있어 등록되지 않았습니다."));
                }
            } catch (Exception ex) {
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    Messager.sendConsoleErrorMessage(ex.getMessage());
                } else {
                    Messager.sendConsoleErrorMessage(ChatColor.translateAlternateColorCodes('&',
                            "&e" + abilityClass.getName() + " &f능력 등록중 오류가 발생하였습니다."));
                }
            }
        }
    }

    public static AbilityRegistration<?> getRegisteration(Class<? extends AbilityBase> clazz) {
        return registeredAbilities.get(clazz);
    }

    public static boolean isRegistered(Class<? extends AbilityBase> clazz) {
        return registeredAbilities.containsKey(clazz);
    }

    private static boolean containsName(String name) {
        for (AbilityRegistration<?> r : registeredAbilities.values()) {
            AbilityManifest manifest = r.getManifest();
            if (manifest.Name().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    static {
        // 초창기 능력자
        registerAbility(Assassin.class);
        registerAbility(Feather.class);
        registerAbility(Demigod.class);
        registerAbility(FastRegeneration.class);
        registerAbility(EnergyBlocker.class);
        registerAbility(DiceGod.class);
        registerAbility(Ares.class);
        registerAbility(Zeus.class);
        registerAbility(Berserker.class);
        registerAbility(Zombie.class);
        registerAbility(Terrorist.class);
        registerAbility(Yeti.class);
        registerAbility(Gladiator.class);
        registerAbility(Chaos.class);
        registerAbility(Void.class);
        registerAbility(DarkVision.class);
        registerAbility(HigherBeing.class);
        registerAbility(BlackCandle.class);
        registerAbility(FireFightWithFire.class);
        registerAbility(Hacker.class);
        registerAbility(Muse.class);
        registerAbility(Chaser.class);
        registerAbility(Flora.class);
        registerAbility(ShowmanShip.class);
        registerAbility(Virtus.class);
        registerAbility(Nex.class);
        registerAbility(Ira.class);
        registerAbility(OnlyOddNumber.class);
        registerAbility(Clown.class);
        registerAbility(TheMagician.class);
        registerAbility(TheHighPriestess.class);
        registerAbility(TheEmpress.class);
        registerAbility(TheEmperor.class);
        registerAbility(Pumpkin.class);
        registerAbility(Virus.class);
        registerAbility(Hermit.class);
        registerAbility(DevilBoots.class);
        registerAbility(BombArrow.class);
        registerAbility(Brewer.class);
        registerAbility(Imprison.class);
        registerAbility(SuperNova.class);
        registerAbility(Celebrity.class);
        registerAbility(ExpertOfFall.class);
        registerAbility(Curse.class);
        registerAbility(TimeRewind.class);

        // 2019 여름 업데이트
        registerAbility(Khazhad.class);
        registerAbility(Sniper.class);
        registerAbility(JellyFish.class);

        // 즐거운 여름휴가 게임모드
        registerAbility(SquirtGun.class);

        registerAbility(Lazyness.class);
    }

    /**
     * 등록된 능력들의 이름을 String ArrayList로 반환합니다. AbilityManifest가 존재하지 않는 능력은 포함되지 않습니다.
     */
    public static ArrayList<String> nameValues() {
        return new ArrayList<>(usedNames.keySet());
    }

    public static ArrayList<String> nameValues(Rank rank) {
        ArrayList<String> values = new ArrayList<>();
        for (AbilityRegistration<?> registration : registeredAbilities.values()) {
            AbilityManifest manifest = registration.getManifest();
            if (manifest.Rank().equals(rank)) {
                values.add(manifest.Name());
            }
        }
        return values;
    }

    public static ArrayList<String> nameValues(Species species) {
        ArrayList<String> values = new ArrayList<>();
        for (AbilityRegistration<?> registration : registeredAbilities.values()) {
            AbilityManifest manifest = registration.getManifest();
            if (manifest.Species().equals(species)) {
                values.add(manifest.Name());
            }
        }
        return values;
    }

    /**
     * 등록된 능력 중 해당 이름의 능력을 반환합니다. AbilityManifest가 존재하지 않는 능력이거나 존재하지 않는 능력일 경우
     * null을 반환할 수 있습니다.
     *
     * @param name 능력의 이름
     * @return 능력 Class
     */
    public static Class<? extends AbilityBase> getByName(String name) {
        return usedNames.get(name);
    }

    public static class AbilityRegistration<T extends AbilityBase> {

        private final Class<T> clazz;
        private final Constructor<T> constructor;
        private final AbilityManifest manifest;
        private final List<Field> timers;
        private final Map<Class<? extends Event>, Method> eventhandlers;

        @SuppressWarnings("unchecked")
        private AbilityRegistration(Class<T> clazz) throws NoSuchMethodException, SecurityException {
            this.clazz = clazz;

            this.constructor = clazz.getConstructor(Participant.class);

            if (!clazz.isAnnotationPresent(AbilityManifest.class))
                throw new IllegalArgumentException("AbilityManfiest가 없는 능력입니다.");
            this.manifest = clazz.getAnnotation(AbilityManifest.class);

            List<Field> timers = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> type = field.getType();
                Class<?> superClass = type.getSuperclass();
                if (type.equals(TimerBase.class) || (superClass != null && superClass.equals(TimerBase.class))) {
                    timers.add(field);
                }
            }
            this.timers = Collections.unmodifiableList(timers);

            Map<Class<? extends Event>, Method> eventhandlers = new HashMap<>();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubscribeEvent.class)) {
                    Class<?>[] parameters = method.getParameterTypes();
                    if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
                        eventhandlers.put((Class<? extends Event>) parameters[0], method);
                    }
                }
            }
            this.eventhandlers = Collections.unmodifiableMap(eventhandlers);
        }

        public Class<T> getAbilityClass() {
            return clazz;
        }

        public Constructor<T> getConstructor() {
            return constructor;
        }

        public AbilityManifest getManifest() {
            return manifest;
        }

        public List<Field> getTimers() {
            return timers;
        }

        public Map<Class<? extends Event>, Method> getEventhandlers() {
            return eventhandlers;
        }

    }

}
