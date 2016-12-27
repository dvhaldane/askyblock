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

package com.wasteofplastic.askyblock.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.schematics.Schematic;

/**
 * This event is fired when a player starts a new island
 * 
 * @author tastybento
 * 
 */
public class IslandNewEvent implements Event {
    private final Player player;
    private final Schematic schematic;
    private final Island island;

    /**
     * @param player
     * @param schematic
     * @param island
     */
    public IslandNewEvent(Player player, Schematic schematic, Island island) {
        this.player = player;
        this.schematic = schematic;
        this.island = island;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
    
    public Cause cause = Cause.builder().build();
    
    /**
     * @return the schematicName
     */
    public Schematic getSchematicName() {
        return schematic;
    }

    /**
     * @return the island
     */
    public Location<World> getIslandLocation() {
        return island.getCenter();
    }

    /**
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return island.getProtectionSize();
    }

    /**
     * @return the isLocked
     */
    public boolean isLocked() {
        return island.isLocked();
    }

    /**
     * @return the islandDistance
     */
    public int getIslandDistance() {
        return island.getIslandDistance();
    }

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}
