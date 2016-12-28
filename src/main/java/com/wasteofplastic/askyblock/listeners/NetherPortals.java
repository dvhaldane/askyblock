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

import java.util.UUID;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.GridManager;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.Island.Flags;
import com.wasteofplastic.askyblock.SafeSpotTeleport;
import com.wasteofplastic.askyblock.Settings;
import com.wasteofplastic.askyblock.commands.IslandCmd;
import com.wasteofplastic.askyblock.schematics.Schematic;

public class NetherPortals {
    private final ASkyBlock plugin;
    private final static boolean DEBUG = false;

    public NetherPortals(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * This handles non-player portal use
     * Currently disables portal use by entities
     * 
     * @param event
     */
    @Listener
    public void onEntityPortal(MoveEntityEvent.Teleport.Portal event) {
        if (DEBUG)
            plugin.getLogger().info("DEBUG: nether portal entity " + event.getTargetEntity());
        // If the nether is disabled then quit immediately
        if (!Settings.createNether || ASkyBlock.getNetherWorld() == null) {
            return;
        }
        if (event.getTargetEntity() == null) {
            return;
        }
        if (event.getPortalAgent() != null && event.getFromTransform().getLocation().getBlock().getType().equals(BlockTypes.END_PORTAL)) {
            event.setCancelled(true);
            // Same action for all worlds except the end itself
            if (!event.getFromTransform().getExtent().getDimension().getType().equals(DimensionTypes.THE_END)) {
                if (plugin.getServer().getWorld(Settings.worldName + "_the_end").isPresent()) {
                    // The end exists
                    Location<World> end_place = plugin.getServer().getWorld(Settings.worldName + "_the_end").get().getSpawnLocation();
                    event.getTargetEntity().teleport(end_place);
                    if (DEBUG)
                        plugin.getLogger().info("DEBUG: Result teleported " + event.getTargetEntity() + " to " + end_place);
                    return;
                }
            }
            return;
        }
        Location<World> currentLocation = event.getFromTransform().getLocation();
        String currentWorld = currentLocation.getExtent().getName();
        // Only operate if this is Island territory
        if (!currentWorld.equalsIgnoreCase(Settings.worldName) && !currentWorld.equalsIgnoreCase(Settings.worldName + "_nether")) {
            return;
        }
        // No entities may pass with the old nether
        if (!Settings.newNether) {
            event.setCancelled(true);
            return;
        }
        // New nether
        // Entities can pass only if there are adjoining portals
        Location<World> dest = event.getFromTransform().toVector().toLocation(ASkyBlock.getIslandWorld());
        if (event.getFromTransform().getExtent().getDimension().getType().equals(DimensionTypes.OVERWORLD)) {
            dest = event.getFrom().toVector().toLocation(ASkyBlock.getNetherWorld());
        }
        // Vehicles
        if (event.getEntity() instanceof Vehicle) {
            Vehicle vehicle = (Vehicle)event.getEntity();   
            vehicle.eject();
        }
        new SafeSpotTeleport(plugin, event.getEntity(), dest);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (DEBUG)
            plugin.getLogger().info("Player portal event - reason =" + event.getCause());
        UUID playerUUID = event.getPlayer().getUniqueId();
        // If the nether is disabled then quit immediately
        if (!Settings.createNether || ASkyBlock.getNetherWorld() == null) {
            return;
        }
        Location currentLocation = event.getFrom().clone();
        String currentWorld = currentLocation.getWorld().getName();
        if (!currentWorld.equalsIgnoreCase(Settings.worldName) && !currentWorld.equalsIgnoreCase(Settings.worldName + "_nether")
                && !currentWorld.equalsIgnoreCase(Settings.worldName + "_the_end")) {
            return;
        }
        // Check if player has permission
        Island island = plugin.getGrid().getIslandAt(currentLocation);
        if ((island == null && !Settings.allowPortalUse) || (island != null && !island.getIgsFlag(Flags.allowPortalUse))) {
            // Portal use is disallowed for visitors, but okay for ops or bypass
            // mods
            if (!event.getPlayer().isOp() && !VaultHelper.checkPerm(event.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
                // Portals use is always allowed around the spawn
                if (!plugin.getGrid().locationIsOnIsland(event.getPlayer(), event.getPlayer().getLocation())
                        && !plugin.getGrid().isAtSpawn(event.getPlayer().getLocation())) {
                    event.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(event.getPlayer().getUniqueId()).islandProtected);
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // Determine what portal it is
        switch (event.getCause()) {
        case END_PORTAL:
            // Same action for all worlds except the end itself
            if (!event.getFrom().getWorld().getEnvironment().equals(Environment.THE_END)) {
                if (plugin.getServer().getWorld(Settings.worldName + "_the_end") != null) {
                    // The end exists
                    event.setCancelled(true);
                    Location end_place = plugin.getServer().getWorld(Settings.worldName + "_the_end").getSpawnLocation();
                    if (GridManager.isSafeLocation(end_place)) {
                        event.getPlayer().teleport(end_place);
                        // event.getPlayer().sendBlockChange(end_place,
                        // end_place.getBlock().getType(),end_place.getBlock().getData());
                        return;
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(event.getPlayer().getUniqueId()).warpserrorNotSafe);
                        plugin.getGrid().homeTeleport(event.getPlayer());
                        return;
                    }
                }
            } else {
                event.setCancelled(true);
                plugin.getGrid().homeTeleport(event.getPlayer());
            }
            break;
        case NETHER_PORTAL:
            // Get the home world of this player
            World homeWorld = ASkyBlock.getIslandWorld();
            Location home = plugin.getPlayers().getHomeLocation(event.getPlayer().getUniqueId());
            if (home != null) {
                homeWorld = home.getWorld();
            }
            if (!Settings.newNether) {
                // Legacy action
                if (event.getFrom().getWorld().getEnvironment().equals(Environment.NORMAL)) {
                    // Going to Nether
                    if (homeWorld.getEnvironment().equals(Environment.NORMAL)) {
                        // Home world is over world
                        event.setTo(ASkyBlock.getNetherWorld().getSpawnLocation());
                        event.useTravelAgent(true); 
                    } else {
                        // Home world is nether - going home
                        event.useTravelAgent(false);
                        Location dest = plugin.getGrid().getSafeHomeLocation(playerUUID,1);
                        if (dest != null) {
                            event.setTo(dest);
                        } else {
                            event.setCancelled(true);
                            new SafeSpotTeleport(plugin, event.getPlayer(), plugin.getPlayers().getIslandLocation(playerUUID), 1);
                        }		
                    }
                } else {
                    // Going to Over world
                    if (homeWorld.getEnvironment().equals(Environment.NORMAL)) {
                        // Home world is over world
                        event.useTravelAgent(false);
                        Location dest = plugin.getGrid().getSafeHomeLocation(playerUUID,1);
                        if (dest != null) {
                            event.setTo(dest);
                        } else {
                            event.setCancelled(true);
                            new SafeSpotTeleport(plugin, event.getPlayer(), plugin.getPlayers().getIslandLocation(playerUUID), 1);
                        }
                    } else {
                        // Home world is nether 
                        event.setTo(ASkyBlock.getIslandWorld().getSpawnLocation());
                        event.useTravelAgent(true); 
                    }
                }
            } else {
                // New Nether
                // Get location of the island where the player is at
                if (island == null) {
                    event.setCancelled(true);
                    return;
                }
                // Can go both ways now
                Location overworldIsland = island.getCenter().toVector().toLocation(ASkyBlock.getIslandWorld());
                Location netherIsland = island.getCenter().toVector().toLocation(ASkyBlock.getNetherWorld());
                //Location dest = event.getFrom().toVector().toLocation(ASkyBlock.getIslandWorld());
                if (event.getFrom().getWorld().getEnvironment().equals(Environment.NORMAL)) {
                    // Going to Nether
                    // Check that there is a nether island there. Due to legacy reasons it may not exist
                    if (DEBUG)
                        plugin.getLogger().info("DEBUG: island center = " + island.getCenter());               
                    if (netherIsland.getBlock().getType() != Material.BEDROCK) {
                        // Check to see if there is anything there
                        if (plugin.getGrid().bigScan(netherIsland, 20) == null) {
                            if (DEBUG)
                                plugin.getLogger().info("DEBUG: big scan is null");
                            plugin.getLogger().warning("Creating nether island for " + event.getPlayer().getName() + " using default nether schematic");
                            Schematic nether = IslandCmd.getSchematics().get("nether");
                            if (nether != null) {
                                if (DEBUG)
                                    plugin.getLogger().info("DEBUG: pasting at " + island.getCenter().toVector());
                                plugin.getIslandCmd().pasteSchematic(nether, netherIsland, event.getPlayer());
                            } else {
                                plugin.getLogger().severe("Cannot teleport player to nether because there is no nether schematic");
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(event.getPlayer().getUniqueId()).warpserrorNotSafe);
                                return;
                            }
                        }
                    }
                    if (DEBUG)
                        plugin.getLogger().info("DEBUG: Teleporting to " + event.getFrom().toVector().toLocation(ASkyBlock.getNetherWorld()));
                    event.setCancelled(true);
                    // Teleport using the new safeSpot teleport
                    new SafeSpotTeleport(plugin, event.getPlayer(), netherIsland);
                    return;
                }
                // Going to the over world - if there isn't an island, do nothing
                event.setCancelled(true);
                // Teleport using the new safeSpot teleport
                new SafeSpotTeleport(plugin, event.getPlayer(), overworldIsland);
            }
            break;
        default:
            break;
        }
    }
    // Nether portal spawn protection

    /**
     * Function to check proximity to nether spawn location
     * 
     * @param player
     * @return true if in the spawn area, false if not
     */
    private boolean awayFromSpawn(Player player) {
        Vector p = player.getLocation().toVector().multiply(new Vector(1, 0, 1));
        Vector spawn = player.getWorld().getSpawnLocation().toVector().multiply(new Vector(1, 0, 1));
        if (spawn.distanceSquared(p) < (Settings.netherSpawnRadius * Settings.netherSpawnRadius)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Prevents blocks from being broken
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (DEBUG)
            plugin.getLogger().info("DEBUG: " + e.getEventName());
        // plugin.getLogger().info("Block break");
        if ((e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether") && !Settings.newNether)
                || e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
            if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
                return;
            }
            if (DEBUG)
                plugin.getLogger().info("Block break in island nether");
            if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
                e.getPlayer().sendMessage(plugin.myLocale(e.getPlayer().getUniqueId()).netherSpawnIsProtected);
                e.setCancelled(true);
            }
        }

    }

    /**
     * Prevents placing of blocks
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBlockPlace(final BlockPlaceEvent e) {
        if (DEBUG)
            plugin.getLogger().info("DEBUG: " + e.getEventName());
        if (!Settings.newNether) {
            if (e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
                    || e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
                if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
                    return;
                }
                if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        if (DEBUG)
            plugin.getLogger().info("DEBUG: " + e.getEventName());
        if (!Settings.newNether) {
            if (e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
                    || e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
                if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
                    return;
                }
                if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Prevent the Nether spawn from being blown up
     * 
     * @param e
     */
    @Listener
    public void onExplosion(final DetonateExplosiveEvent e) {
        if (Settings.newNether) {
            // Not used in the new nether
            return;
        }
        // Find out what is exploding
        Entity expl = e.getTargetEntity();
        if (expl == null) {
            return;
        }
        // Check world
        if (!expl.getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
                || expl.getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
            return;
        }
        Location<World> spawn = expl.getWorld().getSpawnLocation();
        Location<World> loc = expl.getLocation();
        if (spawn.distance(loc) < Settings.netherSpawnRadius) {
            e..blockList().clear();
        }
    }

    /**
     * Converts trees to gravel and glowstone
     * 
     * @param e
     */
    @Listener
    public void onTreeGrow(final ChangeBlockEvent.Grow e) {
        if (DEBUG)
            plugin.getLogger().info("DEBUG: " + e);

        if (!Settings.newNether || !Settings.netherTrees) {
            return;
        }
        // Check world
        if (!e.getTargetWorld().equals(ASkyBlock.getNetherWorld())) {
            return;
        }
        for (Transaction<BlockSnapshot> b : e.getTransactions()) {
            if (b.getFinal().getExtendedState().getType() == BlockTypes.LOG || b.getFinal().getExtendedState().getType() == BlockTypes.LOG2) {
            	b.getFinal().getLocation().get().setBlock(BlockState.builder().blockType(BlockTypes.GRAVEL).build(), Cause.builder().build());
            } else if (b.getFinal().getExtendedState().getType() == BlockTypes.LEAVES || b.getFinal().getExtendedState().getType() == BlockTypes.LEAVES2) {
            	b.getFinal().getLocation().get().setBlock(BlockState.builder().blockType(BlockTypes.GLOWSTONE).build(), Cause.builder().build());
            }
        }
    }
}