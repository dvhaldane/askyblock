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

/**
 * This event is fired when a player resets an island
 * 
 * @author tastybento
 * 
 */
public class IslandResetEvent implements Event {
    private final Player player;
    private final Location<World> location;

    /**
     * @param player
     * @param oldLocation
     */
    public IslandResetEvent(Player player, Location<World> oldLocation) {
        this.player = player;
        this.location = oldLocation;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * @return the location
     */
    public Location<World> getLocation() {
        return location;
    }

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}
