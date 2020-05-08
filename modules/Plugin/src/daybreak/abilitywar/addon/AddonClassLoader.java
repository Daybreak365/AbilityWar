package daybreak.abilitywar.addon;

import com.google.common.io.ByteStreams;
import daybreak.abilitywar.addon.Addon.AddonDescription;
import daybreak.abilitywar.addon.exception.InvalidAddonException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AddonClassLoader extends URLClassLoader {

	private static final Map<String, Class<?>> cachedClasses = new HashMap<>();
	private static final Set<AddonClassLoader> loaders = new HashSet<>();
	final Map<String, Class<?>> classes = new HashMap<>();
	final JarFile jarFile;
	final Addon addon;
	private final File pluginFile;
	private final URL url;
	private final Manifest manifest;

	public AddonClassLoader(final ClassLoader parent, final AddonDescription description, final File pluginFile) throws IOException, InvalidAddonException {
		super(new URL[]{pluginFile.toURI().toURL()}, parent);
		this.pluginFile = pluginFile;
		this.url = pluginFile.toURI().toURL();
		this.jarFile = new JarFile(pluginFile);
		this.manifest = jarFile.getManifest();
		try {
			final Class<? extends Addon> addonClass;
			try {
				addonClass = Class.forName(description.getMain(), true, this).asSubclass(Addon.class);
			} catch (ClassNotFoundException ex) {
				throw new InvalidAddonException("메인 클래스 '" + description.getMain() + "'가 존재하지 않습니다.", ex);
			} catch (ClassCastException ex) {
				throw new InvalidAddonException("메인 클래스 '" + description.getMain() + "'가 Addon을 확장하지 않습니다.", ex);
			}
			this.addon = addonClass.getConstructor().newInstance();
			addon.init(this, description);
		} catch (InstantiationException ex) {
			throw new InvalidAddonException("메인 클래스의 타입이 잘못되었습니다.", ex);
		} catch (InvocationTargetException ex) {
			throw new InvalidAddonException("생성자에서 예외가 발생했습니다.", ex);
		} catch (NoSuchMethodException ex) {
			throw new InvalidAddonException("매개변수가 없는 생성자가 존재하지 않습니다.", ex);
		} catch (IllegalAccessException ex) {
			throw new InvalidAddonException("public 생성자가 존재하지 않습니다.", ex);
		}
		loaders.add(this);
	}

	public static Set<AddonClassLoader> getLoaders() {
		return loaders;
	}

	private static Class<?> getClassByName(String name) {
		Class<?> clazz = cachedClasses.get(name);
		if (clazz != null) {
			return clazz;
		} else {
			for (AddonClassLoader loader : loaders) {
				try {
					clazz = loader.findClass(name, false);
				} catch (ClassNotFoundException ignored) {
				}
				if (clazz != null) {
					return clazz;
				}
			}
		}
		return null;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, true);
	}

	private Class<?> findClass(String name, boolean global) throws ClassNotFoundException {
		Class<?> result = classes.get(name);
		if (result == null) {
			if (global) result = getClassByName(name);
			if (result == null) {
				String path = name.replace('.', '/').concat(".class");
				JarEntry entry = jarFile.getJarEntry(path);

				if (entry != null) {
					byte[] classBytes;

					try (InputStream stream = jarFile.getInputStream(entry)) {
						classBytes = ByteStreams.toByteArray(stream);
					} catch (IOException ex) {
						throw new ClassNotFoundException(name, ex);
					}

					int dot = name.lastIndexOf('.');
					if (dot != -1) {
						String packageName = name.substring(0, dot);
						if (getPackage(packageName) == null) {
							try {
								if (manifest != null) {
									definePackage(packageName, manifest, url);
								} else {
									definePackage(packageName, null, null, null, null, null, null, null);
								}
							} catch (IllegalArgumentException ex) {
								if (getPackage(packageName) == null) {
									throw new IllegalStateException("Cannot find package " + packageName);
								}
							}
						}
					}

					CodeSigner[] signers = entry.getCodeSigners();
					CodeSource source = new CodeSource(url, signers);

					result = defineClass(name, classBytes, 0, classBytes.length, source);
				}
				if (result == null) result = super.findClass(name);

				if (result != null) {
					cacheClass(name, result);
				}
			}
			classes.put(name, result);
		}
		return result;
	}

	void cacheClass(String name, Class<?> clazz) {
		cachedClasses.put(name, clazz);
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			jarFile.close();
		}
	}

	public File getPluginFile() {
		return pluginFile;
	}

}
