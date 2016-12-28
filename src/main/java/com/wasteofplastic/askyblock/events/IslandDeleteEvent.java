/*******************************************************************************
 * This file is part of ASkyBlock.
 * <p>
 * ASkyBlock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * ASkyBlock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.askyblock.events;

import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * This event is fired when a player resets an island
 *
 * @author tastybento
 *
 */
public class IslandDeleteEvent {
	private final UUID playerUUID;
	private final Location<World> location;

	/**
	 * @param playerUUID
	 * @param oldLocation
	 */
	public IslandDeleteEvent(UUID playerUUID, Location<World> oldLocation) {
		this.playerUUID = playerUUID;
		this.location = oldLocation;
	}

	/**
	 * @return the player's UUID
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	/**
	 * @return the location
	 */
	public Location<World> getLocation() {
		return location;
	}
}