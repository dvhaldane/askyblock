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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

/**
 * This file improves the safety of boats in AcidIsland It enables
 * players to get out of boats without being dropped into the acid. It
 * enables players to hit a boat and have it pop into their inventory
 * immediately
 * 
 * @author tastybento
 */
public class SafeBoat  {
    // Flags to indicate if a player has exited a boat recently or not
    private static HashMap<UUID, Entity> exitedBoat = new HashMap<UUID, Entity>();
    // Stores players that should be ignored because they are being teleported away from 
    // a locked islands
    private static Set<UUID> ignoreList = new HashSet<UUID>();

    public SafeBoat(ASkyBlock aSkyBlock) {
    }

    /**
     * @param e
     *            This event check throws the boat at a player when they hit it
     *            unless someone is in it
     */
    @Listener
    public void onClick(DamageEntityEvent e) {
        // plugin.getLogger().info("Damage event " + e.getDamage());
        // Find out what block is being clicked
    	Boat boat = (Boat) e.getTargetEntity();
        if (!(boat instanceof Boat)) {
            return;
        }
        final World playerWorld = boat.getWorld();
        if (!playerWorld.getName().equalsIgnoreCase(Settings.worldName)) {
            // Not the right world
            return;
        }
        // plugin.getLogger().info("Boat ");
        // Find out who is doing the clicking
        if (!(e.getCause().first(Player.class).isPresent())) {
            // If a creeper blows up the boat, tough cookies!
            return;
        }
        Player p = e.getCause().first(Player.class).get();
        // Try to remove the boat and throw it at the player
        Location<World> boatSpot = new Location<World>(boat.getWorld(), boat.getLocation().getX(), boat.getLocation().getY() + 2, boat.getLocation().getZ());
        Location<World> throwTo = new Location<World>(boat.getWorld(), p.getLocation().getX(), p.getLocation().getY() + 1, p.getLocation().getZ());
        EntityType newBoat = EntityTypes.BOAT;
        // Find the direction the boat should move in
        Vector3d dir = throwTo.toVector().subtract(boatSpot.toVector()).normalize();
        dir = dir.multiply(0.5);
        Entity newB = boat.getWorld().createEntity(newBoat, boatSpot.getPosition());
        newB.setVelocity(dir);
        boat.remove();
        e.setCancelled(true);
    }

    /**
     * @param e
     *            This function prevents boats from exploding when they hit
     *            something
     */
    @Listener
    public void onBoatHit(VehicleDestroyEvent e) {
        // plugin.getLogger().info("Vehicle destroyed event called");
        final Entity boat = e.getVehicle();
        if (!(boat instanceof Boat)) {
            return;
        }
        if (!boat.getWorld().getName().equalsIgnoreCase(Settings.worldName)) {
            // Not the right world
            return;
        }
        if (!(e.getAttacker() instanceof Player)) {
            // plugin.getLogger().info("Attacker is not a player so cancel event");
            e.setCancelled(true);
        }
    }

    @Listener
    public void onTeleport(final MoveEntityEvent.Teleport e) {
        //
        // plugin.getLogger().info("DEBUG: Teleport called");
        Player player = (Player) e.getTargetEntity();
        if (SafeBoat.ignoreList.contains(player.getUniqueId())) {
            return;
        }
        // If the player is not teleporting due to boat exit, return
        if (!exitedBoat.containsKey(player.getUniqueId())) {
            return;
        }
        // Entity boat = exitedBoat.get(player.getUniqueId());
        // Reset the flag
        exitedBoat.remove(player.getUniqueId());
        // Okay, so a player is getting out of a boat in the the right world.
        // Now...
        //plugin.getLogger().info("DEBUG: Player just exited a boat");
        // Find a safe place for the player to land
        int radius = 0;
        while (radius++ < 2) {
            for (int x = player.getLocation().getBlockX() - radius; x < player.getLocation().getBlockX() + radius; x++) {
                for (int z = player.getLocation().getBlockZ() - radius; z < player.getLocation().getBlockZ() + radius; z++) {
                    for (int y = player.getLocation().getBlockY(); y < player.getLocation().getBlockY() + 2; y++) {
                        // The safe location to tp to is actually +0.5 to x and
                        // z.
                        final Location<World> loc = new Location<World>(player.getWorld(), (double) (x + 0.5), (double) y, (double) (z + 0.5));
                        // plugin.getLogger().info("XYZ is " + x + " " + y + " "
                        // + z);
                        // Make sure the location is safe
                        if (GridManager.isSafeLocation(loc)) {
                            // plugin.getLogger().info("Safe!");
                            e.getToTransform().setLocation(loc);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param e
     *            This event aims to put the player in a safe place when they
     *            exit the boat
     */
    @Listener
    public void onBoatExit(RideEntityEvent.Dismount e) {
        final Entity boat = e.getTargetEntity();
        if (!boat.getType().equals(EntityTypes.BOAT)) {
            // Not a boat
            return;
        }
        // LivingEntity entity = e.getExited();
        final Entity entityObj = e.getTargetEntity();
        if (!(entityObj.getType().equals(EntityTypes.PLAYER))) {
            return;
        }
        final Player player = (Player) entityObj;
        final World playerWorld = player.getWorld();
        if (!playerWorld.getName().equalsIgnoreCase(Settings.worldName)) {
            // Not the right world
            return;
        }
        if (SafeBoat.ignoreList.contains(player.getUniqueId())) {
            return;
        }
        // Set the boat exit flag for this player
        // midTeleport.add(player.getUniqueId());
        if (exitedBoat.containsKey(player.getUniqueId())) {
            // Debounce
            e.setCancelled(true);
        } else {
            exitedBoat.put(player.getUniqueId(), boat);
        }
        return;
    }

    /**
     * Temporarily ignore a player
     * @param player
     */
    public static void setIgnore(UUID player) {
        if (SafeBoat.ignoreList.contains(player)) {
            SafeBoat.ignoreList.remove(player);
        } else {
            SafeBoat.ignoreList.add(player);
        }
    }
}