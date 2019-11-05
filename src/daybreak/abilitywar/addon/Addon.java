package daybreak.abilitywar.addon;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.addon.exception.InvalidAddonException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.plugin.Plugin;

/**
 * 애드온
 * @author DayBreak 새벽
 */
public abstract class Addon {

    private ClassLoader classLoader;
    private DescriptionFile description;

    protected void onEnable() {}
    protected void onDisable() {}

    /**
     * AbilityWar 플러그인을 받아옵니다.
     */
    protected Plugin getPlugin() {
        return AbilityWar.getPlugin();
    }

    /**
     * addon.yml에 작성한 애드온의 설명을 받아옵니다.
     */
    public DescriptionFile getDescription() {
        return description;
    }

    /**
     * 이 애드온을 불러올 때 사용된 ClassLoader를 받아옵니다.
     */
    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    static class Builder {

        private final JarFile jarFile;
        private final URLClassLoader classLoader;
        private final DescriptionFile descriptionFile;

        Builder(File file) throws IOException {
            this.jarFile = new JarFile(file);
            this.classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, Addon.class.getClassLoader());
            this.descriptionFile = new DescriptionFile(jarFile);
        }

        public Addon build() {
            try {
                Class<?> main = classLoader.loadClass(descriptionFile.getMain());
                if (Addon.class.isAssignableFrom(main)) {
                    @SuppressWarnings("unchecked") Constructor<? extends Addon> constructor = (Constructor<? extends Addon>) main.getDeclaredConstructor();
                    Addon instance = constructor.newInstance();

                    instance.classLoader = classLoader;
                    instance.description = descriptionFile;

                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            classLoader.loadClass(entry.getName().replaceAll("/", ".").replace(".class", ""));
                        }
                    }
                    classLoader.close();
                    return instance;
                } else {
                    throw new InvalidAddonException(descriptionFile.name + ": 메인 클래스가 Addon을 상속받지 않습니다.");
                }
            } catch (ClassNotFoundException e) {
                throw new InvalidAddonException(descriptionFile.name + ": 메인 클래스 '" + descriptionFile.main + "'가 존재하지 않습니다.");
            } catch (NoSuchMethodException e) {
                throw new InvalidAddonException(descriptionFile.name + ": 올바른 Constructor가 존재하지 않습니다.");
            } catch (IllegalAccessException e) {
                throw new InvalidAddonException(descriptionFile.name + ": 접근할 수 있는 Constructor가 존재하지 않습니다.");
            } catch (InvocationTargetException e) {
                throw new InvalidAddonException(descriptionFile.name + ": 메인 인스턴스를 생성하는 도중 오류가 발생하였습니다, " + e.getCause().toString());
            } catch (InstantiationException | IOException e) {
                throw new InvalidAddonException(descriptionFile.name + ": 애드온을 불러오는 도중 오류가 발생하였습니다.");
            }
        }

    }

    public static class DescriptionFile {

        private final String name;
        private final String main;
        private final String version;

        private DescriptionFile(JarFile jarFile) throws InvalidAddonException, IOException {
            ZipEntry entry = jarFile.getEntry("addon.yml");
            if (entry != null) {
                Properties description = new Properties();
                description.load(new InputStreamReader(jarFile.getInputStream(entry)));

                this.name = description.getProperty("name", "");
                this.main = description.getProperty("main", "");
                this.version = description.getProperty("version", "");
                if (name.isEmpty() || main.isEmpty() || version.isEmpty()) {
                    throw new InvalidAddonException(jarFile.getName() + ": 올바르지 않은 addon.yml입니다.");
                }
            } else {
                throw new InvalidAddonException(jarFile.getName() + ": addon.yml이 존재하지 않습니다.");
            }
        }

        public String getName() {
            return name;
        }

        public String getMain() {
            return main;
        }

        public String getVersion() {
            return version;
        }

    }

}
