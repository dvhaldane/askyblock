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
package com.wasteofplastic.askyblock;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.events.WarpCreateEvent;
import com.wasteofplastic.askyblock.events.WarpListEvent;
import com.wasteofplastic.askyblock.events.WarpRemoveEvent;
import com.wasteofplastic.askyblock.util.Util;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Handles warping in ASkyBlock Players can add one sign
 * 
 * @author tastybento
 * 
 */
public class WarpSigns {
	private final ASkyBlock plugin;
	private final static boolean DEBUG = false;
	// Map of all warps stored as player, warp sign Location
	private HashMap<UUID, Location<World>> warpList = new HashMap<UUID, Location<World>>();
	// Where warps are stored
	private CommentedConfigurationNode welcomeWarps;

	/**
	 * @param plugin
	 */
	public WarpSigns(ASkyBlock plugin) {
		this.plugin = plugin;
		this.warpList = new HashMap<UUID, Location<World>>();
	}

	/**
	 * Checks to see if a sign has been broken
	 * 
	 * @param e
	 */
	@Listener
	public void onSignBreak(ChangeBlockEvent.Break e) {
		Location<World> location = e.getTransactions().get(0).getOriginal().getLocation().get();
		Cause cause = e.getCause();
		World world = e.getTargetWorld();
		BlockState b = e.getTransactions().get(0).getOriginal().getExtendedState();
		Optional<ImmutableSignData> s = e.getTransactions().get(0).getOriginal().get(ImmutableSignData.class);
		Optional<Player> player = cause.first(Player.class);
		if (world.equals(ASkyBlock.getIslandWorld()) || world.equals(ASkyBlock.getNetherWorld())) {
			if (b.getType().equals(BlockTypes.STANDING_SIGN) || b.getType().equals(BlockTypes.WALL_SIGN)) {
				if (s.isPresent() && player.isPresent()) {
					// plugin.getLogger().info("DEBUG: sign found at location "
					// + s.toString());
					Player p = player.get();
					if (s.get().get(0).get().equals(TextColors.GREEN + plugin.myLocale().warpswelcomeLine)) {
						// Do a quick check to see if this sign location is in
						// plugin.getLogger().info("DEBUG: welcome sign");
						// the list of warp signs
						if (warpList.containsValue(location)) {
							// plugin.getLogger().info("DEBUG: warp sign is in
							// list");
							// Welcome sign detected - check to see if it is
							// this player's sign
							if ((warpList.containsKey(p.getUniqueId()) && warpList.get(p.getUniqueId()).equals(location))) {
								// Player removed sign
								removeWarp(location);
								
								Sponge.getEventManager().post(new WarpRemoveEvent(plugin, location, p.getUniqueId()));
								
							} else if (p.hasPermission(Settings.PERMPREFIX + "mod.removesign")) {
								// mod removed sign
								p.sendMessage(Text.of(Text.of(TextColors.GREEN + plugin.myLocale(p.getUniqueId()).warpsremoved)));
								removeWarp(location);
								Sponge.getEventManager().post(new WarpRemoveEvent(plugin, location, p.getUniqueId()));
							} else {
								// Someone else's sign - not allowed
								p.sendMessage(Text.of(Text.of(TextColors.RED + plugin.myLocale(p.getUniqueId()).warpserrorNoRemove)));
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Event handler for Sign Changes
	 * 
	 * @param e
	 */
	@Listener
	public void onSignWarpCreate(ChangeSignEvent e) {
		// plugin.getLogger().info("DEBUG: SignChangeEvent called");
		Text title = e.getText().get(0).get();
		Player player = e.getCause().first(Player.class).get();
		if (player.getWorld().equals(ASkyBlock.getIslandWorld())
				|| player.getWorld().equals(ASkyBlock.getNetherWorld())) {
			// plugin.getLogger().info("DEBUG: Correct world");

				// plugin.getLogger().info("DEBUG: The first line of the sign
				// says " + title);
				// Check if someone is changing their own sign
				// This should never happen !!
				if (title.equals(Text.of(plugin.myLocale().warpswelcomeLine))) {
					// plugin.getLogger().info("DEBUG: Welcome sign detected");
					// Welcome sign detected - check permissions
					if (!player.hasPermission(Settings.PERMPREFIX + "island.addwarp")) {
						player.sendMessage(Text.of(TextColors.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoPerm));
						return;
					}
					if (!(ASkyBlockAPI.getInstance()
							.getIslandLevel(player.getUniqueId()) > Settings.warpLevelsRestriction)) {
						player.sendMessage(Text.of(Text.of(TextColors.RED + plugin.myLocale(player.getUniqueId()).warpserrorNotEnoughLevel)));
						return;
					}
					// Check that the player is on their island
					if (!(plugin.getGrid().playerIsOnIsland(player))) {
						player.sendMessage(Text.of(TextColors.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoPlace));
						e.getText().setElement(0, Text.of(TextColors.RED + plugin.myLocale().warpswelcomeLine));
						return;
					}
					// Check if the player already has a sign
					final Location<World> oldSignLoc = getWarp(player.getUniqueId());
					if (oldSignLoc == null) {
						// plugin.getLogger().info("DEBUG: Player does not have
						// a sign already");
						// First time the sign has been placed or this is a new
						// sign
						if (addWarp(player.getUniqueId(), e.getBlock().getLocation())) {
							player.sendMessage(Text.of(TextColors.GREEN + plugin.myLocale(player.getUniqueId()).warpssuccess));
							e.getText().setElement(0, Text.of(TextColors.GREEN + plugin.myLocale().warpswelcomeLine));
							for (int i = 1; i < 4; i++) {
								e.setLine(i, TextColors.translateAlternateColorCodes('&', e.getLine(i)));
							}
						} else {
							player.sendMessage(Text.of(
									TextColors.RED + plugin.myLocale(player.getUniqueId()).warpserrorDuplicate);
							e.setLine(0, TextColors.RED + plugin.myLocale().warpswelcomeLine);
							for (int i = 1; i < 4; i++) {
								e.setLine(i, TextColors.translateAlternateColorCodes('&', e.getLine(i)));
							}
						}
					} else {
						// plugin.getLogger().info("DEBUG: Player already has a
						// Sign");
						// A sign already exists. Check if it still there and if
						// so,
						// deactivate it
						BlockState oldSignBlock = oldSignLoc.getBlock();
						if (oldSignBlock.getType().equals(Material.SIGN_POST)
								|| oldSignBlock.getType().equals(Material.WALL_SIGN)) {
							// The block is still a sign
							// plugin.getLogger().info("DEBUG: The block is
							// still a sign");
							Sign oldSign = (Sign) oldSignBlock.getState();
							if (oldSign != null) {
								// plugin.getLogger().info("DEBUG: Sign block is
								// a sign");
								if (oldSign.getLine(0)
										.equalsIgnoreCase(TextColors.GREEN + plugin.myLocale().warpswelcomeLine)) {
									// plugin.getLogger().info("DEBUG: Old sign
									// had a green welcome");
									oldSign.setLine(0, TextColors.RED + plugin.myLocale().warpswelcomeLine);
									oldSign.update();
									player.sendMessage(Text.of(
											TextColors.RED + plugin.myLocale(player.getUniqueId()).warpsdeactivate);
									removeWarp(player.getUniqueId());
									Bukkit.getPluginManager().callEvent(
											new WarpRemoveEvent(plugin, oldSign.getLocation(), player.getUniqueId()));
								}
							}
						}
						// Set up the warp
						if (addWarp(player.getUniqueId(), e.getBlock().getLocation())) {
							player.sendMessage(Text.of(TextColors.GREEN + plugin.myLocale(player.getUniqueId()).warpssuccess);
							e.setLine(0, TextColors.GREEN + plugin.myLocale().warpswelcomeLine);
						} else {
							player.sendMessage(Text.of(
									TextColors.RED + plugin.myLocale(player.getUniqueId()).warpserrorDuplicate);
							e.setLine(0, TextColors.RED + plugin.myLocale().warpswelcomeLine);
						}
					}
				}
			
		}
	}

	/**
	 * Saves the warp lists to file
	 */
	public void saveWarpList() {
		if (warpList == null || welcomeWarps == null) {
			return;
		}
		// plugin.getLogger().info("Saving warps...");
		final HashMap<String, Object> warps = new HashMap<String, Object>();
		for (UUID p : warpList.keySet()) {
			warps.put(p.toString(), Util.getStringLocation(warpList.get(p)));
		}
		welcomeWarps.set("warps", warps);
		Util.saveYamlFile(welcomeWarps, "warps.yml");
		// Update the warp panel - needs to be done 1 tick later so that the
		// sign
		// text will be updated.
		/*
		 * if (reloadPanel) { // This is not done on shutdown if
		 * (Settings.useWarpPanel && plugin.getWarpPanel() != null) {
		 * plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
		 * 
		 * @Override public void run() { plugin.getWarpPanel().; }}); } }
		 */
		// plugin.getLogger().info("End of saving warps");
	}

	/**
	 * Creates the warp list if it does not exist
	 */
	public void loadWarpList() {
		plugin.getLogger().info("Loading warps...");
		// warpList.clear();
		welcomeWarps = Util.loadFile("warps.json");
		HashMap<String, Object> temp = (HashMap<String, Object>) welcomeWarps.getConfigurationSection("warps")
				.getValues(true);
		for (String s : temp.keySet()) {
			try {
				UUID playerUUID = UUID.fromString(s);
				Location<World> l = Util.getLocationString((String) temp.get(s));
				// plugin.getLogger().info("DEBUG: Loading warp at " + l);
				BlockState b = l.getBlock();
				// Check that a warp sign is still there
				if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
					warpList.put(playerUUID, l);
				} else {
					plugin.getLogger().warning("Warp at location " + temp.get(s) + " has no sign - removing.");
					// Test code
					if (DEBUG) {
						String name = plugin.getTinyDB().getPlayerName(playerUUID);
						warpList.put(playerUUID, l);
						b.getRelative(BlockFace.DOWN).setType(Material.DIRT);
						b.setType(Material.SIGN_POST);
						Sign sign = (Sign) b.getState();
						sign.setLine(0, TextColors.GREEN + plugin.myLocale().warpswelcomeLine);
						sign.setLine(1, name);
						sign.setLine(2, "Test 2");
						sign.update();
					}
					// End test code
				}
			} catch (Exception e) {
				plugin.getLogger().error("Problem loading warp at location " + temp.get(s) + " - removing.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stores warps in the warp array
	 * 
	 * @param player
	 * @param loc
	 */
	public boolean addWarp(final UUID player, final Location<World> loc) {
		// Do not allow warps to be in the same location
		if (warpList.containsValue(loc)) {
			return false;
		}
		// Remove the old warp if it existed
		if (warpList.containsKey(player)) {
			warpList.remove(player);
		}
		warpList.put(player, loc);
		saveWarpList();
		// Update warp signs
		// Run one tick later because text gets updated at the end of tick
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.getWarpPanel().addWarp(player);
				plugin.getWarpPanel().updatePanel();
				Sponge.getEventManager().post(new WarpCreateEvent(plugin, loc, player));
			}
		});
		return true;
	}

	/**
	 * Removes a warp when the welcome sign is destroyed. Called by
	 * WarpSigns.java.
	 * 
	 * @param uuid
	 */
	public void removeWarp(UUID uuid) {
		if (warpList.containsKey(uuid)) {
			popSign(warpList.get(uuid));
			warpList.remove(uuid);
		}
		saveWarpList();
		// Update warp signs
		// Run one tick later because text gets updated at the end of tick
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.getWarpPanel().updatePanel();

			}
		});
	}

	/**
	 * Changes the sign to red if it exists
	 * 
	 * @param loc
	 */
	private void popSign(Location<World> loc) {
		BlockState b = loc.getBlock();
		if (b.getType().equals(BlockTypes.STANDING_SIGN) || b.getType().equals(BlockTypes.WALL_SIGN)) {
			TileEntity signEntity = loc.getTileEntity().get();
			Optional<SignData> s = signEntity.get(SignData.class);
			if (s.isPresent()) {
				if (s.get().get(0).equals(Text.of(TextColors.GREEN + plugin.myLocale().warpswelcomeLine))) {
					s.get().setElement(0, Text.of(TextColors.RED + plugin.myLocale().warpswelcomeLine));
					signEntity.offer(s.get());
				}
			}
		}
	}

	/**
	 * Removes a warp at a location. Called by WarpSigns.java.
	 * 
	 * @param loc
	 */
	public void removeWarp(Location<World> loc) {
		// plugin.getLogger().info("Asked to remove warp at " + loc);
		popSign(loc);
		Iterator<Entry<UUID, Location<World>>> it = warpList.entrySet().iterator();
		while (it.hasNext()) {
			Entry<UUID, Location<World>> en = it.next();
			if (en.getValue().equals(loc)) {
				// Inform player
				Player p = plugin.getServer().getPlayer(en.getKey()).get();
				if (p != null) {
					// Inform the player
					p.sendMessage(Text.of(TextColors.RED + plugin.myLocale(p.getUniqueId()).warpssignRemoved));
				} else {
					plugin.getMessages().setMessage(en.getKey(),
							TextColors.RED + plugin.myLocale(en.getKey()).warpssignRemoved);
				}
				it.remove();
			}
		}
		saveWarpList();
		plugin.getWarpPanel().updatePanel();
	}

	/**
	 * Lists all the known warps
	 * 
	 * @return String set of warps
	 */
	public Set<UUID> listWarps() {
		return warpList.keySet();
	}

	/**
	 * @return Sorted list of warps with most recent players listed first
	 */
	public Collection<UUID> listSortedWarps() {
		// Bigger value of time means a more recent login
		TreeMap<Value<Instant>, UUID> map = new TreeMap<Value<Instant>, UUID>();
		for (UUID uuid : warpList.keySet()) {
			// If never played, will be zero
			Value<Instant> lastPlayed = plugin.getServer().getPlayer(uuid).get().lastPlayed();
			map.put(lastPlayed, uuid);
		}
		Collection<UUID> result = map.descendingMap().values();
		// Fire event
		WarpListEvent event = new WarpListEvent(plugin, result);
		Sponge.getEventManager().post(event);
		// Get the result of any changes by listeners
		result = event.getWarps();
		return result;
	}

	/**
	 * Provides the location of the warp for player or null if one is not found
	 * 
	 * @param player
	 *            - the warp requested
	 * @return Location of warp
	 */
	public Location<World> getWarp(UUID player) {
		if (warpList.containsKey(player)) {
			return warpList.get(player);
		} else {
			return null;
		}
	}

	/**
	 * @param location
	 * @return Name of warp owner
	 */
	public String getWarpOwner(Location<World> location) {
		for (UUID playerUUID : warpList.keySet()) {
			if (location.equals(warpList.get(playerUUID))) {
				return plugin.getPlayers().getName(playerUUID);
			}
		}
		return "";
	}

}