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

import java.util.UUID;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

/**
 * This event is fired when a player joins a new Team
 * 
 * @author Exloki
 * 
 */
public class TeamJoinEvent implements Event {

    private final UUID player;
    private final UUID newTeamLeader;

    public TeamJoinEvent(UUID player, UUID newTeamLeader) {
        this.player = player;
        this.newTeamLeader = newTeamLeader;
    }

    /**
     * The UUID of the player changing Team
     * @return the player UUID
     */
    public UUID getPlayer() {
        return player;
    }

    /**
     * The UUID of the new Team's Leader
     * @return the team leader
     */
    public UUID getNewTeamLeader() {
        return newTeamLeader;
    }

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}
