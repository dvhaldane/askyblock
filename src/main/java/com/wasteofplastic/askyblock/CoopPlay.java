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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.events.CoopJoinEvent;
import com.wasteofplastic.askyblock.events.CoopLeaveEvent;
import com.wasteofplastic.askyblock.util.Util;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

/**
 * Handles coop play interactions
 * 
 * @author tastybento
 * 
 */
public class CoopPlay {
	private static CoopPlay instance = new CoopPlay(ASkyBlock.getPlugin());
	// Stores all the coop islands, the coop player, the location and the
	// inviter
	private HashMap<UUID, HashMap<Location<World>, UUID>> coopPlayers = new HashMap<UUID, HashMap<Location<World>, UUID>>();
	// Defines whether a player is on a coop island or not
	// private HashMap<UUID, Location> onCoopIsland = new HashMap<UUID,
	// Location>();
	private ASkyBlock plugin;

	/**
	 * @param plugin
	 */
	private CoopPlay(ASkyBlock plugin) {
		this.plugin = plugin;
	}

	/**
	 * Adds a player to an island as a coop player.
	 * 
	 * @param requester
	 * @param newPlayer
	 */
	public void addCoopPlayer(Player requester, Player newPlayer) {
		// plugin.getLogger().info("DEBUG: adding coop player");
		// Find out which island this coop player is being requested to join
		Location<World> islandLoc = null;
		if (plugin.getPlayers().inTeam(requester.getUniqueId())) {
			islandLoc = plugin.getPlayers().getTeamIslandLocation(requester.getUniqueId());
			// Tell the team owner
			UUID leaderUUID = plugin.getPlayers().getTeamLeader(requester.getUniqueId());
			// Tell all the team members
			for (UUID member : plugin.getPlayers().getMembers(leaderUUID)) {
				// plugin.getLogger().info("DEBUG: " + member.toString());
				if (!member.equals(requester.getUniqueId())) {
					Optional<Player> player = plugin.getServer().getPlayer(member);
					if (player.isPresent()) {
						player.get().sendMessage(
								Text.of(TextColors.GOLD + plugin.myLocale(player.get().getUniqueId()).coopInvited
										.replace("[name]", requester.getDisplayNameData().displayName().toString())
										.replace("[player]", newPlayer.getName())));
						player.get().sendMessage(
								Text.of(TextColors.GOLD + plugin.myLocale(player.get().getUniqueId()).coopUseExpel));
					} else {
						if (member.equals(leaderUUID)) {
							// offline - tell leader
							plugin.getMessages().setMessage(leaderUUID,
									plugin.myLocale(leaderUUID).coopInvited
											.replace("[name]", requester.getDisplayNameData().displayName().toString())
											.replace("[player]", newPlayer.getName()));
						}
					}
				}
			}
		} else {
			islandLoc = plugin.getPlayers().getIslandLocation(requester.getUniqueId());
		}
		Island coopIsland = plugin.getGrid().getIslandAt(islandLoc);
		// Add the coop to the list. If the location already exists then the new
		// requester will replace the old
		if (coopPlayers.containsKey(newPlayer.getUniqueId())) {
			// This is an existing player in the list
			// Add this island to the set
			coopPlayers.get(newPlayer.getUniqueId()).put(coopIsland.getCenter(), requester.getUniqueId());
		} else {
			// First time. Create the hashmap
			HashMap<Location<World>, UUID> loc = new HashMap<Location<World>, UUID>();
			loc.put(coopIsland.getCenter(), requester.getUniqueId());
			coopPlayers.put(newPlayer.getUniqueId(), loc);
		}
		// Fire event
		final CoopJoinEvent event = new CoopJoinEvent(newPlayer.getUniqueId(), coopIsland, requester.getUniqueId());
		Sponge.getEventManager().post(event);
	}

	/**
	 * Removes a coop player
	 * 
	 * @param requester
	 * @param targetPlayer
	 * @return true if the player was a coop player, and false if not
	 */
	public boolean removeCoopPlayer(Player requester, Player targetPlayer) {
		return removeCoopPlayer(requester, targetPlayer.getUniqueId());
	}

	/**
	 * Returns the list of islands that this player is coop on or empty if none
	 * 
	 * @param player
	 * @return Set of locations
	 */
	public Set<Location<World>> getCoopIslands(Player player) {
		if (coopPlayers.containsKey(player.getUniqueId())) {
			return coopPlayers.get(player.getUniqueId()).keySet();
		}
		return new HashSet<Location<World>>();
	}

	/**
	 * Gets a list of all the players that are currently coop on this island
	 * 
	 * @param islandLoc
	 * @return List of UUID's of players that have coop rights to the island
	 */
	public List<UUID> getCoopPlayers(Location<World> islandLoc) {
		Island coopIsland = plugin.getGrid().getIslandAt(islandLoc);
		List<UUID> result = new ArrayList<UUID>();
		if (coopIsland != null) {
			for (UUID player : coopPlayers.keySet()) {
				if (coopPlayers.get(player).containsKey(coopIsland.getCenter())) {
					result.add(player);
				}
			}
		}
		return result;
	}

	/**
	 * Removes all coop players from an island - used when doing an island reset
	 * 
	 * @param player
	 */
	public void clearAllIslandCoops(UUID player) {
		// Remove any and all islands related to requester
		Island island = plugin.getGrid().getIsland(player);
		if (island == null) {
			return;
		}
		for (HashMap<Location<World>, UUID> coopPlayer : coopPlayers.values()) {
			for (UUID inviter : coopPlayer.values()) {
				// Fire event
				final CoopLeaveEvent event = new CoopLeaveEvent(player, inviter, island);
				Sponge.getEventManager().post(event);
			}
			coopPlayer.remove(island.getCenter());
		}
	}

	/**
	 * Deletes all coops from player. Used when player logs out.
	 * 
	 * @param player
	 */
	public void clearMyCoops(Player player) {
		// plugin.getLogger().info("DEBUG: clear my coops - clearing coops
		// memberships of " + player.getName());
		Island coopIsland = plugin.getGrid().getIsland(player.getUniqueId());
		if (coopPlayers.get(player.getUniqueId()) != null) {
			// plugin.getLogger().info("DEBUG: " + player.getName() + " is a
			// member of a coop");
			for (UUID inviter : coopPlayers.get(player.getUniqueId()).values()) {
				// Fire event
				// plugin.getLogger().info("DEBUG: removing invite from " +
				// plugin.getServer().getPlayer(inviter).getName());
				final CoopLeaveEvent event = new CoopLeaveEvent(player.getUniqueId(), inviter, coopIsland);
				Sponge.getEventManager().post(event);
			}
			coopPlayers.remove(player.getUniqueId());
		}
	}

	public void saveCoops() {
		File coopFile = new File(plugin.configDir(), "coops.yml");
		Util.loadFile("coops.yml");
		for (UUID playerUUID : coopPlayers.keySet()) {
			
			coopConfig.set(playerUUID.toString(), getMyCoops(playerUUID));
		}
		try {
			coopConfig.save(coopFile);
		} catch (IOException e) {
			plugin.getLogger().error("Could not save coop.yml file!");
		}
	}

	public void loadCoops() {
		CommentedConfigurationNode coopConfig = Util.loadFile("coops.yml");
		
		// Run through players
		for (String playerUUID : coopConfig.getValues(false).keySet()) {
			try {
				setMyCoops(UUID.fromString(playerUUID), coopConfig.getStringList(playerUUID));
			} catch (Exception e) {
				plugin.getLogger().error("Could not load coops for player UUID " + playerUUID + " skipping...");
			}
		}
	}

	/**
	 * Gets a serialize list of all the coops for this player. Used when saving
	 * the player
	 * 
	 * @param playerUUID
	 * @return List of island location | uuid of invitee
	 */
	private List<String> getMyCoops(UUID playerUUID) {
		List<String> result = new ArrayList<String>();
		if (coopPlayers.containsKey(playerUUID)) {
			for (Entry<Location<World>, UUID> entry : coopPlayers.get(playerUUID).entrySet()) {
				result.add(Util.getStringLocation(entry.getKey()) + "|" + entry.getValue().toString());
			}
		}
		return result;
	}

	/**
	 * Sets a player's coops from string. Used when loading a player.
	 * 
	 * @param playerUUID
	 * @param coops
	 */
	@SuppressWarnings("unused")
	private void setMyCoops(UUID playerUUID, List<String> coops) {
		try {
			HashMap<Location<World>, UUID> temp = new HashMap<Location<World>, UUID>();
			for (String coop : coops) {
				String[] split = coop.split("\\|");
				if (split.length == 2) {
					Island coopIsland = plugin.getGrid().getIslandAt(Util.getLocationString(split[0]));
					if (coopIsland != null) {
						temp.put(coopIsland.getCenter(), UUID.fromString(split[1]));
					}
				}
			}
			coopPlayers.put(playerUUID, temp);
		} catch (Exception e) {
			plugin.getLogger().error("Could not load coops for UUID " + playerUUID);
			e.printStackTrace();
		}
	}

	/**
	 * Goes through all the known coops and removes any that were invited by
	 * clearer. Returns any inventory Can be used when clearer logs out or when
	 * they are kicked or leave a team
	 * 
	 * @param clearer
	 */
	public void clearMyInvitedCoops(Player clearer) {
		// plugin.getLogger().info("DEBUG: clear my invited coops - clearing
		// coops that were invited by " + clearer.getName());
		Island coopIsland = plugin.getGrid().getIsland(clearer.getUniqueId());
		for (UUID playerUUID : coopPlayers.keySet()) {
			Iterator<Entry<Location<World>, UUID>> en = coopPlayers.get(playerUUID).entrySet().iterator();
			while (en.hasNext()) {
				Entry<Location<World>, UUID> entry = en.next();
				// Check if this invite was sent by clearer
				if (entry.getValue().equals(clearer.getUniqueId())) {
					// Yes, so get the invitee (target)
					Optional<Player> target = plugin.getServer().getPlayer(playerUUID);
					if (target.isPresent()) {
						target.get().sendMessage(Text.of(TextColors.RED + plugin.myLocale(playerUUID).coopRemoved
								.replace("[name]", clearer.getDisplayNameData().displayName().toString())));
					} else {
						plugin.getMessages().setMessage(playerUUID,
								TextColors.RED + plugin.myLocale(playerUUID).coopRemoved.replace("[name]",
										clearer.getDisplayNameData().displayName().toString()));
					}
					// Fire event
					final CoopLeaveEvent event = new CoopLeaveEvent(playerUUID, clearer.getUniqueId(), coopIsland);
					Sponge.getEventManager().post(event);
					// Mark them as no longer on a coop island
					// setOnCoopIsland(players, null);
					// Remove this entry
					en.remove();
				}
			}
		}
	}

	/**
	 * Removes all coop players from an island - used when doing an island reset
	 * 
	 * @param island
	 */
	public void clearAllIslandCoops(Location<World> island) {
		if (island == null) {
			return;
		}
		Island coopIsland = plugin.getGrid().getIslandAt(island);
		// Remove any and all islands related to requester
		for (HashMap<Location<World>, UUID> coopPlayer : coopPlayers.values()) {
			// Fire event
			final CoopLeaveEvent event = new CoopLeaveEvent(coopPlayer.get(island), coopIsland.getOwner(), coopIsland);
			Sponge.getEventManager().post(event);
			coopPlayer.remove(island);
		}
	}

	/**
	 * @return the instance
	 */
	public static CoopPlay getInstance() {
		return instance;
	}

	public boolean removeCoopPlayer(Player requester, UUID targetPlayerUUID) {
		boolean removed = false;
		/*
		 * plugin.getLogger().info("DEBUG: requester is " +
		 * requester.getName()); plugin.getLogger().info("DEBUG: target = " +
		 * targetPlayerUUID.toString()); for (UUID key : coopPlayers.keySet()) {
		 * plugin.getLogger().info("DEBUG: " + key + " ==> " +
		 * coopPlayers.get(key)); }
		 */
		// Only bother if the player is in the list
		if (coopPlayers.containsKey(targetPlayerUUID)) {
			Island coopIsland = plugin.getGrid().getIsland(requester.getUniqueId());
			if (coopIsland != null) {
				removed = coopPlayers.get(targetPlayerUUID).remove(coopIsland.getCenter()) != null ? true : false;
				// Fire event
				final CoopLeaveEvent event = new CoopLeaveEvent(targetPlayerUUID, requester.getUniqueId(), coopIsland);
				Sponge.getEventManager().post(event);
			}
		}
		return removed;
	}
}