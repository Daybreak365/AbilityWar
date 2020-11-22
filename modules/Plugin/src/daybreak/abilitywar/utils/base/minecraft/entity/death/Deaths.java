package daybreak.abilitywar.utils.base.minecraft.entity.death;

import com.google.common.collect.ImmutableMap;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.library.Entities;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Deaths {

	private Deaths() {}

	private static final Random random = new Random();
	private static final ImmutableMap<DamageCause, Messages> handlers;

	static {
		final ImmutableMap.Builder<DamageCause, Messages> builder = ImmutableMap.builder();
		builder.put(DamageCause.ENTITY_ATTACK, new Messages() {
			private final String[] random = {
					"컷!", "싹둑!"
			};
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				final Entity damager = getDamager(damageEvent, true);
				if (killer != null) {
					if (killer.equals(damager)) {
						return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(killer) + "§f에게 살해당했습니다. " + pick(random);
					} else if (damager != null) {
						return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인해 §7" + formatName(damager) + "§f에게 살해당했습니다. " + pick(random);
					}
				} else if (damager != null) {
					return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " §7" + formatName(damager) + "§f에게 살해당했습니다. " + pick(random);
				}
				return null;
			}
		});
		builder.put(DamageCause.PROJECTILE, new Messages() {
			private final String[] random = {
					"피슈우웅", "철컥, 탕!", "빵야!"
			};
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (damageEvent instanceof EntityDamageByEntityEvent) {
					final EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
					if (damageByEntityEvent.getDamager() instanceof Projectile) {
						final Projectile projectile = (Projectile) damageByEntityEvent.getDamager();
						Entity shooter = projectile.getShooter() instanceof Entity ? (Entity) projectile.getShooter() : null;
						if (shooter != null) {
							if (killer != null) {
								if (!killer.equals(shooter)) {
									if (projectile instanceof Arrow) {
										return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인해 " + formatName(shooter) + "§f에게 저격당했습니다. " + pick(random);
									} else {
										return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인해 " + formatName(shooter) + "§f에게 구타당했습니다";
									}
								}
								if (projectile instanceof Arrow) {
									return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(shooter) + "§f에게 저격당했습니다. " + pick(random);
								} else {
									return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(shooter) + "§f에게 구타당했습니다";
								}
							} else {
								if (projectile instanceof Arrow) {
									return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(shooter) + "§f에게 저격당했습니다. " + pick(random);
								} else {
									return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " " + formatName(shooter) + "§f에게 구타당했습니다";
								}
							}
						} else if (projectile instanceof Arrow) {
							return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " 저격당했습니다. " + pick(random);
						}
					}
				}
				return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " 구타당했습니다.";
			}
		});
		builder.put(DamageCause.LAVA, new Messages() {
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인하여 용암에 빠졌습니다.";
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 용암에 빠졌습니다.";
			}
		});
		{
			final Messages messages = new Messages() {
				@Override
				public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
					final Player killer = dead.getKiller();
					if (killer != null) {
						return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인하여 노릇노릇하게 구워졌습니다.";
					}
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 노릇노릇하게 구워졌습니다.";
				}
			};
			builder.put(DamageCause.FIRE, messages);
			builder.put(DamageCause.FIRE_TICK, messages);
		}
		{
			final Messages messages = new Messages() {
				private final String[] random = {
						"펑!", "퍼벙!"
				};
				@Override
				public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
					final Player killer = dead.getKiller();
					final Entity damager = getDamager(damageEvent, false);
					if (damager != null) {
						if (damager.equals(killer)) {
							return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인하여 " + formatName(damager) + "§f에게 폭파당했습니다. " + pick(random);
						} else {
							return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(damager) + "§f에게 폭파당했습니다. " + pick(random);
						}
					} else if (killer != null) {
						return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인하여 폭파당했습니다. " + pick(random);
					}
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 폭파당했습니다. " + pick(random);
				}
			};
			builder.put(DamageCause.ENTITY_EXPLOSION, messages);
			builder.put(DamageCause.BLOCK_EXPLOSION, messages);
		}
		builder.put(DamageCause.FALL, new Messages() {
			private final String[] random = {
					"뽀각!", "쿵!"
			};
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + " 인해 높은 곳에서 떨어졌습니다. " + pick(random);
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 높은 곳에서 떨어졌습니다. " + pick(random);
			}
		});
		builder.put(DamageCause.DROWNING, new Messages() {
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + "부터 도망치려다 익사했습니다. §7뽀글뽀글..";
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 익사했습니다. §7뽀글뽀글..";
			}
		});
		builder.put(DamageCause.SUFFOCATION, new Messages() {
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + "부터 도망치려다 끼어 죽었습니다.";
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 벽 속에서 질식했습니다.";
			}
		});
		builder.put(DamageCause.STARVATION, new Messages() {
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f" + getJosa(killer, Josa.으로로) + "부터 도망치려다 굶어 죽었습니다. §7꼬르륵..";
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 굶어 죽었습니다. §7꼬르륵..";
			}
		});
		builder.put(DamageCause.MAGIC, new Messages() {
			@Override
			public String getMessage(@NotNull Player dead, @NotNull EntityDamageEvent damageEvent) {
				final Player killer = dead.getKiller();
				if (killer != null) {
					return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " " + formatName(killer) + "§f에게 마법으로 살해당했습니다.";
				}
				return "§c" + dead.getName() + "§f" + getJosa(dead, Josa.이가) + " 마법으로 살해당했습니다.";
			}
		});
		handlers = builder.build();
	}

	private static String formatName(final Entity entity) {
		return (entity instanceof Player ? ChatColor.GREEN + entity.getName() : (ChatColor.GRAY + Entities.of(entity.getType()).getName()));
	}

	private static String pick(final String[] strings) {
		return "§7" + random.pick(strings);
	}

	private static String getJosa(final Entity entity, final Josa josa) {
		return KoreanUtil.getJosa(entity.getName().replace("_", ""), josa);
	}

	@Nullable
	private static Entity getDamager(@NotNull final EntityDamageEvent damageEvent, final boolean shooter) {
		if (damageEvent instanceof EntityDamageByEntityEvent) {
			final EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
			if (shooter && damageByEntityEvent.getDamager() instanceof Projectile) {
				final Projectile projectile = (Projectile) damageByEntityEvent.getDamager();
				if (projectile.getShooter() instanceof Entity) return (Entity) projectile.getShooter();
			}
			return damageByEntityEvent.getDamager();
		}
		return null;
	}

	public static String getMessage(@NotNull final Player dead) {
		final EntityDamageEvent damageEvent = dead.getLastDamageCause();
		if (damageEvent != null && handlers.containsKey(damageEvent.getCause())) {
			final String message = handlers.get(damageEvent.getCause()).getMessage(dead, damageEvent);
			if (message != null) return message;
		}
		return "§c" + dead.getName() + "§f" + KoreanUtil.getJosa(dead.getName().replace("_", ""), Josa.이가) + " 죽었습니다.";
	}

	private interface Messages {
		String getMessage(@NotNull final Player dead, @NotNull final EntityDamageEvent damageEvent);
	}

}
