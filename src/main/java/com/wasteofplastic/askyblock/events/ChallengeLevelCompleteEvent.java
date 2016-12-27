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

import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * This event is fired when a player completes a challenge level
 * 
 * @author tastybento
 * 
 */
public class ChallengeLevelCompleteEvent implements Event {
    private final Player player;
    private final int oldLevel;
    private final int newLevel;
    private final List<ItemStack> rewardedItems;

    /**
     * @param player
     * @param oldLevel
     * @param newLevel
     * @param rewardedItems 
     */
    public ChallengeLevelCompleteEvent(Player player, int oldLevel, int newLevel, List<ItemStack> rewardedItems) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.rewardedItems = rewardedItems;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the oldLevel
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * @return the newLevel
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * @return the rewardedItems
     */
    public List<ItemStack> getRewardedItems() {
        return rewardedItems;
    }

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}
