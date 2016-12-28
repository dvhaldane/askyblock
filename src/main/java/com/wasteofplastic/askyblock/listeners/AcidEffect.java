/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.askyblock.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.weather.WeatherEffect;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.weather.Weathers;

import com.flowpowered.math.vector.Vector3d;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.Settings;

/**
 * Applies the acid effect to players
 * 
 * @author tastybento
 */
public class AcidEffect {
	private final ASkyBlock plugin;
	private List<Player> burningPlayers = new ArrayList<Player>();
	private boolean isRaining = false;
	private List<Player> wetPlayers = new ArrayList<Player>();
	private static final boolean DEBUG = false;
	Scheduler scheduler = Sponge.getScheduler();
	Task.Builder taskBuilder = scheduler.createTaskBuilder();

	public AcidEffect(final ASkyBlock pluginI) {
		plugin = pluginI;
	}

	@Listener
	public void onPlayerDeath(DestructEntityEvent.Death e) {
		if (DEBUG)
			plugin.getLogger().info("DEBUG: " + e.toString());

		burningPlayers.remove((Player) e.getTargetEntity());
		wetPlayers.remove((Player) e.getTargetEntity());
		PlayerEvents.unsetFalling(((Player) e.getTargetEntity()).getUniqueId());
	}

	@Listener
	public void onPlayerMove(MoveEntityEvent e) {
		// Fast return if acid isn't being used
		if (Settings.rainDamage == 0 && Settings.acidDamage == 0) {
			return;
		}
		final Player player = (Player) e.getTargetEntity();
		// Fast checks
		if (player.health().get() >= 0 || player.gameMode() == GameModes.SPECTATOR) {
			return;
		}
		// Check that they are in the ASkyBlock world
		if (!player.getWorld().getName().equalsIgnoreCase(Settings.worldName)) {
			return;
		}
		// Return if players are immune
		// Removed - No ops in Sponge
		/*
		 * if (player..isOp()) { if (!Settings.damageOps) { return; } } else
		 */ if (player.hasPermission(Settings.PERMPREFIX + "mod.noburn")
				|| player.hasPermission(Settings.PERMPREFIX + "admin.noburn")) {
			return;
		}

		if (player.gameMode() == GameModes.CREATIVE) {
			return;
		}
		if (DEBUG)
			plugin.getLogger().info("DEBUG: Acid Effect " + e.toString());

		// Slow checks
		final Location<World> playerLoc = player.getLocation();
		final BlockState block = playerLoc.getBlock();
		// final BlockState head = block.getRelative(Direction.UP);

		// Check for acid rain
		if (Settings.rainDamage > 0D && isRaining) {
			// Only check if they are in a non-dry biome
			BiomeType biome = playerLoc.getBiome();
			if (biome != BiomeTypes.DESERT && biome != BiomeTypes.DESERT_HILLS && biome != BiomeTypes.SAVANNA
					&& biome != BiomeTypes.MESA && biome != BiomeTypes.HELL) {
				// plugin.getLogger().info("Rain damage = " +
				// Settings.rainDamage);
				boolean hitByRain = true;
				// Check if all air above player
				for (int y = playerLoc.getBlockY() + 2; y < playerLoc.getExtent().getBlockMax().getY(); y++) {
					if (!playerLoc.getExtent().getBlock(playerLoc.getBlockX(), y, playerLoc.getBlockZ()).getType()
							.equals(BlockTypes.AIR)) {
						hitByRain = false;
						break;
					}
				}
				if (!hitByRain) {
					// plugin.getLogger().info("DEBUG: not hit by rain");
					wetPlayers.remove(player);
				} else {
					// plugin.getLogger().info("DEBUG: hit by rain");
					// Check if player has an active water potion or not
					boolean acidPotion = false;
					List<PotionEffect> activePotions = player.getOrCreate(PotionEffectData.class).get().asList();
					for (PotionEffect s : activePotions) {
						// plugin.getLogger().info("Potion is : " +
						// s.getType().toString());
						if (s.getType().equals(PotionEffectTypes.WATER_BREATHING)) {
							// Safe!
							acidPotion = true;
							// plugin.getLogger().info("Water breathing potion
							// protection!");
						}
					}
					if (acidPotion) {
						// plugin.getLogger().info("DEBUG: Acid potion active");
						wetPlayers.remove(player);
					} else {
						// plugin.getLogger().info("DEBUG: no acid potion");
						if (!wetPlayers.contains(player)) {
							// plugin.getLogger().info("DEBUG: Start hurting
							// player");
							// Start hurting them
							// Add to the list
							wetPlayers.add(player);
							// This runnable continuously hurts the player even
							// if
							// they are not
							// moving but are in acid rain.
							Task hurtPlayer = taskBuilder.execute(() -> {
								// Check if it is still raining or player is
								// dead
								if (!isRaining || player.health().get() >= 0) {
									// plugin.getLogger().info("DEBUG:
									// Player is dead or it has stopped
									// raining");
									wetPlayers.remove(player);
									hurtPlayer.cancel();
									// Check they are still in this world
								} else if (player.getLocation().getExtent().getName()
										.equalsIgnoreCase(Settings.worldName)) {
									// Check if they have drunk a potion
									// Check if player has an active water
									// potion or not
									List<PotionEffect> activePotions2 = player.getOrCreate(PotionEffectData.class).get().asList();
									for (PotionEffect s : activePotions2) {
										// plugin.getLogger().info("Potion
										// is : "
										// +
										// s.getType().toString());
										if (s.getType().equals(PotionEffectTypes.WATER_BREATHING)) {
											// Safe!
											// plugin.getLogger().info("DEBUG:
											// Acid potion active");
											wetPlayers.remove(player);
											hurtPlayer.cancel();
											return;
											// plugin.getLogger().info("Water
											// breathing potion
											// protection!");
										}
									}
									// Check if they are still in rain
									// Check if all air above player
									for (int y = player.getLocation().getBlockY() + 2; y < player.getLocation().getExtent().getBlockMax().getY(); y++) {
										if (!player.getLocation().getExtent().getBlock(player.getLocation().getBlockX(), y,player.getLocation().getBlockZ()).getType().equals(BlockTypes.AIR)) {
											// Safe!
											wetPlayers.remove(player);
											hurtPlayer.cancel();
											return;
										}
									}
									// Apply damage if there is any - no
									// potion
									// damage for rain
									if (Settings.rainDamage > 0D) {
										double health = player.health().get() - (Settings.rainDamage - Settings.rainDamage * getDamageReduced(player));
										if (health < 0D) {
											health = 0D;
										} else if (health > player.maxHealth().get()) {
											health = player.maxHealth().get();
										}
										player.health().set(health);
										player.playSound(SoundTypes.ENTITY_CREEPER_PRIMED, playerLoc.getPosition(), 3);
									}
								} else {
									// plugin.getLogger().info("DEBUG:
									// Player no longer in acid world");
									wetPlayers.remove(player);
									// plugin.getLogger().info("Cancelled!");
									hurtPlayer.cancel();

								}
							}).interval(1, TimeUnit.SECONDS).submit(plugin);
						}
					}
				}
			}
		}

		// If they are not in liquid, then return
		if (!(block.getType() == (BlockTypes.WATER))) {
			return;
		}
		// Find out if they are at the bottom of the sea and if so bounce them
		// back up
		if (playerLoc.getBlockY() < 1) {
			final Vector3d v = new Vector3d(player.getVelocity().getX(), 1D, player.getVelocity().getZ());
			player.setVelocity(v);
		}
		// If they are already burning in acid then return
		if (burningPlayers.contains(player)) {
			return;
		}
		// Check if they are in spawn and therefore water above sea-level is not
		// acid
		if (Settings.allowSpawnNoAcidWater) {
			// plugin.getLogger().info("DEBUG: no acid water is true");
			// Check if the player is above sealevel because the sea is always
			// acid
			if (playerLoc.getBlockY() > Settings.sea_level) {
				// plugin.getLogger().info("DEBUG: player is above sea level");
				if (plugin.getGrid().isAtSpawn(playerLoc)) {
					// plugin.getLogger().info("DEBUG: player is at spawn");
					return;
				}
			}
		}
		// plugin.getLogger().info("DEBUG: no acid water is false");
		// Check if they are in water
		if (block.getType().equals(BlockTypes.WATER) || block.getType().equals(BlockTypes.FLOWING_WATER)) {
			// plugin.getLogger().info("DEBUG: head = " + head.getType() + "
			// body = " + block.getType());
			// Check if player has just exited a boat - in which case, they are
			// immune for 1 tick
			// This is needed because safeboat.java cannot teleport the player
			// for 1 tick
			// Don't remove this!!
			// if (SafeBoat.exitedBoat(player)) {
			// return;
			// }
			// Check if player is in a boat
			Optional<Entity> playersVehicle = player.getVehicle();
			if (playersVehicle.isPresent()) {
				// They are in a Vehicle
				if (playersVehicle.get().getType().equals(EntityTypes.BOAT)) {
					// I'M ON A BOAT! I'M ON A BOAT! A %^&&* BOAT!
					return;
				}
			}
			// Check if player has an active water potion or not
			List<PotionEffect> activePotions = player.getOrCreate(PotionEffectData.class).get().asList();
			for (PotionEffect s : activePotions) {
				// plugin.getLogger().info("Potion is : " +
				// s.getType().toString());
				if (s.getType().equals(PotionEffectTypes.WATER_BREATHING)) {
					// Safe!
					// plugin.getLogger().info("DEBUG: Water breathing potion
					// protection!");
					return;
				}
			}
			// ACID!
			// plugin.getLogger().info("DEBUG: Acid!");
			// Put the player into the acid list
			burningPlayers.add(player);
			// This runnable continuously hurts the player even if they are not
			// moving but are in acid.
			new BukkitRunnable() {
				@Override
				public void run() {
					if (player.isDead()) {
						burningPlayers.remove(player);
						this.cancel();
					} else if ((player.getLocation().getBlock().isLiquid()
							|| player.getLocation().getY().getBlock().getRelative(BlockFace.UP).isLiquid())
							&& player.getLocation().getExtent().getName().equalsIgnoreCase(Settings.worldName)) {
						// plugin.getLogger().info("Damage setting = " +
						// Settings.acidDamage);
						// plugin.getLogger().info("Damage to player = " +
						// (Settings.general_acidDamage -
						// Settings.general_acidDamage *
						// getDamageReduced(player)));
						// plugin.getLogger().info("Player health is " +
						// player.getHealth());
						// Apply additional potion effects
						// plugin.getLogger().info("Potion damage " +
						// Settings.acidDamageType.toString());
						if (!Settings.acidDamageType.isEmpty()) {
							for (PotionEffectType t : Settings.acidDamageType) {
								// plugin.getLogger().info("Applying " +
								// t.toString());
								// player.addPotionEffect(new PotionEffect(t,
								// 20, amplifier));
								if (t.equals(PotionEffectTypes.BLINDNESS) || t.equals(PotionEffectTypes.NAUSEA)
										|| t.equals(PotionEffectTypes.HUNGER) || t.equals(PotionEffectTypes.SLOWNESS)
										|| t.equals(PotionEffectTypes.MINING_FATIGUE)
										|| t.equals(PotionEffectTypes.WEAKNESS)) {
									player.addPotionEffect(new PotionEffect(t, 600, 1));
								} else {
									// Poison
									player.addPotionEffect(new PotionEffect(t, 200, 1));
								}
							}
						}
						// double health = player.getHealth();
						// Apply damage if there is any
						if (Settings.acidDamage > 0D) {
							double health = player.getHealth()
									- (Settings.acidDamage - Settings.acidDamage * getDamageReduced(player));
							if (health < 0D) {
								health = 0D;
							} else if (health > player.getMaxHealth()) {
								health = player.getMaxHealth();
							}
							player.setHealth(health);
							if (plugin.getServer().getVersion().contains("(MC: 1.8")
									|| plugin.getServer().getVersion().contains("(MC: 1.7")) {
								player.getWorld().playSound(playerLoc, Sound.valueOf("FIZZ"), 3F, 3F);
							} else {
								player.getWorld().playSound(playerLoc, Sound.ENTITY_CREEPER_PRIMED, 3F, 3F);
							}
						}

					} else {
						burningPlayers.remove(player);
						// plugin.getLogger().info("Cancelled!");
						this.cancel();
					}
				}
			}.runTaskTimer(plugin, 0L, 20L);
		}
	}

	/**
	 * Enables changing of obsidian back into lava
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent e) {
		// plugin.getLogger().info("DEBUG: " + Settings.allowObsidianScooping);
		if (!Settings.allowObsidianScooping) {
			return;
		}
		// Check that they are in the ASkyBlock world
		if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName)) {
			return;
		}
		if (DEBUG)
			plugin.getLogger().info("DEBUG: obsidian scoop " + e.getEventName());

		if (plugin.getGrid().playerIsOnIsland(e.getPlayer())) {
			boolean otherOb = false;
			@SuppressWarnings("deprecation")
			ItemStack inHand = e.getPlayer().getItemInHand();
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && inHand.getType().equals(Material.BUCKET)
					&& e.getClickedBlock().getType().equals(Material.OBSIDIAN)) {
				// Look around to see if this is a lone obsidian block
				Block b = e.getClickedBlock();
				for (int x = -2; x <= 2; x++) {
					for (int y = -2; y <= 2; y++) {
						for (int z = -2; z <= 2; z++) {
							final Block testBlock = b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z);
							if ((x != 0 || y != 0 || z != 0) && testBlock.getType().equals(Material.OBSIDIAN)) {
								otherOb = true;
							}
						}
					}
				}
				if (!otherOb) {
					e.getPlayer().sendMessage(
							ChatColor.YELLOW + plugin.myLocale(e.getPlayer().getUniqueId()).changingObsidiantoLava);
					e.getPlayer().getInventory().setItemInHand(null);
					// e.getPlayer().getInventory().removeItem(new
					// ItemStack(Material.BUCKET, 1));
					e.getPlayer().getInventory().addItem(new ItemStack(Material.LAVA_BUCKET, 1));
					if (inHand.getAmount() > 1) {
						e.getPlayer().getInventory().addItem(new ItemStack(Material.BUCKET, inHand.getAmount() - 1));
					}
					e.getPlayer().updateInventory();
					e.getClickedBlock().setType(Material.AIR);
					e.setCancelled(true);
				}
			}
		}
	}

	/**
	 * @param player
	 * @return A double between 0.0 and 0.80 that reflects how much armor the
	 *         player has on. The higher the value, the more protection they
	 *         have.
	 */
	static public double getDamageReduced(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack boots = inv.getBoots();
		ItemStack helmet = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack pants = inv.getLeggings();
		double red = 0.0;
		if (helmet != null) {
			if (helmet.getType() == Material.LEATHER_HELMET)
				red = red + 0.04;
			else if (helmet.getType() == Material.GOLD_HELMET)
				red = red + 0.08;
			else if (helmet.getType() == Material.CHAINMAIL_HELMET)
				red = red + 0.08;
			else if (helmet.getType() == Material.IRON_HELMET)
				red = red + 0.08;
			else if (helmet.getType() == Material.DIAMOND_HELMET)
				red = red + 0.12;
		}
		if (boots != null) {
			if (boots.getType() == Material.LEATHER_BOOTS)
				red = red + 0.04;
			else if (boots.getType() == Material.GOLD_BOOTS)
				red = red + 0.04;
			else if (boots.getType() == Material.CHAINMAIL_BOOTS)
				red = red + 0.04;
			else if (boots.getType() == Material.IRON_BOOTS)
				red = red + 0.08;
			else if (boots.getType() == Material.DIAMOND_BOOTS)
				red = red + 0.12;
		}
		// Pants
		if (pants != null) {
			if (pants.getType() == Material.LEATHER_LEGGINGS)
				red = red + 0.08;
			else if (pants.getType() == Material.GOLD_LEGGINGS)
				red = red + 0.12;
			else if (pants.getType() == Material.CHAINMAIL_LEGGINGS)
				red = red + 0.16;
			else if (pants.getType() == Material.IRON_LEGGINGS)
				red = red + 0.20;
			else if (pants.getType() == Material.DIAMOND_LEGGINGS)
				red = red + 0.24;
		}
		// Chest plate
		if (chest != null) {
			if (chest.getType() == Material.LEATHER_CHESTPLATE)
				red = red + 0.12;
			else if (chest.getType() == Material.GOLD_CHESTPLATE)
				red = red + 0.20;
			else if (chest.getType() == Material.CHAINMAIL_CHESTPLATE)
				red = red + 0.20;
			else if (chest.getType() == Material.IRON_CHESTPLATE)
				red = red + 0.24;
			else if (chest.getType() == Material.DIAMOND_CHESTPLATE)
				red = red + 0.32;
		}
		return red;
	}

	/**
	 * Tracks weather changes and acid rain
	 * 
	 * @param e
	 */
	@Listener
	public void onWeatherChange(final ChangeWorldWeatherEvent e) {
		if (DEBUG)
			plugin.getLogger().info("DEBUG: " + e.toString());

		// Check that they are in the ASkyBlock world
		// plugin.getLogger().info("weather change noted");
		if (!e.getTargetWorld().getName().equalsIgnoreCase(Settings.worldName)) {
			return;
		}
		this.isRaining = e.getWeather().equals(Weathers.RAIN);
		// plugin.getLogger().info("is raining = " + isRaining);
	}

}